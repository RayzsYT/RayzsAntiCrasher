package de.rayzs.rayzsanticrasher.addon;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public abstract class RayzsAntiCrasherAddon {
	
	public abstract void onEnable();
	public abstract void onDisable();
	
	public RayzsAntiCrasherAPI getAPI() {
		return RayzsAntiCrasher.getAPI();
	}

	public RayzsAntiCrasher getInstance() {
		return RayzsAntiCrasher.getInstance();
	}
}