package de.rayzs.rayzsanticrasher.plugin;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.rayzsanticrasher.plugin.commands.SetupCommand;
import de.rayzs.rayzsanticrasher.plugin.listener.PlayerJoin;
import de.rayzs.rayzsanticrasher.plugin.listener.PlayerQuit;
import de.rayzs.rayzsanticrasher.addon.AddonManager;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.crasher.impl.client.BlockDig;
import de.rayzs.rayzsanticrasher.crasher.impl.client.Chat;
import de.rayzs.rayzsanticrasher.crasher.impl.client.ClientCommand;
import de.rayzs.rayzsanticrasher.crasher.impl.client.CreativeSlot;
import de.rayzs.rayzsanticrasher.crasher.impl.client.CustomPayload;
import de.rayzs.rayzsanticrasher.crasher.impl.client.EntityInteractor;
import de.rayzs.rayzsanticrasher.crasher.impl.client.FlyFaker;
import de.rayzs.rayzsanticrasher.crasher.impl.client.Netty;
import de.rayzs.rayzsanticrasher.crasher.impl.client.Other;
import de.rayzs.rayzsanticrasher.crasher.impl.client.Settings;
import de.rayzs.rayzsanticrasher.crasher.impl.client.SignUpdater;
import de.rayzs.rayzsanticrasher.crasher.impl.client.Spectate;
import de.rayzs.rayzsanticrasher.crasher.impl.client.TabComplete;
import de.rayzs.rayzsanticrasher.crasher.impl.client.WindowClicker;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.VPNCheck;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.BotCheck;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.ChunkOverflower;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalEndTeleport;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalCommand;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalItemDrop;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalItemSpawn;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalMovement;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.IllegalSign;
import de.rayzs.rayzsanticrasher.crasher.impl.listener.InvalidInteraction;
import de.rayzs.rayzsanticrasher.crasher.impl.server.HandshakeAttack;
import de.rayzs.rayzsanticrasher.crasher.impl.server.InstantCrasher;
import de.rayzs.rayzsanticrasher.crasher.impl.server.LoginStartAttack;
import de.rayzs.rayzsanticrasher.crasher.impl.server.OnlyProxyPing;
import de.rayzs.rayzsanticrasher.crasher.impl.server.StartPingAttack;
import de.rayzs.rayzsanticrasher.crasher.impl.server.StatusPingAttack;
import de.rayzs.rayzsanticrasher.crasher.meth.Attack;
import de.rayzs.rayzsanticrasher.file.FileManager;
import de.rayzs.rayzsanticrasher.server.ServerInjector;
import de.rayzs.rayzsanticrasher.spigotmc.UpdateChecker;
import net.minecraft.server.v1_8_R3.DedicatedServer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import de.rayzs.rayzsanticrasher.database.sql.BasicSQL;
import de.rayzs.rayzsanticrasher.database.sql.MySQL;

public class RayzsAntiCrasher extends JavaPlugin {

	private static RayzsAntiCrasher instance;
	private static RayzsAntiCrasherAPI api;
	private String version = "2.1.6";
	private ServerInjector serverInjector;
	private PluginManager pluginManager;
	private MySQL mysql;
	private BasicSQL notifySQL;
	private FileManager mysqlFile, checkFile, configFile;
	private Boolean useMySQL, validVersion, isSimpleCloud, isRunning, liveAttackCounter, debug, recommentedServerConfiguration, avaibleLicence = false;
	private File thisFile, addonFolder;
	private AddonManager addonManager;
	private String configFilePath, crashReportMessage, standardMessage, liveAttackMessage;

	@Override
	public void onDisable() {
		disable();
	}

	@Override
	public void onEnable() {
		enable();
	}

	public void enable() {
		isRunning = true;
		instance = this;
		thisFile = getFile();
		configFile = new FileManager(new File("plugins/RayzsAntiCrasher", "config.rayzs"));
		configFilePath = configFile.search("filePath").getString("plugins/RayzsAntiCrasher");
		mysqlFile = new FileManager(new File(configFilePath, "mysql.rayzs"));
		checkFile = new FileManager(new File(configFilePath, "checks.rayzs"));
		addonFolder = new File(configFilePath + "/addons");
		addonManager = new AddonManager(instance, api, addonFolder);
		if (!addonFolder.exists())
			addonFolder.mkdir();
		loadUpdate();
	}

	public void disable() {
		for(String currentAddress : api.getTempBlockedIPList()) {
			api.ipTable(currentAddress, false);
			api.removeTempBlockedIP(currentAddress);
		}
		isRunning = false;
		if (!avaibleLicence) {
			HandlerList.unregisterAll();
			return;
		}
		addonManager.unloadAddons();
		logger("§8[§4R§cA§4C§8] §7The plugin is trying to §6shutting down§8...");
		if (serverInjector != null) {
			serverInjector.close();
		}
		serverInjector = null;
		try {
			HandlerList.unregisterAll();
			if (api != null) {
				Bukkit.getOnlinePlayers().forEach(players -> api.deleteCrashPlayer(players));
				api.destroy();
			}
		} catch (Exception error) {
		}
		disable();
		logger("§8[§4R§cA§4C§8] §7The plugin is now §coffline§8!");
	}

	public void reload() {
		disable();
		enable();
	}

	public void implementsChecks() {
		// Server {
		api.addCheck(new InstantCrasher());
		api.addCheck(new HandshakeAttack());
		api.addCheck(new LoginStartAttack());
		api.addCheck(new StartPingAttack());
		api.addCheck(new StatusPingAttack());
		api.addCheck(new OnlyProxyPing());
		// }

		// CLIENT {
		api.addCheck(new Netty());
		api.addCheck(new CustomPayload());
		api.addCheck(new CreativeSlot());
		api.addCheck(new Settings());
		api.addCheck(new Chat());
		api.addCheck(new TabComplete());
		api.addCheck(new ClientCommand());
		api.addCheck(new IllegalCommand());
		api.addCheck(new BlockDig());
		api.addCheck(new Spectate());
		api.addCheck(new WindowClicker());
		api.addCheck(new EntityInteractor());
		api.addCheck(new FlyFaker());
		api.addCheck(new SignUpdater());
		api.addCheck(new Other());
		// }

		// LISTENER {
		api.addCheck(new InvalidInteraction());
		api.addCheck(new IllegalEndTeleport());
		api.addCheck(new IllegalSign());
		api.addCheck(new ChunkOverflower());
		api.addCheck(new VPNCheck());
		api.addCheck(new BotCheck());
		api.addCheck(new IllegalItemDrop());
		api.addCheck(new IllegalMovement());
		api.addCheck(new IllegalItemSpawn());
		// }
	}

	private void loadUpdate() {
		validVersion = false;
		logger("§8[§4R§cA§4C§8] §7The plugin is just checked for a new version...");
		new UpdateChecker(this).getVersion(gotVersion -> {
			if (gotVersion.equalsIgnoreCase(version)) {
				logger("§8[§4R§cA§4C§8] §7This plugin is up to date!");
				validVersion = true;
			} else
				logger("§8[§4R§cA§4C§8] §7This plugin is §c§noutdated§8!");
			loadPlugin();
		});
	}

	public void loadPlugin() {
		this.pluginManager = getServer().getPluginManager();
		liveAttackCounter = Boolean.valueOf(instance.getConfigFile().search("settings.liveAttackCounter").getString("true"));
		debug = Boolean.valueOf(instance.getConfigFile().search("settings.debugMode").getString("true"));
		recommentedServerConfiguration = Boolean.valueOf(instance.getConfigFile().search("settings.recommentedServerConfiguration").getString("true"));
		api = new RayzsAntiCrasherAPI(this, version);
		logger("§8[§4R§cA§4C§8] §7Injecting minecraft server...");
		serverInjector = new ServerInjector(this);
		implementsChecks();
		new PlayerJoin();
		new PlayerQuit();
		new SetupCommand(instance, api);
		try {
			useMySQL = mysqlFile.getYAML().getBoolean("enabled");
			mysqlFile.set("enabled", useMySQL);
		} catch (Exception error) {
			mysqlFile.set("enabled", false);
		}
		mysql = new MySQL(mysqlFile.search("host").getString("localhost"), mysqlFile.search("port").getInt(3306),
				mysqlFile.search("username").getString("Username"), mysqlFile.search("passwort").getString("Password"),
				mysqlFile.search("database").getString("Database"));
		if (mysql.isConnected())
			loadSQL();
		loadAllPlayers();
		logger("§8[§4R§cA§4C§8] §7Loading properties...");
		loadPropeties();
		if(recommentedServerConfiguration) {
			logger("§8[§4R§cA§4C§8] §7Loading recommented server configuration...");
			loadRecommentedServerConfiguration();
			logger("§8[§4R§cA§4C§8] §7Done!");
		}
		logger("§8[§4R§cA§4C§8] §7The plugin could be loaded §asuccessfully§8!");
		addonManager.loadAddons();
	}
	
	private void loadRecommentedServerConfiguration() {
		String serverVersion = getServer().getVersion();
		if(serverVersion.contains("PaperSpigot")) {
			String searching = "use-native-transport";
			Boolean result = Boolean.parseBoolean(getServerPropeties(searching).toString());
			if(!result) return;
			setServerPropeties(searching, false);
		}else
			logger("§8[§4R§cA§4C§8] §7We recomment you to use §bPaperSpigot §7as your new server version§8!");
	}
	
	protected void loadPropeties() {
		crashReportMessage = instance.getConfigFile().search("messages.crashreport")
				.getString("&7&8[&9R&bA&9C&8] &b%CLIENT% &8| &b%DETECTION% %AMOUNT%&9x &b&n&o%PACKET%");
		standardMessage = instance.getConfigFile().search("messages.informations")
				.getString("&7This server is using &9Rayzs&bAnti&9Crasher &8- &b&l&nv%VERSION%&8.");
		liveAttackMessage = instance.getConfigFile().search("messages.liveattack")
				.getString("&8>> &8[&4&n%ATTACK%&8] &c&nSERVER IS UNDER ATTACK&8! &7Blocked&8-&7IP&8'&7s&8: &b&l&o&n%BLOCKED%&8 | &7CPS&8: &b%CPS% &8<<");
		logger("§8[§4R§cA§4C§8] §7Done!");
	}

	public static RayzsAntiCrasher getInstance() {
		return instance;
	}

	public static RayzsAntiCrasherAPI getAPI() {
		return api;
	}

	public void registerEvent(Listener listener) {
		this.pluginManager.registerEvents(listener, this);
	}

	public void unregisterEvent(Listener listener) {
		HandlerList.unregisterAll(listener);
	}

	protected void loadAllPlayers() {
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (!api.existCrashPlayer(players))
				api.createCrashPlayer(players);
			if (!instance.useMySQL()) {
				if (players.hasPermission("rayzsanticrasher.notify") && !api.existNotify(players))
					api.setNotify(players, 1);
			} else
				players.kickPlayer("§cYou have to reconnect from the server!");
			;
		}
	}

	public AddonManager getAddonManager() {
		return addonManager;
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public File getAddonsFolder() {
		return addonFolder;
	}

	public File getThisFile() {
		return thisFile;
	}

	public BasicSQL getNotifySQL() {
		return notifySQL;
	}

	public MySQL getMySQL() {
		return mysql;
	}

	public String getStandartMessage() {
		final String text = standardMessage.replace("%VERSION%", version);
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public String getCrashReportMessage(String client, Integer amount, String check, String packet) {
		final String text = (crashReportMessage.replace("%CLIENT%", client).replace("%AMOUNT%", amount.toString())
				.replace("%DETECTION%", check).replace("%PACKET%", packet));
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	public String getLiveAttackMessage(Attack attack) {
		final String text = (liveAttackMessage.replace("%CPS%", attack.getConnections().toString())
				.replace("%ATTACK%", attack.getTaskName()).replace("%BLOCKED%", attack.getBlacklist().size() + ""));
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public Boolean useLiveAttackCounter() {
		return liveAttackCounter;
	}
	
	public Boolean useMySQL() {
		return useMySQL;
	}
	
	public Boolean useDebug() {
		return debug;
	}

	public Boolean isSimpleCloud() {
		return isSimpleCloud;
	}

	public Boolean isRunning() {
		return isRunning;
	}

	public Boolean hasValidVersion() {
		return validVersion;
	}

	public FileManager getCheckFile() {
		return checkFile;
	}

	public FileManager getConfigFile() {
		return configFile;
	}

	public ServerInjector getServerInjector() {
		return this.serverInjector;
	}
	
	public Object getServerPropeties(String property) {
		return ((DedicatedServer) MinecraftServer.getServer()).propertyManager.properties.get(property);
	}
	
	public void setServerPropeties(String property, Object object) {
	   ((DedicatedServer) MinecraftServer.getServer()).propertyManager.setProperty(property, object);
	   ((DedicatedServer) MinecraftServer.getServer()).propertyManager.savePropertiesFile();
	}
	
	protected void loadSQL() {
		try {
			notifySQL = new BasicSQL("RayzsAntiCrasher", "UUID text, NOTIFY integer", "'" + 1 + "'");
		} catch (Exception error) {
		}
	}

	public void logger(String text) {
		Bukkit.getConsoleSender().sendMessage("[RayzsAPI | RAC] " + text);
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (!players.isOp())
				continue;
			players.sendMessage(text);
		}
	}
}