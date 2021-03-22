package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalEntitySpawn implements Listener {
	
	private RayzsAntiCrasher instance;
	private Integer max;
	
	public IllegalEntitySpawn() {
		instance = RayzsAntiCrasher.getInstance();
		max = instance.getCheckFile().search(
				"settings.listener." + this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "max").getInt(10000);
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if(!event.getEntityType().equals(EntityType.DROPPED_ITEM))
			return;
		Item item = (Item)event.getEntity();
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item.getItemStack());
		if(!nmsItem.hasTag())
			return;
		if(nmsItem.getTag().toString().length() > max) {
			event.setCancelled(true);
			item.remove();
			return;
		}
	}
}