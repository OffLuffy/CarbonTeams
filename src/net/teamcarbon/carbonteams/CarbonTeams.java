package net.teamcarbon.carbonteams;

import net.milkbowl.vault.chat.Chat;
import net.teamcarbon.carbonteams.commands.TeamsCommand;
import net.teamcarbon.carbonteams.listeners.ChatListener;
import net.teamcarbon.carbonteams.listeners.MiscListeners;
import net.teamcarbon.carbonteams.listeners.TeamListeners;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.teamcarbon.carbonlib.ConfigAccessor;
import net.teamcarbon.carbonlib.Log;
import net.teamcarbon.carbonlib.MiscUtils;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonTeams extends JavaPlugin {

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
		public FileConfiguration getConfig() { return ca.getConfig(); }

		/**
		 * Saves the FileConfiguration associated with this config type to the stored file path
		 */
		public void saveConfig() { ca.saveConfig(); }

		/**
		 * Reloads the FileConfiguration associated with this config type from the stored file path
		 */
		public void reloadConfig() { ca.reloadConfig(); }
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

	// inst variable can be used to get a reference to the main plugin class via
	// 'CarbonTeams.inst' from anywhere without passing the class into methods
	public static CarbonTeams inst;
	public static PluginManager pm;

	// I'll use this to make sure banners exist. Some methods refer to this
	// boolean later before executing to make sure it doesn't throw errors
	public static boolean bannersEnabled = Material.getMaterial("BANNER") != null;

	// Variables to hold Vault's service accessors
	public static Permission perms;
	public static Economy econ;
	public static Chat chat;

	// Some variables for holding other data used elsewhere
	private static HashMap<Player, ChatType> chatMode = new HashMap<Player, ChatType>();
	private static List<Player> spies = new ArrayList<Player>();
	private static List<Player> listeningAllies = new ArrayList<Player>();
	
	public void onEnable() {
		inst = this;
		pm = Bukkit.getPluginManager();

		// Init the default config
		saveDefaultConfig();
		reloadConfig();

		// Inits all the non-default configs
		for (ConfType ct : ConfType.values()) ct.initConfType();

		// Setup the custom log. Specify the debug logging path in config.yml
		new Log(this, "core.enable-debug-logging");

		// Setup all Vault hooks or disable the plugin if any fail
		if (!setupChat() || !setupPermissions() || !setupEconomy()) {
			Log.severe("Couldn't find Vault! Disabling " + getDescription().getName());
			pm.disablePlugin(this);
			return;
		}
		Log.debug("Hooked to Vault for permission and economy support");

		// Registers the chat listener event
		pm.registerEvents(new ChatListener(), this);
		pm.registerEvents(new TeamListeners(), this);
		pm.registerEvents(new MiscListeners(), this);

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

	/**
	 * @return Returns Vault's Chat object
	 */
	public static Chat chat() { return chat; }

	/**
	 * @return Returns Vault's Permission object
	 */
	public static Permission perm() { return perms; }

	/**
	 * @return Returns Vault's Economy object
	 */
	public static Economy econ() { return econ; }

	// Hook into Vault's chat service, store as global static variable
	private boolean setupChat() {
		RegisteredServiceProvider<Chat> cp = Bukkit.getServicesManager().getRegistration(Chat.class);
		if (cp != null) chat = cp.getProvider();
		return chat != null;
	}

	// Hook into Vault's permission service, store in MiscUtils and as global static variable
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (pp != null)
			perms = pp.getProvider();
		MiscUtils.setPerms(perms); // MiscUtils has a perm check. Store a perm object in here for checking later
		return perms != null;
	}

	// Hook into Vault's economy service, store as global static variable
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> ep = getServer().getServicesManager().getRegistration(Economy.class);
		if (ep != null)
			econ = ep.getProvider();
		return econ != null;
	}
}
