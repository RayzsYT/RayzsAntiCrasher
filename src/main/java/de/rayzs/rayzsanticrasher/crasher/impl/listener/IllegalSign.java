package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalSign implements Listener {

	private RayzsAntiCrasher instance;
	private Integer max;
	
	public IllegalSign() {
		instance = RayzsAntiCrasher.getInstance();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max").getInt(20);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		Boolean isInvalid = false;
		for (String line : event.getLines()) {
			int lineLength = line.length();
			if (lineLength > max) {
				isInvalid = true;
				break;
			}
		}
		if(isInvalid) {
			event.setCancelled(true);
			((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.close();
			RayzsAntiCrasher.getAPI().createCustomReport(player, this.getClass(), "Too big distance to sign!");
		}
	}
}