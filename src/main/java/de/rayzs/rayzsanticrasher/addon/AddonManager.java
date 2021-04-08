package de.rayzs.rayzsanticrasher.addon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class AddonManager {

	private RayzsAntiCrasher instance;
	@SuppressWarnings("unused")
	private RayzsAntiCrasherAPI api;
	private File addonsFolder;
	private List<Addon> addonList;

	public AddonManager(RayzsAntiCrasher instance, RayzsAntiCrasherAPI api, File addonFolder) {
		this.instance = instance;
		this.api = api;
		this.addonsFolder = addonFolder;
		addonList = new ArrayList<>();
	}

	public void loadAddons() {
		instance.logger("§8[§4R§cA§4C§8] §7Registering §6addons§8...");
		(new Thread(() -> {
			String fileName;
			for (File currentFile : addonsFolder.listFiles()) {
				if (currentFile == null)
					continue;
				fileName = currentFile.getName();
				if (fileName.endsWith(".jar")) {
					Addon addon = new Addon(instance, currentFile, fileName.split(".jar")[0]);
					Boolean addonWorking = addon.enable();
					if (!addonWorking)
						continue;
					addAddon(addon);
				}
			}
			instance.logger("§8[§4R§cA§4C§8] §7Done! All addons are now loaded§8!");
		})).start();
	}

	public void unloadAddons() {
		instance.logger("§8[§4R§cA§4C§8] §7Unregistering §6addons§8...");
		(new Thread(() -> {
			try {
				for (Addon currentAddon : listAddons()) {
					if (currentAddon == null)
						continue;
					Boolean worked = currentAddon.disable();
					if (!worked)
						continue;
					removeAddon(currentAddon);
				}
			}catch (Exception error) { }
			instance.logger("§8[§4R§cA§4C§8] §7Done! All addons are now unloaded§8!");
		})).start();
	}

	public void registerAddon(File file) {
		String fileName;
		if (file == null) {
			instance.logger("§8[§4" + "ADDONSYSTEM" + "§8] §7This file does not §cexist§8!");
			return;
		}
		fileName = file.getName();
		if (fileName.endsWith(".jar")) {
			Addon addon = new Addon(instance, file, fileName.split(".jar")[0]);
			Boolean addonWorking = addon.enable();
			if (!addonWorking)
				return;
			addAddon(addon);
		}
	}

	public void unregisterAddon(Addon addon) {
		if (addon == null) {
			instance.logger("§8[§4" + "ADDONSYSTEM" + "§8] §7This addon is not §cregistered§8!");
			return;
		}
		Boolean worked = addon.disable();
		if (!worked)
			return;
		removeAddon(addon);
	}

	public Addon getAddonByName(String addonName) {
		Addon addon = null;
		for (Addon currentAddon : listAddons()) {
			if (!currentAddon.getName().equals(addonName))
				continue;
			addon = currentAddon;
			break;
		}
		return addon;
	}

	public Addon getAddonByFile(File file) {
		Addon addon = null;
		for (Addon currentAddon : listAddons()) {
			if (!currentAddon.getFile().equals(file))
				continue;
			addon = currentAddon;
			break;
		}
		return addon;
	}

	public Addon getAddonByObject(Object object) {
		Addon addon = null;
		for (Addon currentAddon : listAddons()) {
			if (!currentAddon.getObject().equals(object))
				continue;
			addon = currentAddon;
			break;
		}
		return addon;
	}

	public List<Addon> listAddons() {
		return addonList;
	}

	public Boolean isListed(Addon addon) {
		return addonList.contains(addon);
	}

	public void addAddon(Addon addon) {
		if (isListed(addon))
			return;
		addonList.add(addon);
	}

	public void removeAddon(Addon addon) {
		if (!isListed(addon))
			return;
		addonList.remove(addon);
	}
}