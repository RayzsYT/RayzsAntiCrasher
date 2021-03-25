package de.rayzs.rayzsanticrasher.crasher.impl.listener;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class VPNCheck implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Boolean blockPlayer;

	public VPNCheck() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		blockPlayer = Boolean.parseBoolean(instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "blockPlayer")
				.getString("false"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final String clientAddress = player.getAddress().getAddress().getHostAddress();
		(new Thread(() -> {
				Boolean usingVPN = RayzsAntiCrasher.getAPI().isVPN(clientAddress);
				if (usingVPN) {
					try {
						api.disconnectChannel(
								((CraftPlayer) player).getHandle().playerConnection.networkManager.channel);
						new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI()).send(
								"§8[§9R§bA§9C§8] §b" + player.getName() + "§7 got §cdetected §7by using a VPN§8!");
					}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
					if (blockPlayer) isIlleagel(clientAddress);
					return;
				}
		})).start();
	}

	private void isIlleagel(String clientAddress) {
		api.getHandshakeAttack().addBlacklist(clientAddress);
		api.getLoginAttack().addBlacklist(clientAddress);
		api.getPingAttack().addBlacklist(clientAddress);
		api.getPingStatusAttack().addBlacklist(clientAddress);
		RayzsAntiCrasher.getAPI().ipTable(clientAddress, true);
	}
}