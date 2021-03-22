package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ChunkOverflower implements Listener {
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location toLocation = event.getTo();
		Chunk chunk = toLocation.getChunk();
		if(!chunk.isLoaded() || chunk == null) {
			event.setCancelled(true);
		}
	}
}