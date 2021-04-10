package de.rayzs.rayzsanticrasher.notify;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class Notify {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;

	public Notify(RayzsAntiCrasher instance, RayzsAntiCrasherAPI api) {
		this.instance = instance;
		this.api = api;
	}

	public void send(String text) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				Bukkit.getConsoleSender().sendMessage(text);
				for(Player players : Bukkit.getOnlinePlayers()) if (players.hasPermission("rayzsanticrasher.notify") && api.getNotify(players) == 1)
					players.sendMessage(text);
			}
		});
	}
}