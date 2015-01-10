package net.teamcarbon.carbonteams.commands;

import net.teamcarbon.carbonlib.MiscUtils;
import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.CarbonTeams.ChatType;
import net.teamcarbon.carbonteams.listeners.ChatListener;
import net.teamcarbon.carbonteams.utils.Team;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

@SuppressWarnings("deprecation")
public class TeamsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.NOT_ONLINE.noPre());
			return true;
		}
		Player player = (Player)sender;
		if (args.length == 0) {
			PluginDescriptionFile pdf = CarbonTeams.inst.getDescription();
			String aliases = "/" + CarbonTeams.inst.getServer().getPluginCommand("teams").getLabel();
			for (String s : CarbonTeams.inst.getServer().getCommandAliases().get("teams")) { aliases += ", /" + s; }

			CustomMessage.printHeader(player, pdf.getName() + " v" + pdf.getVersion());
			player.sendMessage(ChatColor.GRAY + "Aliases: " + aliases);
			player.sendMessage(ChatColor.GREEN + "To change chat modes, do #Team, if you want to revert back to ALL chat, do ##");
			player.sendMessage(ChatColor.GREEN + "/" + label + " " + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Shows this help menu.");
			player.sendMessage(ChatColor.GREEN + "/" + label + " home" + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + "Teleports you to your team's home");
			if (player.hasPermission("teams.admin")) {
				player.sendMessage(ChatColor.GREEN + "/" + label + " create <name>" + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Creates a Team");
				player.sendMessage(ChatColor.GREEN + "/" + label + " create <name> [user] [user] ..." + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Creates a team with the specified players in it");
				player.sendMessage(ChatColor.GREEN + "/" + label + " delete <name>" + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Deletes a team");
				player.sendMessage(ChatColor.GREEN + "/" + label + " add [team] [user] [user] ..." + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Adds players to the specified team");
				player.sendMessage(ChatColor.GREEN + "/" + label + " remove [team] [user] [user] ..." + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Removes the players from the team");
				player.sendMessage(ChatColor.GREEN + "/" + label + " sethome [team]" + ChatColor.DARK_GRAY + " || " + ChatColor.GRAY + " Sets the teams home");
			}
			return true;
		}
		if (args.length == 1) {
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
					// TODO Fix these errors
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
		}
		return true;
	}
}
