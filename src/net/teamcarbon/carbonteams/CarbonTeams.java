package net.teamcarbon.carbonteams;

import net.teamcarbon.carbonlib.CarbonPlugin;
import net.teamcarbon.carbonteams.commands.TeamsCommand;
import net.teamcarbon.carbonteams.listeners.ChatListener;
import net.teamcarbon.carbonteams.listeners.MiscListeners;
import net.teamcarbon.carbonteams.listeners.TeamListeners;
import net.teamcarbon.carbonlib.Misc.ConfigAccessor;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonTeams extends CarbonPlugin {

	/**
	 * Config types, used to easily access, save, and reload non-default configs
	 */
	public enum ConfType {
		/**
		 * The teams file, stores data for each team
		 */
		TEAMS("teams.yml"),
		/**
		 * Stores data that won't need to be altered by hand much at all
		 */
		DATA("data.yml"),
		/**
		 * Stores messages that are sent to players
		 */
		MESSAGES("messages.yml");
		private String fn;
		private ConfigAccessor ca;
		ConfType(String fileName) { fn = fileName; }

		/**
		 * Initializes this configuration file
		 */
		public void initConfType() { ca = new ConfigAccessor(CarbonTeams.inst, fn); }

		/**
		 * Returns the config object stored in this ConfType
		 * @return Returns a FileConfiguration object associated with this config type
		 */
		public FileConfiguration getConfig() { return ca.config(); }

		/**
		 * Saves the FileConfiguration associated with this config type to the stored file path
		 */
		public void saveConfig() { ca.save(); }

		/**
		 * Reloads the FileConfiguration associated with this config type from the stored file path
		 */
		public void reloadConfig() { ca.reload(); }
	}

	/**
	 * Chat modes, stored per player to determine how their chat is handled
	 */
	public enum ChatType {
		/**
		 * Indicates the player is talking in normal chat
		 */
		NORMAL,
		/**
		 * Indicates the player is talking only to online team members
		 */
		TEAM,
		/**
		 * Indicates the player is talking to all online members of their team or ally teams
		 */
		ALLY
	}

	// I'll use this to make sure banners exist. Some methods refer to this
	// boolean later before executing to make sure it doesn't throw errors
	public static boolean bannersEnabled = Material.getMaterial("BANNER") != null;

	// Some variables for holding other data used elsewhere
	private static HashMap<Player, ChatType> chatMode = new HashMap<>();
	private static List<Player> spies = new ArrayList<>();
	private static List<Player> listeningAllies = new ArrayList<>();
	
	public void enablePlugin() {

		// Inits all the non-default configs
		for (ConfType ct : ConfType.values()) ct.initConfType();

		// Registers the chat listener event
		pm().registerEvents(new ChatListener(), this);
		pm().registerEvents(new TeamListeners(), this);
		pm().registerEvents(new MiscListeners(), this);

		// Registers commands
		getServer().getPluginCommand("teams").setExecutor(new TeamsCommand());

		// Initialize Teams data
		Team.init();
	}

	/**
	 * Fetches the requested ConfType
	 * @param ct The ConfType to fetch
	 * @return Returns a FileConfiguration
	 */
	public static FileConfiguration getConfig(ConfType ct) { return ct.getConfig(); }

	/**
	 * Saves the specified ConfType
	 * @param ct The ConfType to save
	 */
	public static void saveConfig(ConfType ct) { ct.saveConfig(); }

	/**
	 * Saves all the config files (including default config.yml)
	 */
	public static void saveAllConfigs() { inst.saveConfig(); for (ConfType ct : ConfType.values()) ct.saveConfig(); }

	/**
	 * Reloads the specified ConfType from disk
	 * @param ct The ConfType to reload
	 */
	public static void reloadConfig(ConfType ct) { ct.reloadConfig(); }

	/**
	 * Reloads all the configs from disk (including default config.yml)
	 */
	public static void reloadAllConfigs() {
		inst.reloadConfig();
		for (ConfType ct : ConfType.values())
			ct.reloadConfig();
	}

	/**
	 * Sets the specified Player's ChatType
	 * @param p The Player to modify
	 * @param c The ChatType to set it to
	 */
	public static void setChatMode(Player p, ChatType c) { if (p.isOnline()) chatMode.put(p, c); }

	/**
	 * Fetches the Player's ChatType
	 * @param p The Player to check
	 * @return Returns the ChatType of the specified player
	 */
	public static ChatType getChatMode(Player p) {
		if (chatMode.containsKey(p)) {
			return chatMode.get(p);
		} else {
			setChatMode(p, ChatType.NORMAL);
			return ChatType.NORMAL;
		}
	}

	/**
	 * Set whether the Player is spying or not
	 * @param p The Player to set
	 * @param spying Whether or not the Player is spying
	 */
	public static void setSpying(Player p, boolean spying) {
		if (!spying && spies.contains(p)) spies.remove(p);
		else if (spying && !spies.contains(p)) spies.add(p);
	}

	/**
	 * Toggles the Player's spying state
	 * @param p The Player to toggle
	 */
	public static void toggleSpying(Player p) { setSpying(p, !isSpying(p)); }

	/**
	 * Indicates if the Player is spying
	 * @param p The Player to check
	 * @return Returns true if the Player is spying, false otherwise
	 */
	public static boolean isSpying(Player p) { return spies.contains(p); }

	/**
	 * Sets whether the Player is listening to their ally chat or not
	 * @param p The Player to set
	 * @param listening Whether or not the Player is listening
	 */
	public static void setListeningToAllies(Player p, boolean listening) {
		if (!listening && listeningAllies.contains(p)) listeningAllies.remove(p);
		else if (listening && !listeningAllies.contains(p)) listeningAllies.add(p);
	}

	/**
	 * Toggles whether the Player is listening to their ally chat or not
	 * @param p The Player to toggle
	 */
	public static void toggleListeningToAllies(Player p) { setListeningToAllies(p, !isListeningToAllies(p));}

	/**
	 * Indicates if the Player is listening to their ally chat
	 * @param p The Player to check
	 * @return Returns true if the Player is listening to their ally chat, false otherwise
	 */
	public static boolean isListeningToAllies(Player p) { return listeningAllies.contains(p); }
}
