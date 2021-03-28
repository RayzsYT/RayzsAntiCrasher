package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalMovement implements Listener {
	
	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer distance;
	
	public IllegalMovement() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		distance = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "distance").getInt(5);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		try {
			Player player = event.getPlayer();
			Location fromLocation = event.getFrom();
			Location toLocation = event.getTo();
			if(fromLocation.distance(toLocation) > distance) {
				player.teleport(fromLocation);
				event.setCancelled(true);
				api.kickPlayer(player, "Too big distance");
				api.createCustomReport(player, this.getClass(), "Too big distance!");
			}
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}