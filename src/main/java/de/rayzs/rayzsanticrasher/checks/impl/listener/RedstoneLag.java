package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class RedstoneLag implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private Double riskTPS;

	public RedstoneLag() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		riskTPS = (Double) instance
				.getCheckFile().search("settings.listener."
						+ this.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + "riskTPS")
				.get(19.95);
	}

	@EventHandler
	public void onBlockRedstone(final BlockRedstoneEvent event) {
		final Double currentTPS = api.getServerTPS();
		if (currentTPS <= riskTPS)
			event.setNewCurrent(0);
	}

	@EventHandler
	public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
		final Double currentTPS = api.getServerTPS();
		if (currentTPS <= riskTPS)
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
		final Double currentTPS = api.getServerTPS();
		if (currentTPS <= riskTPS)
			event.setCancelled(true);
	}
}