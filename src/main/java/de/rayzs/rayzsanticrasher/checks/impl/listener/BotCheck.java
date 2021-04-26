package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class BotCheck implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Boolean blockPlayer, canChatBeforeCheck, canExecuteBeforeCheck, broadcastReport;

	public BotCheck() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		blockPlayer = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "blockPlayer")
				.getString("false"));
		canChatBeforeCheck = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "canChatBeforeCheck")
				.getString("false"));
		canExecuteBeforeCheck = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "canExecuteBeforeCheck")
				.getString("false"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		try {
			final Player player = event.getPlayer();
			final String playerName = player.getName();
			final CraftPlayer craftPlayer = (CraftPlayer) player;
			final String clientAddress = player.getAddress().getAddress().getHostAddress();

			Integer ping = craftPlayer.getHandle().ping;
			if (ping < 1)
				ping = 1;
			if (player.isFlying()) {

			}
			final Location oldLocation = player.getLocation();
			player.teleport(oldLocation.add(0, 1, 0));

			new BukkitRunnable() {
				@Override
				public void run() {
					if (player.isOnline()) {
						final Location newLocation = player.getLocation();
						if (newLocation.distance(oldLocation) < 1) {
							if(api.getServerAttack().isWhitelisted(clientAddress)) api.getServerAttack().removeWhitelist(clientAddress);
							api.customKickPlayer(craftPlayer, instance.getBotKickMessage());
							if(playerName != null)
							if (broadcastReport) new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI())
								.send("§8[§9R§bA§9C§8] §b" + playerName + "§7 got §cdetected §7playing as a bot§8!");
							if (blockPlayer) isIlleagel(clientAddress);
						}
					}
				}
			}.runTaskLater(instance, 50L);

		} catch (Exception error) {
			if (instance.useDebug())
				error.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if (canChatBeforeCheck)
			return;
		try {
			final Player player = event.getPlayer();
			final String clientAddress = player.getAddress().getAddress().getHostAddress();
			if (api.getServerAttack().isWhitelisted(clientAddress))
				return;
			event.setCancelled(true);
		} catch (Exception error) {
			if (instance.useDebug())
				error.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (canExecuteBeforeCheck)
			return;
		try {
			final Player player = event.getPlayer();
			final String clientAddress = player.getAddress().getAddress().getHostAddress();
			if (api.getServerAttack().isWhitelisted(clientAddress))
				return;
			event.setCancelled(true);
		} catch (Exception error) {
			if (instance.useDebug())
				error.printStackTrace();
		}
	}

	private void isIlleagel(String clientAddress) {
		api.getServerAttack().addBlacklist(clientAddress);
		RayzsAntiCrasher.getAPI().ipTable(clientAddress, true);
	}
}