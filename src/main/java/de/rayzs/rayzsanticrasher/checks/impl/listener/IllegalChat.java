package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalChat implements Listener {

	private RayzsAntiCrasherAPI api;

	public IllegalChat() {
		api = RayzsAntiCrasher.getAPI();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (player.hasPermission("rayzsanticrasher.bypass") || player.isOp()) return;
		final String message = event.getMessage();
		try {
			final String problemChar = api.hasOnlyLettersString(message);
			if(problemChar.equals("empty")) return;
			player.sendMessage("§8[§9R§bA§9C§8] §7You§8'§7re not allowed to use this symbol§8!");
			player.sendMessage("§8[§9R§bA§9C§8] §7Your message§8: §e" + message.replace(problemChar, "§c§o" + problemChar + "§e"));
			event.setCancelled(true);
			return;
		}catch (Exception exception) { }
	}
}