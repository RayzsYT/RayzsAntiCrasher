package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class ChunkOverflower implements Listener {
	
	private RayzsAntiCrasher instance;
	
	public ChunkOverflower() {
		instance = RayzsAntiCrasher.getInstance();
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		try {
			Location toLocation = event.getTo();
			Chunk chunk = toLocation.getChunk();
			if(!chunk.isLoaded())
				event.setCancelled(true);
			if(!toLocation.getWorld().isChunkLoaded(chunk))
				event.setCancelled(true);
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}