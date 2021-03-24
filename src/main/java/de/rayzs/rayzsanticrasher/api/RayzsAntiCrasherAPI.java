package de.rayzs.rayzsanticrasher.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.crasher.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.crasher.meth.Attack;
import de.rayzs.rayzsanticrasher.json.JsonReader;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.player.CrashPlayer;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import de.rayzs.rayzsanticrasher.runtime.RuntimeExec;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class RayzsAntiCrasherAPI {

	private RayzsAntiCrasher instance;
	private Notify notify;
	private List<ServerCheck> serverCheckList;
	private List<ClientCheck> clientCheckList;
	private List<ClientSourceCheck> clientSourceCheckList;
	private List<Listener> listenerCheckList;
	private List<Player> notifyList;
	private List<String> notifyAddressList;
	private HashMap<Player, CrashPlayer> crashplayerHash;
	private HashMap<UUID, Integer> notifyHash;
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
		crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		notifyList = new ArrayList<>();
		notifyAddressList = new ArrayList<>();
		useIPTables = Boolean.valueOf(instance.getConfigFile().search("settings.iptables").getString("false"));
		handshakeAttack = new Attack("Handshake", useIPTables);
		loginAttack = new Attack("Login", useIPTables);
		pingAttack = new Attack("Ping", useIPTables);
		pingStatusAttack = new Attack("PingStatus", useIPTables);
		this.version = version;
	}

	public void destroy() {
		clientCheckList = new ArrayList<>();
		this.crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		notifyList = new ArrayList<>();
	}

	public void createCrashPlayer(Player player) {
		this.crashplayerHash.put(player, new CrashPlayer(player));
		getCrashPlayer(player).start();
	}

	public void deleteCrashPlayer(Player player) {
		getCrashPlayer(player).stop();
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
		return;
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
	
	public void disconnectChannel(Channel channel) {
		String clientAddress = channel.remoteAddress().toString().split(":")[0].replace("/", "");
		channel.flush();
		channel.close();
		ipTable(clientAddress, true);
		try { Thread.sleep(1000); } catch (InterruptedException error) { if(instance.useDebug()) error.printStackTrace(); }
		ipTable(clientAddress, false);
	}

	public void addCheck(ClientCheck clientCheck) {
		if (this.clientCheckList.contains(clientCheck))
			return;
		String check = clientCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.client." + check).getString("true").equals("true"))
			this.clientCheckList.add(clientCheck);
	}

	public void addCheck(ClientSourceCheck clientSourceCheck) {
		if (this.clientSourceCheckList.contains(clientSourceCheck))
			return;
		String check = clientSourceCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.clientsource." + check).getString("true").equals("true"))
			this.clientSourceCheckList.add(clientSourceCheck);
	}

	public void addCheck(ServerCheck serverCheck) {
		if (this.serverCheckList.contains(serverCheck))
			return;
		String check = serverCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.server." + check).getString("true").equals("true"))
			this.serverCheckList.add(serverCheck);
	}

	public void addCheck(Listener listenerCheck) {
		if (this.listenerCheckList.contains(listenerCheck))
			return;
		String check = listenerCheck.getClass().getSimpleName().toLowerCase().split("@")[0];
		if (instance.getCheckFile().search("checks.listener." + check).getString("true").equals("true")) {
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
		try {
			return new JsonReader("http://api.vpnblocker.net/v2/json/" + clientAddress).get("host-ip").equals("true");
		} catch (Exception error) {
			return false;
		}
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

	public String hasOnlyLetters(String text) {
		for (char c : text.toCharArray()) {
			if (c >= 'a' && c <= 'z')
				continue;
			if (c >= 'A' && c <= 'Z')
				continue;
			if (c >= '0' && c <= '9')
				continue;
			if (c == 'ä' || c == 'ö' || c == 'ü' || c == 'Ä' || c == 'Ö' || c == 'Ü' || c == '_')
				continue;
			return "" + c;
		}
		return "empty";
	}

	public boolean hasInvalidChars(String text) {
		for (char c : text.toCharArray()) {
			if (c >= 'a' && c <= 'z')
				return true;
			if (c >= 'A' && c <= 'Z')
				return true;
			if (c == 'ö' || c == 'ß' || c == 'ä' || c == 'ü' || c == 'Ö' || c == 'Ä' || c == 'Ü')
				return true;
			if (c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9'
					|| c == '0' || c == '_')
				return true;
		}
		return false;
	}

	private int count;

	@SuppressWarnings("null")
	public boolean hasInvalidTag(NBTTagCompound nbtTagCompound) { // BY GODECHO
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
				if (!hasInvalidChars(content))
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