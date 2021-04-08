package de.rayzs.rayzsanticrasher.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.checks.meth.Attack;
import de.rayzs.rayzsanticrasher.json.SecuredJsonReader;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.player.CrashPlayer;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import de.rayzs.rayzsanticrasher.runtime.RuntimeExec;
import de.rayzs.rayzsanticrasher.server.PacketCounter;
import io.netty.channel.Channel;

public class RayzsAntiCrasherAPI {

	private RayzsAntiCrasher instance;
	private Notify notify;
	private List<ServerCheck> serverCheckList;
	private List<ClientCheck> clientCheckList;
	private List<ClientSourceCheck> clientSourceCheckList;
	private List<Listener> listenerCheckList;
	private List<Player> notifyList;
	private List<String> notifyAddressList, tempBlockedPlayers;
	private HashMap<Player, CrashPlayer> crashplayerHash;
	private HashMap<UUID, Integer> notifyHash;
	private HashMap<Channel, PacketCounter> packetCounterHash;
	private Attack handshakeAttack, loginAttack, pingAttack, pingStatusAttack;
	private String version;
	private Boolean useIPTables;

	public RayzsAntiCrasherAPI(RayzsAntiCrasher instance, String version) {
		this.instance = instance;
		notify = new Notify(instance, this);
		clientCheckList = new ArrayList<>();
		clientSourceCheckList = new ArrayList<>();
		serverCheckList = new ArrayList<>();
		listenerCheckList = new ArrayList<>();
		notifyList = new ArrayList<>();
		tempBlockedPlayers = new ArrayList<>();
		notifyAddressList = new ArrayList<>();
		crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		packetCounterHash = new HashMap<>();
		useIPTables = Boolean.valueOf(instance.getConfigFile().search("settings.iptables").getString("false"));
		handshakeAttack = new Attack("Handshake", useIPTables);
		loginAttack = new Attack("Login", useIPTables);
		pingAttack = new Attack("Ping", useIPTables);
		pingStatusAttack = new Attack("PingStatus", useIPTables);
		this.version = version;
	}

	public void destroy() {
		clientCheckList = new ArrayList<>();
		crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		notifyList = new ArrayList<>();
	}

	public void createPacketCounter(Channel channel) {
		this.packetCounterHash.put(channel, new PacketCounter(channel));
	}

	public void deletePacketCounter(Channel channel) {
		this.packetCounterHash.remove(channel);;
	}

	public Boolean existPacketCounter(Channel channel) {
		return (this.packetCounterHash.get(channel) != null);
	}

	public PacketCounter getPacketCounter(Channel channel) {
		return this.packetCounterHash.get(channel);
	}
	
	public void createCrashPlayer(Player player) {
		this.crashplayerHash.put(player, new CrashPlayer(player));
	}

	public void deleteCrashPlayer(Player player) {
		try { getCrashPlayer(player).uninject(); } catch (Exception exception) { }
		this.crashplayerHash.remove(player);
	}

	public Boolean existCrashPlayer(Player player) {
		return (this.crashplayerHash.get(player) != null);
	}

	public CrashPlayer getCrashPlayer(Player player) {
		return this.crashplayerHash.get(player);
	}

	public void ipTable(String address, Boolean bool) {
		if (!useIPTables)
			return;
		if (bool) {
			new RuntimeExec("iptables -I INPUT -s " + address + " -j DROP");
			return;
		}
		new RuntimeExec("iptables -D INPUT -s " + address + " -j DROP");
	}

	public void unregisterAll() {
		clientCheckList.clear();
		clientSourceCheckList.clear();
		serverCheckList.clear();
		for (Listener currentListener : listenerCheckList)
			instance.unregisterEvent(currentListener);
		listenerCheckList.clear();
	}

	public void createCustomReport(Player player, Class<?> clazz, String reason) {
		doNotify("§8[§9R§bA§9C§8] §b" + player.getName() + " §8» §b" + clazz.getSimpleName() + " §8┊┊ §b" + reason,
				player);
	}

	public void kickPlayer(Player player, String reason) {
		try {
			final String kickMessage = instance.getKickMessage(reason);
			final net.minecraft.server.v1_8_R3.IChatBaseComponent textComponent = new net.minecraft.server.v1_8_R3.ChatComponentText(kickMessage);
			final net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect packet = new net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect(textComponent);
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			return;
		}catch (Exception | NoClassDefFoundError error) { }
	}
	
	public void disconnectChannel(Channel channel) {
		String clientAddress = channel.remoteAddress().toString().split(":")[0].replace("/", "");
		channel.flush();
		channel.close();
		addTempBlockedIP(clientAddress);
		ipTable(clientAddress, true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException error) {
			if (instance.useDebug())
				error.printStackTrace();
		}
		removeTempBlockedIP(clientAddress);
		ipTable(clientAddress, false);
	}

	public void addCheck(ClientCheck clientCheck, Boolean bool) {
		if (this.clientCheckList.contains(clientCheck))
			return;
		String check = clientCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.client." + check).getString(bool.toString()).equals("true"))
			this.clientCheckList.add(clientCheck);
	}

	public void addCheck(ClientSourceCheck clientSourceCheck, Boolean bool) {
		if (this.clientSourceCheckList.contains(clientSourceCheck))
			return;
		String check = clientSourceCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.clientsource." + check).getString(bool.toString()).equals("true"))
			this.clientSourceCheckList.add(clientSourceCheck);
	}

	public void addCheck(ServerCheck serverCheck, Boolean bool) {
		if (this.serverCheckList.contains(serverCheck))
			return;
		String check = serverCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.server." + check).getString(bool.toString()).equals("true"))
			this.serverCheckList.add(serverCheck);
	}

	public void addCheck(Listener listenerCheck, Boolean bool) {
		if (this.listenerCheckList.contains(listenerCheck))
			return;
		String check = listenerCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.listener." + check).getString(bool.toString()).equals("true")) {
			this.listenerCheckList.add(listenerCheck);
			instance.registerEvent(listenerCheck);
		}
	}

	public void removeCheck(ClientCheck clientCheck) {
		if (this.clientCheckList.contains(clientCheck))
			this.clientCheckList.remove(clientCheck);
	}

	public void removeCheck(ClientSourceCheck clientSourceCheck) {
		if (this.clientSourceCheckList.contains(clientSourceCheck))
			this.clientSourceCheckList.remove(clientSourceCheck);
	}

	public void removeCheck(ServerCheck serverCheck) {
		if (this.serverCheckList.contains(serverCheck))
			this.serverCheckList.remove(serverCheck);
	}

	public void removeCheck(Listener listenerCheck) {
		if (this.listenerCheckList.contains(listenerCheck)) {
			this.listenerCheckList.remove(listenerCheck);
			instance.unregisterEvent(listenerCheck);
		}
	}

	public void addTempBlockedIP(String clientAddress) {
		if (hasTempBlockedIP(clientAddress))
			return;
		tempBlockedPlayers.add(clientAddress);
	}

	public void removeTempBlockedIP(String clientAddress) {
		if (!hasTempBlockedIP(clientAddress))
			return;
		tempBlockedPlayers.remove(clientAddress);
	}

	public Boolean hasTempBlockedIP(String clientAddress) {
		return tempBlockedPlayers.contains(clientAddress);
	}

	public List<String> getTempBlockedIPList() {
		return tempBlockedPlayers;
	}

	public List<ClientCheck> getClientChecks() {
		return clientCheckList;
	}

	public List<ClientSourceCheck> getClientSourceChecks() {
		return clientSourceCheckList;
	}

	public List<ServerCheck> getServerChecks() {
		return serverCheckList;
	}

	public List<Listener> getListenerChecks() {
		return listenerCheckList;
	}

	public Boolean existNotify(Player player) {
		return (notifyHash.get(player.getUniqueId()) != null);
	}

	public Boolean doIPTable() {
		return useIPTables;
	}

	public Boolean isVPN(String clientAddress) {
		SecuredJsonReader unsecuredJsonReader = new SecuredJsonReader(
				"https://vpnapi.io/api/" + clientAddress + "?key=F9J3K1V02MFO1C93KA7B.json");
		unsecuredJsonReader.get("ip");
		Boolean vpn = (Boolean) unsecuredJsonReader.get("security", "vpn");
		Boolean proxy = (Boolean) unsecuredJsonReader.get("security", "proxy");
		Boolean tor = (Boolean) unsecuredJsonReader.get("security", "tor");
		if (vpn || proxy | tor)
			return true;
		return false;
	}

	public Boolean isProxy(String clientAddress) {
		SecuredJsonReader unsecuredJsonReader = new SecuredJsonReader(
				"https://vpnapi.io/api/" + clientAddress + "?key=F9J3K1V02MFO1C93KA7B.json");
		unsecuredJsonReader.get("ip");
		Boolean proxy = (Boolean) unsecuredJsonReader.get("security", "proxy");
		if (proxy)
			return true;
		return false;
	}

	public Integer getNotify(Player player) {
		if (!notifyHash.containsKey(player.getUniqueId()))
			setNotify(player, 1);
		return notifyHash.get(player.getUniqueId());
	}

	public Integer getNotify(UUID uuid) {
		return notifyHash.get(uuid);
	}

	public Attack getHandshakeAttack() {
		return handshakeAttack;
	}

	public Attack getLoginAttack() {
		return loginAttack;
	}

	public Attack getPingAttack() {
		return pingAttack;
	}

	public Attack getPingStatusAttack() {
		return pingStatusAttack;
	}

	public Double getServerTPS() {
		Double tps = null;
			for (double currentTPS : net.minecraft.server.v1_8_R3.MinecraftServer.getServer().recentTps) {
				tps = (Double) (Math.round(currentTPS * Math.pow(10, 2)) / Math.pow(10, 2));
				break;
			}
			if (tps > 20)
				tps = (Double) 20.0;
		return tps;
	}

	public void setNotify(Player player, Integer value) {
		notifyHash.put(player.getUniqueId(), value);
	}

	public void doNotify(String text, Player player) {
		if (notifyList.contains(player))
			return;
		notifyList.add(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
			@Override
			public void run() {
				notify.send(text);
			}
		});
	}

	public void doNotify(String text, String notifyAddress) {
		if (notifyAddressList.contains(notifyAddress))
			return;
		notifyAddressList.add(notifyAddress);
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
			@Override
			public void run() {
				notify.send(text);
			}
		});
	}

	public String getVersion() {
		return version;
	}

	public String hasOnlyLettersString(String text) {
		for (char c : text.toCharArray()) {
			if (c >= 'a' && c <= 'z')
				continue;
			if (c >= 'A' && c <= 'Z')
				continue;
			if (c >= '0' && c <= '9')
				continue;
			if (c == 'ä' || c == 'ö' || c == 'ü' || c == 'Ä' || c == 'Ö' || c == 'Ü' || c == '_' || c == 'ß')
				continue;
			return "" + c;
		}
		return "empty";
	}

	public Boolean hasOnlyLettersBoolean(String text) {
		for (char c : text.toCharArray()) {
			if (c >= 'a' && c <= 'z')
				continue;
			if (c >= 'A' && c <= 'Z')
				continue;
			if (c >= '0' && c <= '9')
				continue;
			if (c == 'ä' || c == 'ö' || c == 'ü' || c == 'Ä' || c == 'Ö' || c == 'Ü' || c == '_' || c == 'ß')
				continue;
			return false;
		}
		return true;
	}

	private int count;

	@SuppressWarnings("null")
	public boolean hasInvalidTag(net.minecraft.server.v1_8_R3.NBTTagCompound nbtTagCompound) { // BY GODECHO
		if (nbtTagCompound != null)
			return false;
		assert false;
		if (nbtTagCompound.hasKey("Fireworks") || nbtTagCompound.hasKey("Explosion")) {
			if (nbtTagCompound.toString().length() > 2000)
				return true;
		}
		if (nbtTagCompound.hasKey("pages")) {
			if (nbtTagCompound.getList("pages", 8).size() > 100)
				return true;
			for (int i = 0; i < nbtTagCompound.getList("pages", 8).size(); i++) {
				count += nbtTagCompound.getList("pages", 8).getString(i).length();
				String content = nbtTagCompound.getList("pages", 8).getString(i);
				if (count > 10)
					return true;
				if (!hasOnlyLettersBoolean(content))
					return true;
				if (content.contains(": {"))
					return true;
				if (getCount(content, "§".charAt(0)) > 20)
					return true;
				if (getCount(content, ".".charAt(0)) > 20)
					return true;
			}
		}
		return false;
	}

	private int getCount(String input, char c) {
		int count = 0;
		for (char act : input.toCharArray())
			if (act == c)
				count++;
		return count;
	}
}