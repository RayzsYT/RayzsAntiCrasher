package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class BotJoin implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer teleportDuration;
	private Integer checkDuration;
	private Boolean multyiplyPing, blockPlayer;

	public BotJoin() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		multyiplyPing = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "multyiplyPing")
				.getString("false"));
		teleportDuration = instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "teleportDuration")
				.getInt(1);
		checkDuration = instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "checkDuration")
				.getInt(20);
		blockPlayer = Boolean.parseBoolean(instance.getCheckFile().search("settings.listener."
				+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "blockPlayer")
		.getString("false"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CraftPlayer craftPlayer = (CraftPlayer) player;
		String clientAddress = player.getAddress().getAddress().getHostAddress();
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
				if (player.getLocation().equals(playerLocationHash.get(player))) {
					craftPlayer.getHandle().playerConnection.networkManager.channel.close();
					new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI())
							.send("§8[§9R§bA§9C§8] §b" + player.getName() + "§7 got §cdetected §7playing as a bot§8!");
					if(blockPlayer) isIlleagel(clientAddress);
				}
			}
		}).runTaskLater(instance, getDuraction(ping, checkDuration));
	}

	private Integer getDuraction(Integer ping, Integer duration) {
		if (multyiplyPing) {
			return ping * duration;
		}
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