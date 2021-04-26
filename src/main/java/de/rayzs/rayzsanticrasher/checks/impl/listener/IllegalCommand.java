package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class IllegalCommand implements Listener {
	
	private RayzsAntiCrasher instance;
	
	public IllegalCommand() {
		instance = RayzsAntiCrasher.getInstance();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
			final Player player = event.getPlayer();
			final String message = event.getMessage();
			final String[] splittedMessage = message.split(" ")[0].split("/");
			if (player.hasPermission("rayzsanticrasher.bypass") || player.isOp())
				return;
			try {
				if(message.length() <= 1) return;
				final String command = splittedMessage[1];
				String problemChar = RayzsAntiCrasher.getAPI().hasOnlyLettersString(command);
				if (!problemChar.equals("empty") && !command.equalsIgnoreCase("?")) {
					player.sendMessage("§8[§9R§bA§9C§8] §7You§8'§7re not allowed to use this symbol§8!");
					player.sendMessage("§8[§9R§bA§9C§8] §7Your message§8: §e"
							+ message.replace(problemChar, "§c§o" + problemChar + "§e"));
					event.setCancelled(true);
					return;
				}
			}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}
}