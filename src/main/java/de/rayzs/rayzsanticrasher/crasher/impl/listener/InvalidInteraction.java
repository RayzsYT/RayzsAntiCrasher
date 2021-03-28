package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class InvalidInteraction implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer max;

	public InvalidInteraction() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max")
				.getInt(5000);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		try {
			final Player player = event.getPlayer();
			final org.bukkit.inventory.ItemStack item = event.getItem();
			if (item == null) 
				return;
			final net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
			if (!nmsItem.hasTag()) return;
			if (nmsItem.getTag().toString().length() > max) {
				event.getPlayer().getInventory().removeItem(item);
				api.kickPlayer(player, "Interacting with item with too many nbttags");
				api.createCustomReport(player, this.getClass(), "Trying to interact with a item with too many nbttags!");
			}
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}