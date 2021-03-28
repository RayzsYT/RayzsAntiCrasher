package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalSign implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer max;
	
	public IllegalSign() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max").getInt(20);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		try {
			final Player player = event.getPlayer();
			Boolean isInvalid = false;
			for (String line : event.getLines()) {
				final int lineLength = line.length();
				if (lineLength > max) {
					isInvalid = true;
					break;
				}
			}
			if(isInvalid) {
				event.setCancelled(true);
				api.kickPlayer(player, "Too big distance to the sign");
				RayzsAntiCrasher.getAPI().createCustomReport(player, this.getClass(), "Too big distance to sign!");
			}
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}