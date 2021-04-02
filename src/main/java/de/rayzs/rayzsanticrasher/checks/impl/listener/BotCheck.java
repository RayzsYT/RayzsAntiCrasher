package de.rayzs.rayzsanticrasher.checks.impl.listener;

import java.util.HashMap;
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
	private Integer teleportDuration, checkDuration;
	private Boolean multyiplyPing, blockPlayer, canChatBeforeCheck, canExecuteBeforeCheck, broadcastReport;

	public BotCheck() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		teleportDuration = instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "teleportDuration")
				.getInt(50);
		checkDuration = instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "checkDuration")
				.getInt(80);
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
		multyiplyPing = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "multyiplyPing")
				.getString("true"));
		broadcastReport = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "broadcastReport")
				.getString("true"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		try {
			final Player player = event.getPlayer();
			final CraftPlayer craftPlayer = (CraftPlayer) player;
			final String clientAddress = player.getAddress().getAddress().getHostAddress();
			Integer ping = craftPlayer.getHandle().ping;
			if (ping < 1)
				ping = 1;
			HashMap<Player, Location> playerLocationHash = new HashMap<>();
			(new BukkitRunnable() {
				public void run() {
					player.teleport(new Location(player.getWorld(), player.getLocation().getX(),
							(player.getLocation().getY()) + (0.05), player.getLocation().getZ(),
							player.getLocation().getYaw(), player.getLocation().getPitch()));
					playerLocationHash.put(player, player.getLocation());
				}
			}).runTaskLater(instance, getDuraction(ping, teleportDuration));
			(new BukkitRunnable() {
				public void run() {
					if(player != null)
						if (player.getLocation().equals(playerLocationHash.get(player))) {
							api.kickPlayer(craftPlayer, instance.getBotKickMessage());
							if (broadcastReport)
								new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI()).send(
										"§8[§9R§bA§9C§8] §b" + player.getName() + "§7 got §cdetected §7playing as a bot§8!");
							if (blockPlayer)
								isIlleagel(clientAddress);
						}
				}
			}).runTaskLater(instance, getDuraction(ping, checkDuration));
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if (canChatBeforeCheck)
			return;
		try {
			final Player player = event.getPlayer();
			final String clientAddress = player.getAddress().getAddress().getHostAddress();
			if(api.getHandshakeAttack().isWhitelisted(clientAddress)) 
				return;
			if(api.getLoginAttack().isWhitelisted(clientAddress))
				return;
			if(api.getPingAttack().isWhitelisted(clientAddress))
				return;
			if(api.getPingStatusAttack().isWhitelisted(clientAddress))
				return;
			event.setCancelled(true);
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (canExecuteBeforeCheck)
			return;
		try {
			final Player player = event.getPlayer();
			final String clientAddress = player.getAddress().getAddress().getHostAddress();
			if(api.getHandshakeAttack().isWhitelisted(clientAddress)) 
				return;
			if(api.getLoginAttack().isWhitelisted(clientAddress))
				return;
			if(api.getPingAttack().isWhitelisted(clientAddress))
				return;
			if(api.getPingStatusAttack().isWhitelisted(clientAddress))
				return;
			event.setCancelled(true);
		}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
	}

	private Integer getDuraction(Integer ping, Integer duration) {
		if (multyiplyPing)
			return ping * duration;
		return duration;
	}

	private void isIlleagel(String clientAddress) {
		api.getHandshakeAttack().addBlacklist(clientAddress);
		api.getLoginAttack().addBlacklist(clientAddress);
		api.getPingAttack().addBlacklist(clientAddress);
		api.getPingStatusAttack().addBlacklist(clientAddress);
		RayzsAntiCrasher.getAPI().ipTable(clientAddress, true);
	}
}