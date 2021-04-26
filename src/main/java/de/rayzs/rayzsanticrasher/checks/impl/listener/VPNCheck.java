package de.rayzs.rayzsanticrasher.checks.impl.listener;

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
		if(clientAddress.equals("127.0.0.1"))
			return;
		(new Thread(() -> {
				Boolean usingVPN = RayzsAntiCrasher.getAPI().isVPN(clientAddress);
				if (usingVPN) {
					try {
						api.customKickPlayer(player, instance.getVPNKickMessage());
						new Notify(RayzsAntiCrasher.getInstance(), RayzsAntiCrasher.getAPI()).send(
								"§8[§9R§bA§9C§8] §b" + player.getName() + "§7 got §cdetected §7by using a VPN§8!");
					}catch (Exception error) { if(instance.useDebug()) error.printStackTrace(); }
					if (blockPlayer) isIlleagel(clientAddress);
					return;
				}
		})).start();
	}

	private void isIlleagel(String clientAddress) {
		api.getServerAttack().addBlacklist(clientAddress);
		RayzsAntiCrasher.getAPI().ipTable(clientAddress, true);
	}
}