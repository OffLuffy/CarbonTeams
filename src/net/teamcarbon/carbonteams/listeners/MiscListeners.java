package net.teamcarbon.carbonteams.listeners;

import net.teamcarbon.carbonlib.Misc.LocUtils;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonteams.CarbonTeams;
import net.teamcarbon.carbonteams.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonteams.utils.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This listener will handle the code for exp sharing and team activities
 */
public class MiscListeners implements Listener {
	// This handles when a user receives exp.
	@EventHandler
	public void expChange(PlayerExpChangeEvent e) {
		ConfigurationSection conf = CarbonTeams.inst.getConfig().getConfigurationSection("experience-sharing");
		Player p = e.getPlayer();
		// Player must have a team or we just ignore this event and let him collect exp normally
		if (Team.hasTeam(p)) {
			Team t = Team.getTeam(p);
			// This check also makes sure exp share is enabled in config and checks if per-team toggles are enabled
			if (t.isExpShareEnabled()) {
				List<Player> sharedWith = new ArrayList<>();
				double maxDist = conf.getDouble("share-radius", 50);
				boolean ignoreRadius = false;
				// If the max distance is impossible (negative), disable radius and share with all members or allies if enabled
				if (maxDist < 0) { ignoreRadius = true; }
				// Store the players available for exp sharing
				for (Player p2 : Bukkit.getOnlinePlayers())
					if (ignoreRadius || LocUtils.distance(p.getLocation(), p2.getLocation()) <= maxDist)
						if (t.isMember(p2) || (conf.getBoolean("share-with-allies", false) && t.isAlly(p2)))
							sharedWith.add(p2);
				// If there's no one to share exp with, just exit and allow normal exp collecting
				if (!sharedWith.isEmpty()) {
					boolean notifyShare = conf.getBoolean("notify-on-share", false);
					// If there's a share-bonus (additional overall exp if sharing), adjust it here
					if (conf.getInt("share-bonus", 20) > 0)
						e.setAmount(e.getAmount() + (e.getAmount() * (conf.getInt("share-bonus", 20) / 100)));
					int share = e.getAmount()/sharedWith.size();
					int collectorShare = share;
					// If the player who collected the exp gets a bonus, redistribute the exp
					if (conf.getInt("collect-bonus-percent",30) > 0) {
						// first, collector's share is adjusted with the bonus (share amount + bonus percent of share amount)
						collectorShare = share + (share * (conf.getInt("collect-bonus-percent")/100));
						// then we subtract that amount from the overall exp and split that between the other players
						share = (e.getAmount()-collectorShare)/sharedWith.size();
					}
					// TODO Call a custom exp share event so that this event can be modified later
					// TODO Allows the collector's exp and shared exp to be modified or cancel the event
					e.setAmount(collectorShare);
					HashMap<String, String> rep = new HashMap<>();
					rep.put("{COLLECTED}", collectorShare+"");
					rep.put("{COLLECTOR}", p.getName());
					rep.put("{SHARED}", share+"");
					rep.put("{SHARECOUNT}", sharedWith.size()+"");
					if (notifyShare)
						p.sendMessage(MiscUtils.massReplace(CustomMessage.COLLECTOR_EXP_SHARE.pre(), rep));
					for (Player p2 : sharedWith) {
						p2.setTotalExperience(p2.getTotalExperience() + share);
						if (notifyShare)
							p2.sendMessage(MiscUtils.massReplace(CustomMessage.SHARED_EXP_SHARE.pre(), rep));
					}
				}
			}
		}
	}
}
