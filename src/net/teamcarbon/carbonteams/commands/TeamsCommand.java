package net.teamcarbon.carbonteams.commands;

import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

@SuppressWarnings("deprecation")
public class TeamsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String perm = "carbonteams."; // So I don't have to retype this everytime
		boolean isPlayer = sender instanceof Player;
		if (args.length == 0 || MiscUtils.eq(args[0], "help", "?")) {
			PluginDescriptionFile pdf = CarbonTeams.inst.getDescription();
			if (!isPlayer) {
				CustomMessage.printHeader(sender, pdf.getName() + " Console Commands");
				sender.sendMessage(Clr.BLUE + "<> = Required, [] = Optional");
				// No need for perm checks when executing as console
				sender.sendMessage(Clr.LIME + "/" + label + Clr.GRAY + " Teams help menu");
				sender.sendMessage(Clr.LIME + "/" + label + " create <team>" + Clr.GRAY + " Creates a new empty team");
				sender.sendMessage(Clr.LIME + "/" + label + " create <team> [users]" + Clr.GRAY + " Creates a new team add users in it");
				sender.sendMessage(Clr.LIME + "/" + label + " delete <team>" + Clr.GRAY + " Deletes a team");
				sender.sendMessage(Clr.LIME + "/" + label + " kick <team> <users>" + Clr.GRAY + " Kick users from a team");
				sender.sendMessage(Clr.LIME + "/" + label + " add <team> <users>" + Clr.GRAY + " Adds users to a team");
			} else {
				CustomMessage.printHeader(sender, "CarbonTeams Help");
				// TODO Players can join/invite/create/remove/set (with permission)
				sender.sendMessage(Clr.LIME + "/" + label + Clr.GRAY + " Teams help menu");
				// This method sends the message if the player has a specified permission
				ps(sender, perm+"create",			Clr.LIME+"/"+label+" create <team>" + Clr.GRAY + " Creates your team");
				ps(sender, perm+"delete.self",		Clr.LIME+"/"+label+" delete" + Clr.GRAY + " Deletes your team");
				ps(sender, perm+"delete.others",	Clr.LIME+"/"+label+" delete <team>" + Clr.GRAY + " Deletes another team");
				ps(sender, perm+"kick.others",		Clr.LIME+"/"+label+" kick <team> [users]" + Clr.GRAY + " Kicks users out of another team");
				ps(sender, perm+"accept",			Clr.LIME+"/"+label+" accept [team]" + Clr.GRAY + " Accepts an invite to a team");
				ps(sender, perm+"join",				Clr.LIME+"/"+label+" join <team>" + Clr.GRAY + " Join a team if unlocked or invited");
				if (Team.hasTeam((Player)sender)) {
					Team t = Team.getTeam((Player)sender);
					if (t.isLeader((Player)sender) || t.canMembersInvite())
						ps(sender, perm + "invite", Clr.LIME + "/" + label + " invite [users]" + Clr.GRAY + " Invites users to your team team");
					if (t.isLeader((Player) sender)) {
						ps(sender, perm + "kick.self",			Clr.LIME+"/"+label+" kick [users]" + Clr.GRAY + " Kicks users out of your team");
						ps(sender, perm + "set.self.home",		Clr.LIME+"/"+label+" set home" + Clr.GRAY + " Set your team's home where you're standing");
						ps(sender, perm + "set.self.banner",	Clr.LIME+"/"+label+" set banner" + Clr.GRAY + " Set your team's banner (hold a banner)");
						ps(sender, perm + "set.self.prefix",	Clr.LIME+"/"+label+" set prefix <prefix>" + Clr.GRAY + " Set your team's prefix");
						ps(sender, perm + "set.self.postfix",	Clr.LIME+"/"+label+" set postfix <postfix>" + Clr.GRAY + " Set your team's postfix");
						ps(sender, perm + "set.self.greeting",	Clr.LIME+"/"+label+" set greeting <greeting>" + Clr.GRAY + " Set your team's greeting");
						ps(sender, perm + "set.self.notice",	Clr.LIME+"/"+label+" set notice <notice>" + Clr.GRAY + " Set your team's notice");
						ps(sender, perm + "set.self.title",		Clr.LIME+"/"+label+" set title <title>" + Clr.GRAY + " Set your team's title");
						ps(sender, perm + "set.self.locked",	Clr.LIME+"/"+label+" set locked [on|off]" + Clr.GRAY + " Toggle if players can join without invites");
						ps(sender, perm + "set.self.invites",	Clr.LIME+"/"+label+" set invites [on|off]" + Clr.GRAY + " Toggle if members can invite or not");
					}
				}
			}
			return true;
		}
		/*if (args.length == 1) {
			if (player.hasPermission("teams.admin")) {
				if (MiscUtils.eq(args[0], "create")) {
					player.sendMessage(ChatColor.RED + "/teams create [TeamName]");
				}
				if (MiscUtils.eq(args[0], "spy")) {
					CarbonTeams.toggleSpying(player);
					player.sendMessage(ChatColor.YELLOW + (CarbonTeams.isSpying(player)?"You are now spying on team/ally chat":"Spy Chat mode off."));
				}
				return true;
			}
			if (MiscUtils.eq(args[0], "chat")) {
				if (Team.hasTeam(player)) {
					ChatType type = CarbonTeams.getChatMode(player);
					switch (type) {
						case ENEMY:
							player.sendMessage(ChatColor.YELLOW + "Now talking in ALL chat.");
							ChatListener.chatMode.put(player.getUniqueId(), ChatType.NORMAL);
							break;
						default:
							player.sendMessage(ChatColor.YELLOW + "Now talking in Team Chat.");
							ChatListener.chatMode.put(player.getUniqueId(), ChatType.TEAM);
							break;
					}
				} else {
					player.sendMessage(ChatColor.RED + "You do not have a team to chat with!");
				}
				return true;
			}
			if (MiscUtils.eq(args[0], "home")) {
				if (Team.hasTeam(player)) {
					Team t = Team.getTeam(player);
					player.teleport(t.getHome());
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Teleporting to cubeteams home...");
					return true;
				}
				player.sendMessage(ChatColor.RED + "You are not a part of any team to go home to!");
				return true;
			}
		}
		if (!player.hasPermission("teams.admin")) { return true; }
		if (args.length == 2) {
			if (MiscUtils.eq(args[0], "create")) {
				if (Team.getTeam(args[1]) != null) {
					player.sendMessage(ChatColor.RED + "Team already exists: " + args[1]);
					return true;
				}
				new Team(args[1]);
				player.sendMessage(ChatColor.GRAY + "You have created the team: " + ChatColor.AQUA + args[1]);
				return true;
			}
			if (MiscUtils.eq(args[0], "sethome")) {
				if (Team.getTeam(args[1]) != null) {
					Team t = Team.getTeam(args[1]);
					t.setHome(player.getLocation());
					player.sendMessage(ChatColor.GREEN + "You have set home for the team: " + t.getName());
				} else {
					player.sendMessage(ChatColor.RED + "Team does not exist: " + args[1]);
				}
				return true;
			}
		}
		if (args.length > 2) {
			if (MiscUtils.eq(args[0], "create")) {
				if (Team.getTeam(args[1]) != null) {
					player.sendMessage(ChatColor.RED + "Team already exists: " + args[1]);
					return true;
				}
				Team t = new Team(args[1]);
				for (int i = 2; i < args.length; i++) { // TODO Convert for loop to echo comma list of usernames
					if (Bukkit.getPlayer(args[i]) == null) {
						player.sendMessage(ChatColor.RED + "Could not add " + args[i] + " to the team, is he offline?");
					} else {
						Player p = Bukkit.getPlayer(args[i]);
						t.addMember(p);
						player.sendMessage(ChatColor.GREEN + p.getName() + " has successfully been assigned to the team " + t.getName());
					}
				}
				player.sendMessage(ChatColor.GRAY + "You have created the team: " + ChatColor.AQUA + args[1]);
				return true;
			}
			if (MiscUtils.eq(args[0], "add")) {
				if (Team.getTeam(args[1]) != null) {
					Team t = Team.getTeam(args[1]);
					for (int i = 2; i < args.length; i++) { // TODO Convert for loop to echo comma list of usernames
						if (Bukkit.getPlayer(args[i]) == null) {
							player.sendMessage(ChatColor.RED + "Could not add " + args[i] + " to the team, is he offline?");
						} else {
							Player p = Bukkit.getPlayer(args[i]);
							t.addMember(p);
							player.sendMessage(ChatColor.GREEN + p.getName() + " has successfully been assigned to the team " + t.getName());
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "Team does not exist: " + args[1]);
					return true;
				}
			}
			if (MiscUtils.eq(args[0], "remove")) {
				if (Team.getTeam(args[1]) != null) {
					Team t = Team.getTeam(args[1]);
					for (int i = 2; i < args.length; i++) {
						boolean on = false;
						if (Bukkit.getPlayer(args[i]) != null) { on = true; }
						if (on) {
							if (t.isMember(Bukkit.getPlayer(args[i]))) {
								t.removeMember(Bukkit.getPlayer(args[i]));
								player.sendMessage(ChatColor.GREEN + "Removed " + args[i] + " from the team");
							}
						} else if (t.isMember(Bukkit.getOfflinePlayer(args[i]))) {
							t.removeMember(Bukkit.getOfflinePlayer(args[i]));
							player.sendMessage(ChatColor.GREEN + "Removed " + args[i] + " from the team");
						}
					}
					return true;
				} else {
					player.sendMessage(ChatColor.RED + "Team does not exist: " + args[1]);
					return true;
				}
			}
		}*/
		return true;
	}

	// Shorthand method, just to shorten line lengths since I use it a lot above.
	private void ps(CommandSender s, String p, String m) { MiscUtils.permSend(s, p, m); }
}
