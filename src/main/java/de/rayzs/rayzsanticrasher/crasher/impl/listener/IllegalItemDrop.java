package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalItemDrop implements Listener {
	
	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer max;
	
	public IllegalItemDrop() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max").getInt(10000);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		Item item = event.getItemDrop();
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.getItemStack());
		if(!nmsItem.hasTag())
			return;
		if(nmsItem.getTag().toString().length() > max) {
			((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.close();
			item.remove();
			api.createCustomReport(player, this.getClass(), "Item has to many nbttags!");
		}
	}
}
