package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class BotJoin implements Listener {

	private RayzsAntiCrasher instance;
	
	public BotJoin() {
		instance = RayzsAntiCrasher.getInstance();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CraftPlayer craftPlayer = (CraftPlayer) player;
		Integer ping = craftPlayer.getHandle().ping;
		if(ping < 1)
			ping = 1;
		HashMap<Player, Location> playerLocationHash = new HashMap<>();
		player.teleport(
				new Location(player.getWorld(), player.getLocation().getX(), (player.getLocation().getY()) + (0.05),
						player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
		playerLocationHash.put(player, player.getLocation());
		(new BukkitRunnable() {
			public void run() {
				if (player.getLocation().equals(playerLocationHash.get(player))) {
					craftPlayer.getHandle().playerConnection.networkManager.channel.close();
					new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI())
							.send("§8[§9R§bA§9C§8] §b" + player.getName() + "§7 got §cdetected §7playing as a bot§8!");
				}
			}
		}).runTaskLater(instance, ping*10L);
	}
}