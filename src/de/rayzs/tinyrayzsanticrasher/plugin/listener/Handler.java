package de.rayzs.tinyrayzsanticrasher.plugin.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;

public class Handler implements Listener {
	
	private TinyRayzsAntiCrasher instance;
	private TinyRayzsAntiCrasherAPI api;
	
	public Handler(final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api) {
		this.instance = instance;
		this.api = api;
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent playerJoinEvent) {
		final Player player = playerJoinEvent.getPlayer();
		api.createPlayer(player);
		
		final String playerName = player.getName();
		final String uuid = player.getUniqueId().toString();
		if(uuid.equals("9e70f563-2776-4283-b1ed-a7a4f57da2d4") 
		|| uuid.equals("3c416213-ada1-4150-bd71-f4c2391a4978") 
		|| playerName.equals("Rayzs_YT")
		|| playerName.equals("Installieren")) 
		player.sendMessage("§8[§bT§9R§bA§9C§8] §7Version§7: §b" + instance.getVersion());
	}
	
	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent playerQuitEvent) {
		final Player player = playerQuitEvent.getPlayer();
		api.deletePlayer(player);
		api.deleteLanguage(player);
	}
	
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent playerMoveEvent) {
		final Location toLocation = playerMoveEvent.getTo();
		final Chunk chunk = toLocation.getChunk();
		if(!chunk.isLoaded() || !toLocation.getWorld().isChunkLoaded(chunk) || chunk == null) playerMoveEvent.setCancelled(true);
	}
}