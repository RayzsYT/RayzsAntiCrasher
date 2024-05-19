package de.rayzs.tinyrayzsanticrasher.plugin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.enums.LanguageEnum;
import de.rayzs.tinyrayzsanticrasher.file.FileManager;
import de.rayzs.tinyrayzsanticrasher.injection.CrashClient;
import de.rayzs.tinyrayzsanticrasher.language.LanguageManager;
import de.rayzs.tinyrayzsanticrasher.plugin.listener.Handler;
import de.rayzs.tinyrayzsanticrasher.plugin.metrics.Metrics;
import de.rayzs.tinyrayzsanticrasher.reflection.Reflection;

public class TinyRayzsAntiCrasher extends JavaPlugin {

	private FileManager configFile;
	private Reflection reflection;
	private TinyRayzsAntiCrasherAPI api;
	private LanguageManager languageManager;
	private String version;
	protected Metrics metrics;

	@Override
	public void onEnable() {
		metrics = new Metrics(this, 11170);
		version = "0.0.1";
		configFile = new FileManager(this, new File("plugins/TinyRayzsAntiCrasher", "config.yml"));
		final Boolean silentMode = Boolean.valueOf(configFile.search("silentMode").getString("false"));
		final LanguageEnum consoleLanguage = LanguageEnum
				.valueOf(configFile.search("consoleLanguage").getString("en").toUpperCase());
		final PluginManager pluginManager = getServer().getPluginManager();
		reflection = new Reflection(this);
		api = new TinyRayzsAntiCrasherAPI(this, silentMode, consoleLanguage);
		languageManager = new LanguageManager(this);

		pluginManager.registerEvents(new Handler(this, api), this);
		new CrashClient(this, api);

		Bukkit.getOnlinePlayers().forEach(players -> {
			api.setLanguage(players, LanguageEnum.EN);
		});
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	public String getVersion() {
		return version;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public Reflection getReflection() {
		return reflection;
	}
}