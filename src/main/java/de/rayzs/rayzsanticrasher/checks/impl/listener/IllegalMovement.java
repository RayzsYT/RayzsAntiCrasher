package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalMovement implements Listener {
	
	private RayzsAntiCrasher instance;
	private Integer distance;
	
	public IllegalMovement() {
		instance = RayzsAntiCrasher.getInstance();
		distance = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "distance").getInt(5);
	}
	
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		try {
			final Player player = event.getPlayer();
			final Location fromLocation = event.getFrom();
			final Location toLocation = event.getTo();
			if(fromLocation.distance(toLocation) > distance)
				player.teleport(fromLocation);
			final Chunk chunk = toLocation.getChunk();
			if(!chunk.isLoaded() || !toLocation.getWorld().isChunkLoaded(chunk) || chunk == null) event.setCancelled(true);
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}