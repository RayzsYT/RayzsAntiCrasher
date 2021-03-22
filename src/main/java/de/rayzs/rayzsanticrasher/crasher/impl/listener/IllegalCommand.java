package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalCommand implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission("rayzsanticrasher.bypass") || player.isOp())
			return;
		try {
		String message = event.getMessage().toLowerCase().split(" ")[0].split("/")[1];
		String onlyLetters = RayzsAntiCrasher.getAPI().hasOnlyLetters(message);
		if (!onlyLetters.equals("empty")) {
			player.sendMessage("§8[§9R§bA§9C§8] §7You§8'§7re not allowed to use this symbol§8!");
			player.sendMessage("§8[§9R§bA§9C§8] §7Your message§8: §e"
					+ event.getMessage().replace(onlyLetters, "§c§o" + onlyLetters + "§e"));
			event.setCancelled(true);
		}
		}catch (Exception error) { return; }
	}
}