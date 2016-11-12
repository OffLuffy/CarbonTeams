package net.teamcarbon.carbonteams.commands;

import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
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
						ps(sender, perm+"kick.self", label,			"kick [users]" + Clr.GRAY + " Kicks users out of your team");
						ps(sender, perm+"set.self.home", label,		"set home" + Clr.GRAY + " Set your team's home where you're standing");
						ps(sender, perm+"set.self.banner", label,	"set banner" + Clr.GRAY + " Set your team's banner (hold a banner)");
						ps(sender, perm+"set.self.prefix", label,	"set prefix <prefix>" + Clr.GRAY + " Set your team's prefix");
						ps(sender, perm+"set.self.postfix", label,	"set postfix <postfix>" + Clr.GRAY + " Set your team's postfix");
						ps(sender, perm+"set.self.greeting", label,	"set greeting <greeting>" + Clr.GRAY + " Set your team's greeting");
						ps(sender, perm+"set.self.notice", label,	"set notice <notice>" + Clr.GRAY + " Set your team's notice");
						ps(sender, perm+"set.self.title", label,	"set title <title>" + Clr.GRAY + " Set your team's title");
						ps(sender, perm+"set.self.locked", label,	"set locked [on|off]" + Clr.GRAY + " Toggle if players can join without invites");
						ps(sender, perm+"set.self.invites", label,	"set invites [on|off]" + Clr.GRAY + " Toggle if members can invite or not");
					}
				}
			}
			return true;
		}
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "set")) {
				if (args.length == 1) {
					// TODO Print set help
				} else {
					if (MiscUtils.eq(args[1], "home")) {

					} else if (MiscUtils.eq(args[1], "banner")) {

					} else if (MiscUtils.eq(args[1], "prefix")) {

					} else if (MiscUtils.eq(args[1], "postfix")) {

					} else if (MiscUtils.eq(args[1], "greeting")) {

					} else if (MiscUtils.eq(args[1], "notice")) {

					} else if (MiscUtils.eq(args[1], "title")) {

					} else if (MiscUtils.eq(args[1], "locked")) {

					} else if (MiscUtils.eq(args[1], "invites")) {

					}
				}
			} else if (MiscUtils.eq(args[0], "create")) {

			} else if (MiscUtils.eq(args[0], "delete")) {

			} else if (MiscUtils.eq(args[0], "add")) {

			} else if (MiscUtils.eq(args[0], "remove")) {

			} else if (MiscUtils.eq(args[0], "invite")) {

			} else if (MiscUtils.eq(args[0], "kick")) {

			} else if (MiscUtils.eq(args[0], "home")) {

			} else if (MiscUtils.eq(args[0], "spy")) {

			} else if (MiscUtils.eq(args[0], "chat")) {

			}
		}
		return true;
	}

	// Shorthand method, just to shorten line lengths since I use it a lot above.
	private void ps(CommandSender s, String p, String m) { MiscUtils.permSend(s, p, m); }

	// Idental to the above method but auto prepends the command label
	private void ps(CommandSender s, String p, String l, String m) { MiscUtils.permSend(s, p, Clr.LIME+"/"+l+" "+m); }
}
