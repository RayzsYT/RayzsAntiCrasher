package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalBlockPlace implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer max;
	
	public IllegalBlockPlace() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max").getInt(5000);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		org.bukkit.inventory.ItemStack item = event.getItemInHand();
		if (item == null)
			return;
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		if (!nmsItem.hasTag())
			return;
		if (nmsItem.getTag().toString().length() > max) {
			event.getPlayer().getInventory().removeItem(item);
			event.setBuild(false);
			((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.close();
			api.createCustomReport(player, this.getClass(), "Trying to place invalid block!");
		}
	}
}