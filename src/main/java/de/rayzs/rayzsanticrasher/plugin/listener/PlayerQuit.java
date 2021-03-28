package de.rayzs.rayzsanticrasher.plugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class PlayerQuit implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;

	public PlayerQuit() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		instance.registerEvent(this);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			
			@Override
			public void run() {
				if (api.existCrashPlayer(player))
					api.deleteCrashPlayer(player);
			}
		}, 1);
	}
}