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
import de.rayzs.rayzsanticrasher.bstats.Metrics;
import de.rayzs.rayzsanticrasher.checks.impl.client.BlockDig;
import de.rayzs.rayzsanticrasher.checks.impl.client.Chat;
import de.rayzs.rayzsanticrasher.checks.impl.client.ClientCommand;
import de.rayzs.rayzsanticrasher.checks.impl.client.CreativeSlot;
import de.rayzs.rayzsanticrasher.checks.impl.client.CustomPayload;
import de.rayzs.rayzsanticrasher.checks.impl.client.EntityInteractor;
import de.rayzs.rayzsanticrasher.checks.impl.client.FlyFaker;
import de.rayzs.rayzsanticrasher.checks.impl.client.Netty;
import de.rayzs.rayzsanticrasher.checks.impl.client.Other;
import de.rayzs.rayzsanticrasher.checks.impl.client.Settings;
import de.rayzs.rayzsanticrasher.checks.impl.client.SignUpdater;
import de.rayzs.rayzsanticrasher.checks.impl.client.Spectate;
import de.rayzs.rayzsanticrasher.checks.impl.client.TabComplete;
import de.rayzs.rayzsanticrasher.checks.impl.client.WindowClicker;
import de.rayzs.rayzsanticrasher.checks.impl.listener.BotCheck;
import de.rayzs.rayzsanticrasher.checks.impl.listener.FAWE;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalCommand;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalEndTeleport;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalItemDrop;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalItemSpawn;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalMovement;
import de.rayzs.rayzsanticrasher.checks.impl.listener.IllegalSign;
import de.rayzs.rayzsanticrasher.checks.impl.listener.InvalidInteraction;
import de.rayzs.rayzsanticrasher.checks.impl.listener.RedstoneLag;
import de.rayzs.rayzsanticrasher.checks.impl.listener.VPNCheck;
import de.rayzs.rayzsanticrasher.checks.impl.server.HandshakeAttack;
import de.rayzs.rayzsanticrasher.checks.impl.server.InstantCrasher;
import de.rayzs.rayzsanticrasher.checks.impl.server.LoginStartAttack;
import de.rayzs.rayzsanticrasher.checks.impl.server.OnlyProxyPing;
import de.rayzs.rayzsanticrasher.checks.impl.server.StartPingAttack;
import de.rayzs.rayzsanticrasher.checks.impl.server.StatusPingAttack;
import de.rayzs.rayzsanticrasher.checks.meth.Attack;
import de.rayzs.rayzsanticrasher.file.FileManager;
import de.rayzs.rayzsanticrasher.json.SecuredJsonReader;
import de.rayzs.rayzsanticrasher.server.TinyProtocol;
import de.rayzs.rayzsanticrasher.database.sql.BasicSQL;
import de.rayzs.rayzsanticrasher.database.sql.MySQL;

public class RayzsAntiCrasher extends JavaPlugin {

	private static RayzsAntiCrasher instance;
	private static RayzsAntiCrasherAPI api;
	private String version = "2.2.1";
	private TinyProtocol serverInjector;
	private PluginManager pluginManager;
	private MySQL mysql;
	private BasicSQL notifySQL;
	private FileManager mysqlFile, checkFile, configFile;
	private Boolean useMySQL, validVersion, isSimpleCloud, isRunning, liveAttackCounter, debug,
			recommentedServerConfiguration, notifyUpdate, avaibleLicence = false;
	private File thisFile, addonFolder;
	private AddonManager addonManager;
	private String configFilePath, crashReportMessage, standardMessage, liveAttackMessage, kickMessage, vpnMessage,
			botMessage, downloadLink, vpnKey;
	private SecuredJsonReader jsonReader;
	protected Metrics metrics;

	@Override
	public void onDisable() {
		disable();
	}

	@Override
	public void onEnable() {
		metrics = new Metrics(this, 10804);
		enable();
	}

	public void enable() {
		isRunning = true;
		instance = this;
		(new Thread(() -> {
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
		})).start();
	}

	public void disable() {
		for (String currentAddress : api.getTempBlockedIPList()) {
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
		api.addCheck(new InstantCrasher(), true);
		api.addCheck(new HandshakeAttack(), true);
		api.addCheck(new LoginStartAttack(), true);
		api.addCheck(new StartPingAttack(), true);
		api.addCheck(new StatusPingAttack(), true);
		api.addCheck(new OnlyProxyPing(), true);
		// }

		// CLIENT {
		api.addCheck(new Netty(), true);
		api.addCheck(new CustomPayload(), true);
		api.addCheck(new CreativeSlot(), true);
		api.addCheck(new Settings(), true);
		api.addCheck(new Chat(), true);
		api.addCheck(new TabComplete(), true);
		api.addCheck(new ClientCommand(), true);
		api.addCheck(new BlockDig(), true);
		api.addCheck(new Spectate(), true);
		api.addCheck(new WindowClicker(), true);
		api.addCheck(new EntityInteractor(), true);
		api.addCheck(new FlyFaker(), true);
		api.addCheck(new SignUpdater(), true);
		api.addCheck(new Other(), true);
		// }

		// LISTENER {
		api.addCheck(new InvalidInteraction(), true);
		api.addCheck(new IllegalEndTeleport(), true);
		api.addCheck(new IllegalSign(), true);
		api.addCheck(new VPNCheck(), false);
		api.addCheck(new BotCheck(), false);
		api.addCheck(new IllegalItemDrop(), true);
		api.addCheck(new IllegalMovement(), true);
		api.addCheck(new IllegalItemSpawn(), true);
		api.addCheck(new RedstoneLag(), true);
		api.addCheck(new FAWE(), true);
		api.addCheck(new IllegalCommand(), false);
		// }
	}

	private void loadUpdate() {
		loadPlugin();
		validVersion = true;
		try {
			jsonReader = new SecuredJsonReader("https://www.rayzs.de/rayzsanticrasher/plugin.json");
			downloadLink = (String) jsonReader.get("download");
			final String onlineVersion = (String) jsonReader.get("version");
				if (!onlineVersion.equalsIgnoreCase(version)) {
					validVersion = false;
					logger("§8[§4R§cA§4C§8] §7This plugin is §c§noutdated§8!");
				}
		}catch (Exception exception) { validVersion = false; }
	}

	public void loadPlugin() {
		this.pluginManager = getServer().getPluginManager();
		liveAttackCounter = Boolean
				.valueOf(instance.getConfigFile().search("settings.liveAttackCounter").getString("true"));
		debug = Boolean.valueOf(instance.getConfigFile().search("settings.debugMode").getString("false"));
		recommentedServerConfiguration = Boolean
				.valueOf(instance.getConfigFile().search("settings.recommentedServerConfiguration").getString("true"));
		notifyUpdate = Boolean
				.valueOf(instance.getConfigFile().search("settings.notifyUpdate").getString("true"));
		vpnKey = configFile.search("settings.vpnKey").getString("Go to vpnapi.io and replace this text here with your api-key!");
		api = new RayzsAntiCrasherAPI(this, version);
		logger("§8[§4R§cA§4C§8] §7Injecting minecraft server...");
		serverInjector = new TinyProtocol(this);
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
		if (recommentedServerConfiguration) {
			logger("§8[§4R§cA§4C§8] §7Loading recommented server configuration...");
			try {
				loadRecommentedServerConfiguration();
			} catch (Exception error) {
				logger("§8[§4R§cA§4C§8] §7Error loading plugin§8! §7I need a §c§n1.8.8§7 server to work perfectly§8...");
			}
			logger("§8[§4R§cA§4C§8] §7Done!");
		}
		logger("§8[§4R§cA§4C§8] §7The plugin could be loaded §asuccessfully§8!");
		addonManager.loadAddons();
		new TinyProtocol(instance);
		checkForUpdate();
	}

	protected void loadRecommentedServerConfiguration() {
		String serverVersion = getServer().getVersion();
		if (serverVersion.contains("PaperSpigot")) {
			String searching = "use-native-transport";
			Boolean result = Boolean.parseBoolean(getServerPropeties(searching).toString());
			if (!result)
				return;
			setServerPropeties(searching, false);
		} else
			logger("§8[§4R§cA§4C§8] §7I recomment you to use §bPaperSpigot §7as your new server version§8!");
	}

	protected void loadPropeties() {
		crashReportMessage = instance.getConfigFile().search("messages.crashreport")
				.getString("&7&8[&9R&bA&9C&8] &b%CLIENT% &8| &b%DETECTION% %AMOUNT%&9x &b&n&o%PACKET%");
		standardMessage = instance.getConfigFile().search("messages.informations")
				.getString("&7This server is using &9Rayzs&bAnti&9Crasher &8- &b&l&nv%VERSION%&8.");
		liveAttackMessage = instance.getConfigFile().search("messages.liveattack").getString(
				"&8>> &8[&4&n%ATTACK%&8] &c&nSERVER IS UNDER ATTACK&8! &7Blocked&8-&7IP&8'&7s&8: &b&l&o&n%BLOCKED%&8 | &7CPS&8: &b%CPS% &8<<");
		kickMessage = instance.getConfigFile().search("messages.kick.crash").getString(
				"&9&lRayzs&b&lAnti&9&lCrasher%NEW%&7Don't try to &c&ncrash&7 my server&8.%NEW%&7Reason&8: &b%REASON%");
		vpnMessage = instance.getConfigFile().search("messages.kick.vpn")
				.getString("&9&lRayzs&b&lAnti&9&lCrasher%NEW%&7Don't try using an &c&nvpn &7on my server&8.");
		botMessage = instance.getConfigFile().search("messages.kick.bot")
				.getString("&9&lRayzs&b&lAnti&9&lCrasher%NEW%&7I don't like &cbots &7btw. :3");
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
	
	public SecuredJsonReader getJsonReader() {
		return jsonReader;
	}
	
	public String getVPNKey() {
		return vpnKey;
	}
	
	public String getDownloadLink() {
		return downloadLink;
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

	public String getKickMessage(String reason) {
		final String text = kickMessage.replace("%NEW%", "\n").replace("%REASON%", reason);
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public String getVPNKickMessage() {
		final String text = vpnMessage.replace("%NEW%", "\n");
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public String getBotKickMessage() {
		final String text = botMessage.replace("%NEW%", "\n");
		return ChatColor.translateAlternateColorCodes('&', text);
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
	
	public Boolean useNotifyUpdate() {
		return notifyUpdate;
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

	public TinyProtocol getServerInjector() {
		return this.serverInjector;
	}

	public Object getServerPropeties(String property) {
		return ((net.minecraft.server.v1_8_R3.DedicatedServer) net.minecraft.server.v1_8_R3.MinecraftServer
				.getServer()).propertyManager.properties.get(property);
	}

	public void setServerPropeties(String property, Object object) {
		((net.minecraft.server.v1_8_R3.DedicatedServer) net.minecraft.server.v1_8_R3.MinecraftServer
				.getServer()).propertyManager.setProperty(property, object);
		((net.minecraft.server.v1_8_R3.DedicatedServer) net.minecraft.server.v1_8_R3.MinecraftServer
				.getServer()).propertyManager.savePropertiesFile();
	}

	public void logger(String text) {
		Bukkit.getConsoleSender().sendMessage("[RayzsAPI | RAC] " + text);
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (!players.isOp())
				continue;
			players.sendMessage(text);
		}
	}
	
	protected Integer taskID;
	
	@SuppressWarnings("deprecation")
	protected void checkForUpdate() {
		if(!notifyUpdate) return;
		taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {	
			@Override
			public void run() {
				try {
					jsonReader = new SecuredJsonReader("https://www.rayzs.de/rayzsanticrasher/plugin.json");
					final String onlineVersion = (String) jsonReader.get("version");
						if (!onlineVersion.equalsIgnoreCase(version)) {
							downloadLink = (String) jsonReader.get("download");
							validVersion = false;
							Bukkit.getScheduler().cancelTask(taskID);
						}
			}catch (Exception exception) { validVersion = false; }
			}
		}, 20*60*15, 20*60*15);
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

	protected void loadSQL() {
		try {
			notifySQL = new BasicSQL("RayzsAntiCrasher", "UUID text, NOTIFY integer", "'" + 1 + "'");
		} catch (Exception error) {
		}
	}
}