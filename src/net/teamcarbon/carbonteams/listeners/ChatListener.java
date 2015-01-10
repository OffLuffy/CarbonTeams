package net.teamcarbon.carbonteams.listeners;


import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.CarbonTeams.ChatType;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class ChatListener implements Listener {
	
	@EventHandler(ignoreCancelled = false)
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
			
		// Chat Mode Change Event
		if (e.getMessage().equalsIgnoreCase("##")) {
			e.setCancelled(true);
			if (!Team.hasTeam(player)) {
				player.sendMessage(CustomMessage.NO_CHAT_TEAM.pre());
				return;
			}
			CarbonTeams.setChatMode(player,
					CarbonTeams.getChatMode(player) == ChatType.TEAM ? ChatType.NORMAL : ChatType.TEAM);
			if (CarbonTeams.getChatMode(player) == ChatType.TEAM)
			player.sendMessage(ChatColor.YELLOW + "Now talking in Team Chat.");
			return;
		} else if (e.getMessage().equalsIgnoreCase("##")) {
			e.setCancelled(true);
			CarbonTeams.setChatMode(player, ChatType.NORMAL);
			player.sendMessage(ChatColor.YELLOW + "Now talking in ALL chat.");
			return;
		}
			
		// Chat handling
		ChatType mode = CarbonTeams.getChatMode(player);
		// Only deal with chat if not in normal chat mode
		if (mode != ChatType.NORMAL) {
			// Cancel the event so it doesn't send the message to normal chat
			e.setCancelled(true);
			ConfigurationSection sect = CarbonTeams.inst.getConfig().getConfigurationSection("message-settings");
			String defTeamPattern = "&8[&b{PREFIX}&8] &7{SENDER}:&r {MESSAGE}";
			String defAllyPattern = "&7♥&8[&b{PREFIX}&8] &7{SENDER}:&r {MESSAGE}";
			if (Team.hasTeam(player)) {
				Team t = Team.getTeam(player);
				HashMap<String, String> additional = new HashMap<String, String>();
				additional.put("{MESSAGE}", e.getMessage());
				additional.put("{SENDER}", player.getName());
				if (mode == ChatType.TEAM) {
					t.sendTeamMessage(player, t.vars(sect.getString("team-message-pattern", defTeamPattern), additional));
				} else if (mode == ChatType.ALLY) {
					t.sendTeamMessage(player, t.vars(sect.getString("ally-message-pattern", defAllyPattern), additional));
				}
			} else {
				player.sendMessage(CustomMessage.NO_CHAT_TEAM.pre());
			}
		}
	}
}
