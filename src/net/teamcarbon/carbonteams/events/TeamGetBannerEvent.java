package net.teamcarbon.carbonteams.events;

import org.bukkit.event.Cancellable;

public class TeamGetBannerEvent implements Cancellable {
	boolean cancelled;
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean b) { cancelled = b; }
}
