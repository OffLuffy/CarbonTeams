package net.teamcarbon.carbonteams.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.teamcarbon.carbonlib.LocUtils;
import net.teamcarbon.carbonlib.Log;
import net.teamcarbon.carbonlib.MiscUtils;
import net.teamcarbon.carbonteams.CarbonTeams.ConfType;
import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

@SuppressWarnings("UnusedDeclaration")
public class Team {

	// Store all loaded Teams here for easier management
	private static List<Team> teams;

	private String name, prefix, postfix, teamsPath, greet, notice;
	private OfflinePlayer owner;
	private List<OfflinePlayer> members;
	private List<Team> allies;
	private Location home;

	/**
	 * Constructor for a new Team or when loading Teams. Default data will be applied if it doesn't exist in teams.yml
	 * @param name The name of the team
	 */
	public Team(String name) {
		this.name = name;
		teamsPath = "teams." + name;
		loadTeamDataFromFile();
		teams.add(this); // Adds this Team to the caches teams list
	}

	// Getters will simply fetch data or copies of data.
	// This methods can't be used to alter the Team's data
	/* ************************************* *
	 * **            GETTERS              ** *
	 * ************************************* */

	/**
	 * Fetches the Team's name
	 * @return Returns a String of the Team's name
	 */
	public String getName() { return this.name; }

	/**
	 * Fetches the Team's pretfix
	 * @return Returns a String of the Team's pretfix with the pattern and color codes applies
	 */
	public String getPrefix() { return this.prefix; }

	/**
	 * Fetches the Team's postfix
	 * @return Returns a String of the Team's postfix with the pattern and color codes applies
	 */
	public String getPostfix() { return this.postfix; }

	/**
	 * Fetches the Location of this Team's home
	 * @return Returns a Location of the Team's home
	 */
	public Location getHome() { return home; }

	/**
	 * Fetches this Team's greeting
	 * @return Returns the String greeting
	 */
	public String getGreeting() { return greet; }

	/**
	 * Fetches this Team's notice
	 * @return Returns the String notice
	 */
	public String getNotice() { return notice; }

	/**
	 * Checks if the specified OfflinePlayer matches an OfflinePlayer in the members list
	 * @param p The OfflinePlayer to check
	 * @return Returns true if the player is a member, false otherwise
	 */
	public boolean isMember(OfflinePlayer p) { return members.contains(p); }

	/**
	 * Fetches a copy of the cached members list. To refresh this list, call loadMembers()
	 * @return Returns a List&gt;UUID&lt; of members in this Team
	 */
	public List<OfflinePlayer> getMembers() { return new ArrayList<OfflinePlayer>(members); }

	/**
	 * Indicates whether the specified Team is an ally of the current Team
	 * @param t The Team to check
	 * @return Returns true if the specified Team is an ally or this team
	 */
	public boolean isAlly(Team t) { return t.equals(this) || allies.contains(t); }

	/**
	 * Indicates whether the specified Team is an ally of the current Team
	 * @param t The Team to check
	 * @return Returns true if the specified Team is an ally or this team
	 */
	public boolean isAlly(String t) { return Team.getTeam(t) != null && isAlly(Team.getTeam(t)); }

	/**
	 * Indicates whether the specified OfflinePlayer's Team is an ally of the current Team
	 * @param p The OfflinePlayer to check
	 * @return Returns true if the specified OfflinePlayer's Team is an ally or this team
	 */
	public boolean isAlly(OfflinePlayer p) { return Team.getTeam(p) != null && isAlly(Team.getTeam(p)); }

	/**
	 * Fetches a list of friendly Team names
	 * @return The String List of Team names listed as allies
	 */
	public List<Team> getAllies() { return new ArrayList<Team>(allies); }

	/**
	 * Fetches a list of all members in all ally Teams
	 * @return Returns a List of OfflinePlayers whom belong to allied Teams
	 */
	public List<OfflinePlayer> getAllyMembers() {
		List<OfflinePlayer> allyMembers = new ArrayList<OfflinePlayer>();
		for (Team t : allies) allyMembers.addAll(t.getMembers());
		return allyMembers;
	}

	// Mutator methods are used to modify a Team's data
	// and makes sure the data provided is valid
	/* ************************************* *
	 * **            MUTATOR              ** *
	 * ************************************* */

	/**
	 * Sets the team's prefix, using the pattern specified in config.yml
	 * @param prefix The text to insert into the prefix pattern
	 * @return Returns true if the prefix was set, false if prefix is invalid or disabled
	 */
	public boolean setPrefix(String prefix) {
		FileConfiguration conf = CarbonTeams.inst.getConfig();
		// Exit here if prefixes are disabled, return false
		if (!conf.getBoolean("prefix.enabled", true)) return false;
		// Store the min and max lengths for prefixes allowed
		int pfMin = conf.getInt("prefix.min-length", 1), pfMax = conf.getInt("prefix.max-length", 5);
		// Compile the pattern used to make sure the prefix is valid
		String pattern = (!conf.getBoolean("prefix.alpha-num-only", true)?".{":"[a-zA-Z0-9]{") + pfMin + "," + pfMax + ")";
		// If the pattern is valid, set it and return true, otherwise return false
		if (prefix.matches(pattern)) {
			this.prefix = trans(vars(CarbonTeams.inst.getConfig().getString("prefix.pattern"), null));
			return true;
		} else { return false; }
	}

	/**
	 * Sets the team's postfix, using the pattern specified in config.yml
	 * @param postfix The text to insert into the postfix pattern
	 * @return Returns true if the postfix was set, false if postfix is invalid or disabled
	 */
	public boolean setPostfix(String postfix) { // Essentially the same as setPrefix() above
		FileConfiguration conf = CarbonTeams.inst.getConfig();
		if (!conf.getBoolean("postfix.enabled", true)) return false;
		int pfMin = conf.getInt("postfix.min-length", 1), pfMax = conf.getInt("postfix.max-length", 5);
		String pattern = (!conf.getBoolean("postfix.alpha-num-only", true)?".{":"[a-zA-Z0-9]{") + pfMin + "," + pfMax + ")";
		if (postfix.matches(pattern)) {
			this.postfix = trans(vars(CarbonTeams.inst.getConfig().getString("postfix.pattern"), null));
			return true;
		} else { return false; }
	}

	/**
	 * Sets the Team's home Location. This does not adjust for a safe location
	 * @param home The Location of the Team's home
	 */
	public void setHome(Location home) {
		this.home = home;
		CarbonTeams.inst.getConfig(ConfType.TEAMS).set(getName() + ".home", LocUtils.toStr(home, false));
		CarbonTeams.inst.saveConfig(ConfType.TEAMS);
	}

	public void setGreeting(String greeting) {
		this.greet = trans(greeting); // TODO Replace stuff
	}

	public void setNotice(String notice) {
		this.notice = trans(notice); // TODO Replace stuff
	}

	public boolean setBanner(ItemStack banner) { // TODO Finish
		ConfigurationSection sect = CarbonTeams.inst.getConfig(ConfType.TEAMS).getConfigurationSection("banner-settings");
		banner = new ItemStack(banner);
		if (CarbonTeams.bannersEnabled) {
			if (banner.getType().equals(Material.getMaterial("BANNER"))) {
				banner.setAmount(1);
				BannerMeta bm = (BannerMeta)banner.getItemMeta();
				String bannerName = trans(vars(sect.getString("banner-name"), null));
				List<String> bannerLore = new ArrayList<String>();
				for (String s : sect.getStringList("banner-lore"))
					bannerLore.add(trans(vars(s, null)));
				if (bannerName.length() > 0) bm.setDisplayName(bannerName);
				if (bannerLore.size() > 0) bm.setLore(bannerLore);
				return true;
			} else { Log.warn("Banner specified for team: " + getName() + " in teams.yml isn't a banner!"); }
		} else { Log.debug("The server doesn't seem to support banners yet! Skipping setting the team's banner"); }
		return false;
	}

	/**
	 * Reloads this Team's data from file, using the mutators to set the data so if the file is edited by
	 * hand, some values may fail to set if they don't adhere to the mutator limits
	 */
	public void loadTeamDataFromFile() {
		Log.debug("Loading data for Team: " + getName());
		ConfigurationSection sect = CarbonTeams.inst.getConfig(ConfType.TEAMS).getConfigurationSection(teamsPath);
		setHome(LocUtils.fromStr(sect.getString(teamsPath + ".home")));
		setPrefix(sect.getString(teamsPath + ".prefix"));
		setPostfix(sect.getString(teamsPath + ".postfix"));
		setGreeting(sect.getString(teamsPath + ".greeting"));
		setNotice(sect.getString(teamsPath + ".notice"));
		setBanner(sect.getItemStack(teamsPath + ".banner"));

		// Load members
		if (members == null) members = new ArrayList<OfflinePlayer>();
		if (!members.isEmpty()) members.clear();
		boolean needsSave = false;
		FileConfiguration teams = CarbonTeams.inst.getConfig(ConfType.TEAMS);
		// Iterates over a copy of the list in case it needs to remove a UUID during iteration
		// This will log a warning noting the invalid UUID then remove it to prevent spamming the warning
		for (String s : new ArrayList<String>(teams.getStringList("teams." + getName() + ".members"))) {
			try {
				members.add(Bukkit.getOfflinePlayer(UUID.fromString(s)));
			} catch (Exception e) {
				Log.warn("Invalid UUID in teams.yml in members of team: " + getName() + ", removing it...");
				// Remove the invalid UUID from the actual stored data, not the copy we're iterating over
				MiscUtils.removeFromStringList(teams, "teams." + getName() + ".members", s);
				needsSave = true;
			}
		}
		// If values have been removed from the member list, resave the file
		if (needsSave) { CarbonTeams.inst.saveConfig(ConfType.TEAMS); }
	}
	
	public void addMember(Player p) {
		if (p == null) return;
		String id = p.getUniqueId().toString();
		FileConfiguration teams = CarbonTeams.inst.getConfig(ConfType.TEAMS);
		if (MiscUtils.addToStringList(teams, teamsPath + ".members", id))
			p.sendMessage(vars(CustomMessage.ADDED_TO_TEAM.pre(), null));
		CarbonTeams.inst.saveConfig(ConfType.TEAMS);
	}

	/**
	 * Attempts to remove the specified player from this Team and sends them a message of being removed
	 * @param p The Player to remove
	 */
	public void removeMember(Player p) {
		if (p == null) return;
		String id = p.getUniqueId().toString();
		FileConfiguration teams = CarbonTeams.inst.getConfig(ConfType.TEAMS);
		if (MiscUtils.removeFromStringList(teams, teamsPath + ".members", id))
			p.sendMessage(vars(CustomMessage.REMOVED_FROM_TEAM.pre(), null));
		CarbonTeams.inst.saveConfig(ConfType.TEAMS);
	}

	/**
	 * Attempts to remove the specified player from this Team and sends them a message of being removed if online
	 * @param p The OfflinePlayer to remove
	 */
	public void removeMember(OfflinePlayer p) {
		if (p == null) return;
		String id = p.getUniqueId().toString();
		FileConfiguration teams = CarbonTeams.inst.getConfig(ConfType.TEAMS);
		if (MiscUtils.removeFromStringList(teams, "teams." + getName() + ".members", id) && p.isOnline())
			((Player)p).sendMessage(CustomMessage.REMOVED_FROM_TEAM.pre());
		CarbonTeams.inst.saveConfig(ConfType.TEAMS);
	}

	/**
	 * Adds an ally Team
	 * @param t The Team to add as an ally
	 */
	public void addAlly(Team t) { if (!allies.contains(t)) allies.add(t); }

	/**
	 * Removes an ally Team
	 * @param t The Team to remove as an ally
	 */
	public void removeAlly(Team t) { if (allies.contains(t)) allies.remove(t); }

	// Other methods that don't set or fetch data
	/* ************************************* *
	 * **              MISC               ** *
	 * ************************************* */

	/**
	 * Sends a message to all online Team members
	 * @param p The Player who sent the message
	 * @param message The message to send
	 */
	public void sendTeamMessage(Player p, String message) {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (isMember(pl)) {
				// TODO Sent message to team members and spies
			}
		}
	}

	public void sendAllyMessage(Player p, String message) {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (isMember(pl)) {// TODO Check if ally team member
				// TODO Sent message to team members, allies, and spies
			}
		}
	}

	// Just a shorter way to parse color codes, Bukkit's is a bit lengthy
	private String trans(String m) { return ChatColor.translateAlternateColorCodes('&', m); }

	/**
	 * Parses the variables: {TEAMNAME}, {PREFIX}, {POSTFIX}, {LEADER}, {GREETING}, and {NOTICE}
	 * @param s The String to translate the variables in
	 * @param additional Additional text to replace and what to replace it with
	 * @return Returns the String with the parsed variables
	 */
	public String vars(String s, HashMap<String, String> additional) {
		HashMap<String, String> replace = new HashMap<String, String>();
		replace.put("{TEAMNAME}", getName());
		replace.put("{PREFIX}", getPrefix());
		replace.put("{POSTFIX}", getPostfix());
		replace.put("{LEADER}", owner.getName());
		replace.put("{GREETING}", getGreeting());
		replace.put("{NOTICE}", getNotice());
		if (additional != null && !additional.isEmpty()) s = MiscUtils.massReplace(s, additional);
		return MiscUtils.massReplace(s, replace);
	}

	// Static methods are accessed through the Team class rather than an instance of a Team
	// i.e. Team.staticMethod() rather than blueTeam.nonStaticMethod()
	// Generally to modify all Team instances or static data members of the Team class
	/* ************************************* *
	 * **             STATIC              ** *
	 * ************************************* */

	/**
	 * Static version of Team's vars() method, parses team-specific variables
	 * @param s The String to translate the variables in
	 * @param t The Team to use when parsing variables
	 * @param additional Additional text to replace and what to replace it with
	 * @return Returns the String with the parsed variables
	 * @see Team#vars
	 */
	public static String vars(String s, Team t, HashMap<String, String> additional) { return t.vars(s, additional); }

	/**
	 * Fetches a Team that matches the specified name
	 * @param name The name of the Team to search for
	 * @return Returns the Team if found, null otherwise
	 */
	public static Team getTeam(String name) { for (Team t : teams) if (MiscUtils.eq(t.getName(), name)) return t; return null; }

	/**
	 * Fetches the Team the specified OfflinePlayer is in
	 * @param p The OfflinePlayer to check for
	 * @return Returns a Team if the OfflinePlayer is on one, null otherwise
	 */
	public static Team getTeam(OfflinePlayer p) { for (Team t : teams) if (t.getMembers().contains(p)) return t; return null; }

	/**
	 * Checks if the speicifed OfflinePlayer is in a Team
	 * @param p The OfflinePlayer to check for
	 * @return Returns true if the OfflinePlayer is in a Team, false otherwise
	 */
	public static boolean hasTeam(OfflinePlayer p) { for (Team t : teams) if (t.getMembers().contains(p)) return true; return false; }

	/**
	 * Load Teams data.
	 */
	public static void init() {
		ConfigurationSection teams = CarbonTeams.inst.getConfig(ConfType.TEAMS).getConfigurationSection("teams");
		for (String key : teams.getKeys(false)) { new Team(key); } // Data loading is done in constructor
		// Now that we've instantiated all Teams, we can determine which Teams are allies of what
		loadAllies();
	}

	/**
	 * Goes over all loaded members and loads allies. This has to be done
	 * after initial team loading to ensure Team objects are instansiated
	 * before trying to add it as an Ally of another Team
	 */
	public static void loadAllies() {
		for (Team t : teams) { // Iterates over internally cached list of Teams

		}
	}

	// Overriden methods (methods in parent classes I need to alter)
	// I override equality checking methods of Object class to specify what attribues to
	// check when comparing if two Team objects are equal. (Object.equals(Object obj))
	// I use EqualsBuilder and HashCodeBuilder from Apache Commons, they're packaged into Bukkit already
	/* ************************************* *
	 * **            OVERRIDES            ** *
	 * ************************************* */

	@Override
	public boolean equals(Object obj) {
		if (obj == null ) return false; // Always return false if checking a null, this is standard
		if (obj == this) return true; // Exactly the same object (same memory address), must be equal
		if (!(obj instanceof Team)) return false; // If it's not an instance of a Team object, it can't be equal

		// Now that we know that 'obj' is a Teams object, we can cast it and continue to check equality
		Team t = (Team)obj;

		// Now to use an EqualsBuilder object to compare data members of this object and what we're comparing it to
		// We could do this manually with a series of .equals for each data member, but this is shorter
		return new EqualsBuilder()
				.append(getName(), t.getName()) // Add the name data of both objects
				.isEquals();
	}

	// hashCode() is used in conjuction with equals(), it's good practice to override it
	// as well, but be sure HashCodeBuilder is given the same data members as the EqualsBuilder
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getName())
				.toHashCode();
	}
 }
