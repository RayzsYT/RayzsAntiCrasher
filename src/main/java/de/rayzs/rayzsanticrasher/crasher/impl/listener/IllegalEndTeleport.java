package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalEndTeleport implements Listener {

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		TeleportCause teleportCause = event.getCause();
		Location toLocation = event.getTo();
		if (teleportCause != TeleportCause.END_PORTAL)
			return;
		event.setCancelled(true);
		Bukkit.getScheduler().runTaskLater(RayzsAntiCrasher.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (player != null) player.teleport(toLocation);
			}
		}, 15);
	}
}