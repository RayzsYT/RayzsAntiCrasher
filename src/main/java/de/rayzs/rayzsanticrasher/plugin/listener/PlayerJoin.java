package de.rayzs.rayzsanticrasher.plugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.checks.impl.server.IllegalConnection;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerJoin implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Integer addToWhitelistTime;

	public PlayerJoin() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		addToWhitelistTime = instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "addToWhitelistTime")
				.getInt(100);
		instance.registerEvent(this);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final String clientAddress = player.getAddress().getHostString().toString().split(":")[0];
		if (!api.existCrashPlayer(player))
			api.createCrashPlayer(player);
		if (instance.useMySQL())
			if (player.hasPermission("rayzsanticrasher.notify") && !api.existNotify(player))
				Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
					public void run() {
						Integer result = 1;
						try {
							result = instance.getNotifySQL().getInteger(player.getUniqueId(), "NOTIFY");
						} catch (Exception error) {
							if (instance.useMySQL())
								instance.getNotifySQL().set(player.getUniqueId(), "NOTIFY", result);
						}
						api.setNotify(player, result);
					}
				});
			else
				api.setNotify(player, 1);
		if (instance.useNotifyUpdate())
			if (player.hasPermission("rayzsanticrasher.admin"))
				if (!instance.hasValidVersion())
					Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
						@Override
						public void run() {
							TextComponent clickCompoment = new TextComponent("§b§l§nHERE§7");
							clickCompoment.setClickEvent(
									new ClickEvent(ClickEvent.Action.OPEN_URL, instance.getDownloadLink()));
							player.sendMessage(
									"§8[§4R§cA§4C§8] §7This server is still using an §coutdated §7version§8.");
							player.spigot().sendMessage(new TextComponent("§8[§4R§cA§4C§8] §7Click "), clickCompoment,
									new TextComponent(" §7to get the newest version§8."));
						}
					}, 20);

		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			@Override
			public void run() {
				if (player != null) {
					api.getServerAttack().addWhitelist(clientAddress);
					instance.getCheckFile().getYAML().set(
					"settings.server." + IllegalConnection
					.class.getSimpleName().toLowerCase() + ".whitelistedIPs",
					api.getServerAttack().getWhitelist());
				}
			}
		}, addToWhitelistTime);
	}
}