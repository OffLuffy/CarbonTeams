package net.teamcarbon.carbonteams.utils;

import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.CarbonTeams.ConfType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import static net.teamcarbon.carbonlib.Misc.Messages.Clr;

/**
 * Convenience class. This allows me to fetch messages easily, as well as store them and provide default messages
 */
@SuppressWarnings("UnusedDeclaration")
public class CustomMessages {
	private static boolean init = false;
	public enum CustomMessage {
		// Generic messages
		PREFIX("prefix", "&6&l[" + CarbonTeams.inst.getDescription().getName() + "] &r"),
		RELOADED("reloaded", "&bReloaded"),
		NO_PERM("no-perm", "&cYou don't have permission to do that"),
		NOT_ONLINE("not-online", "&cYou must be in-game to use that"),
		GENERIC_ERROR("generic-error", "&cAn error occurred. Please report this to an admin!"),
		PLAYER_NOT_FOUND("player-not-found", "&cThat player couldn't be found"),

		// Team messages
		REMOVED_FROM_TEAM("removed-from-team", "&bYou've been removed from the team: &6{TEAMNAME}"),
		ADDED_TO_TEAM("added-to-team", "&bYou've been assigned to the team: &6{TEAMNAME}"),
		COLLECTOR_EXP_SHARE("collector-exp-share", "&bCollected {COLLECTED} exp, shared {SHARED} exp with {SHARECOUNT} others"),
		SHARED_EXP_SHARE("shared-exp-share", "&Gained {SHARED} exp, collected by {COLLECTOR}"),
		NO_CHAT_TEAM("no-chat-team", "&cYou don't have a team to chat with!"),
		ALL_CHAT_MODE("all-chat-mode", "&bYou're now talking in public chat"),
		ALL_CHAT_ALREADY("all-chat-already", "&bYou're already talking in public chat"),
		TEAM_CHAT_MODE("team-chat-mode", "&bYou're now talking in team chat"),
		TEAM_CHAT_ALREADY("team-chat-mode", "&bYou're already talking in team chat"),
		ALLY_CHAT_MODE("ally-chat-mode", "&bYou're now talking in ally chat"),
		ALLY_CHAT_ALREADY("ally-chat-mode", "&bYou're already talking in ally chat");

		private String msg, path, defMsg;
		CustomMessage(String path, String defMsg) { this.path = path; this.defMsg = defMsg; }
		public String getPath() { return path; }
		public String noPre() { if (!init) loadMessages(); return ChatColor.translateAlternateColorCodes('&', msg); }
		public String pre() { if (!init) loadMessages(); return ChatColor.translateAlternateColorCodes('&', PREFIX.msg + msg); }

		/**
		 * Sets the cached message for this enumarated object
		 * @param message The message to set this CustomMessage to
		 */
		public void setMessage(String message) { msg = ChatColor.translateAlternateColorCodes('&', message); }

		/**
		 * Attempts to load all CustomMessage objects from disk, filling in missing messages with default values
		 */
		public static void loadMessages() {
			FileConfiguration msgs = CarbonTeams.getConfig(ConfType.MESSAGES);
			for (CustomMessage cm : CustomMessage.values()) {
				cm.msg = msgs.getString(cm.path, cm.defMsg);
				msgs.set(cm.path, cm.msg);
			}
			CarbonTeams.saveConfig(ConfType.MESSAGES);
			init = true;
		}

		/**
		 * Prints a formatted, bold and orange, header to the specified CommandSender
		 * @param sender The CommandSender to send the message to
		 * @param header The header to print (is wrapped in brackets)
		 */
		public static void printHeader(CommandSender sender, String header) { sender.sendMessage(Clr.fromChars("6l") + "===[ "+header+" ]======"); }

		/**
		 * Custom toString() override which makes sure the messages are loaded before attempting to fetch the message
		 * @return Returns the string version of the message
		 */
		public String toString() { if (!init) loadMessages(); return msg; }
	}
}
