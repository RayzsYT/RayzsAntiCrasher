package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalEndTeleport implements Listener {

	private RayzsAntiCrasher instance;
	private Integer delay;
	
	public IllegalEndTeleport() {
		instance = RayzsAntiCrasher.getInstance();
		delay = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "delay").getInt(15);
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		TeleportCause teleportCause = event.getCause();
		Location toLocation = event.getTo();
		if (teleportCause != TeleportCause.END_PORTAL)
			return;
		event.setCancelled(true);
		try {
			Bukkit.getScheduler().runTaskLater(RayzsAntiCrasher.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (player != null) player.teleport(toLocation);
				}
			}, delay);
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}