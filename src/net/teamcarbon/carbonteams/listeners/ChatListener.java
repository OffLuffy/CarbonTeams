package net.teamcarbon.carbonteams.listeners;


import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.CarbonTeams.ChatType;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class ChatListener implements Listener {
	
	@EventHandler()
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		ChatType ct = CarbonTeams.getChatMode(p);
		String[] msgParts = e.getMessage().split(" ");
		// Chat Mode Change Event
		if (MiscUtils.eq(msgParts[0], "#t", "#tc")) {
			e.setCancelled(true);
			if (!MiscUtils.perm(p, "carbonteams.chat.teams")) {
				p.sendMessage(CustomMessage.NO_PERM.noPre());
				return;
			}
			if (!Team.hasTeam(p)) {
				p.sendMessage(CustomMessage.NO_CHAT_TEAM.pre());
			} else {
				if (msgParts.length > 1) { // Send just this message to team chat, stay in previous mode
					Team.getTeam(p).sendTeamMessage(p, MiscUtils.stringFromSubArray(" ", 1, msgParts.length-1, msgParts));
				} else { // Switch to team chat mode
					if (ct == ChatType.TEAM) {
						p.sendMessage(CustomMessage.TEAM_CHAT_ALREADY.pre());
					} else {
						CarbonTeams.setChatMode(p, ChatType.TEAM);
						p.sendMessage(CustomMessage.TEAM_CHAT_MODE.pre());
					}
				}
			}
			return;
		} else if (MiscUtils.eq(msgParts[0], "#a", "#ac")) {
			e.setCancelled(true);
			if (!MiscUtils.perm(p, "carbonteams.chat.allies")) {
				p.sendMessage(CustomMessage.NO_PERM.noPre());
				return;
			}
			if (!Team.hasTeam(p)) {
				p.sendMessage(CustomMessage.NO_CHAT_TEAM.pre());
			} else {
				if (msgParts.length > 1) { // Send just this message to ally chat, stay in previous mode
					Team.getTeam(p).sendAllyMessage(p, MiscUtils.stringFromSubArray(" ", 1, msgParts.length-1, msgParts));
				} else { // Switch to ally chat mode
					if (ct == ChatType.ALLY) {
						p.sendMessage(CustomMessage.ALLY_CHAT_ALREADY.pre());
					} else {
						CarbonTeams.setChatMode(p, ChatType.ALLY);
						p.sendMessage(CustomMessage.ALLY_CHAT_MODE.pre());
					}
				}
			}
			return;
		} else if (MiscUtils.eq(msgParts[0], "##", "#p", "#pc")) {
			// No permission check since it's public chat, not handled by this plugin
			if (msgParts.length > 1) { // Send just this message to normal chat, stay in previous mode
				// Lets remove the hashtag prefix from the message, then let the event execute normally
				e.setMessage(MiscUtils.stringFromSubArray(" ", 1, msgParts.length-1, msgParts));
			} else { // Switch to normal chat mode
				e.setCancelled(true);
				if (ct == ChatType.NORMAL) {
					p.sendMessage(CustomMessage.ALL_CHAT_ALREADY.pre());
				} else {
					CarbonTeams.setChatMode(p, ChatType.NORMAL);
					p.sendMessage(CustomMessage.ALL_CHAT_MODE.pre());
				}
			}
			return;
		}
			
		// If they don't specify a chat mode prefix, it'll fall down to this to handle chatting normally
		ChatType mode = CarbonTeams.getChatMode(p);
		// Only deal with chat if not in normal chat mode
		if (mode != ChatType.NORMAL) {
			// Cancel the event so it doesn't send the message to normal chat
			e.setCancelled(true);
			ConfigurationSection sect = CarbonTeams.inst.getConfig().getConfigurationSection("message-settings");
			String defTeamPattern = "&8[&b{PREFIX}&8] &7{SENDER}:&b {MESSAGE}";
			String defAllyPattern = "&7♥&8[&b{PREFIX}&8] &7{SENDER}:&3 {MESSAGE}";
			if (Team.hasTeam(p)) {
				Team t = Team.getTeam(p);
				HashMap<String, String> additional = new HashMap<String, String>();
				additional.put("{MESSAGE}", e.getMessage());
				additional.put("{SENDER}", sect.getBoolean("use-player-displayname", false)?p.getDisplayName():p.getName());
				if (mode == ChatType.TEAM) {
					t.sendTeamMessage(p, t.vars(sect.getString("team-message-pattern", defTeamPattern), additional));
				} else if (mode == ChatType.ALLY) {
					t.sendTeamMessage(p, t.vars(sect.getString("ally-message-pattern", defAllyPattern), additional));
				}
			} else {
				p.sendMessage(CustomMessage.NO_CHAT_TEAM.pre());
			}
		}
	}
}
