package de.rayzs.rayzsanticrasher.api;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.checks.meth.Attack;
import de.rayzs.rayzsanticrasher.json.UnsecuredJsonReader;
import de.rayzs.rayzsanticrasher.notify.Notify;
import de.rayzs.rayzsanticrasher.packet.PacketCounter;
import de.rayzs.rayzsanticrasher.player.CrashPlayer;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import de.rayzs.rayzsanticrasher.runtime.RuntimeExec;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemBookAndQuill;
import net.minecraft.server.v1_8_R3.ItemFireworks;
import net.minecraft.server.v1_8_R3.ItemFireworksCharge;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.ItemWrittenBook;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

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
	private HashMap<Object, PacketCounter> packetCounterHash;
	private Attack serverAttack;
	private String version;
	private Boolean useIPTables, useIPSet;
	private PacketCounter attackPacketCounter;
	private Pattern pattern;

	public RayzsAntiCrasherAPI(RayzsAntiCrasher instance, String version) {
		this.instance = instance;
		notify = new Notify(instance, this);
		clientCheckList = new ArrayList<>();
		clientSourceCheckList = new ArrayList<>();
		serverCheckList = new ArrayList<>();
		listenerCheckList = new ArrayList<>();
		notifyList = new ArrayList<>();
		notifyAddressList = new ArrayList<>();
		crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		packetCounterHash = new HashMap<>();
		useIPTables = Boolean.valueOf(instance.getConfigFile().search("settings.iptables").getString("false"));
		useIPSet = Boolean.valueOf(instance.getConfigFile().search("settings.ipSet").getString("false"));
		serverAttack = new Attack("Attack", useIPTables);
		this.version = version;
		attackPacketCounter = new PacketCounter("Attack");
		pattern = Pattern.compile("URL");
		if (useIPSet) {
			new RuntimeExec("ipset create racban hash:ip maxelem 4194304");
			new RuntimeExec("iptables -t raw -I PREROUTING -m set --match-set racban src -j DROP");
		}
	}

	public void destroy() {
		clientCheckList = new ArrayList<>();
		crashplayerHash = new HashMap<>();
		notifyHash = new HashMap<>();
		notifyList = new ArrayList<>();
	}

	public void createPacketCounter(Object objective) {
		this.packetCounterHash.put(objective, new PacketCounter(objective));
	}

	public void deletePacketCounter(Object objective) {
		this.packetCounterHash.remove(objective);
		;
	}

	public Boolean existPacketCounter(Object objective) {
		return (this.packetCounterHash.get(objective) != null);
	}

	public PacketCounter getPacketCounter(Object objective) {
		return this.packetCounterHash.get(objective);
	}

	public PacketCounter getAttackPacketCounter() {
		return this.attackPacketCounter;
	}

	public void createCrashPlayer(Player player) {
		this.crashplayerHash.put(player, new CrashPlayer(player));
	}

	public void deleteCrashPlayer(Player player) {
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
			if(useIPSet) new RuntimeExec("ipset add racban " + address);
			if(useIPTables) new RuntimeExec("iptables -I INPUT -s " + address + " -j DROP");
			unbanIP(address);
			return;
		}
		if(useIPSet) new RuntimeExec("ipset del racban " + address);
		if(useIPTables) new RuntimeExec("iptables -D INPUT -s " + address + " -j DROP");
	}

	protected void unbanIP(String clientAddress) {
		(new Thread(() -> {
			try {
				Thread.sleep(1000 * 60 * 2);
				ipTable(clientAddress, false);
			} catch (Exception exception) {
			}
		})).start();
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

	public void kickPlayer(final Player player, final String reason) {
		try {
			final String kickMessage = instance.getKickMessage(reason);
			final net.minecraft.server.v1_8_R3.IChatBaseComponent textComponent = new net.minecraft.server.v1_8_R3.ChatComponentText(
					kickMessage);
			final net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect packet = new net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect(
					textComponent);
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection
					.sendPacket(packet);
			return;
		} catch (Exception | NoClassDefFoundError error) {
		}
	}

	public void customKickPlayer(final Player player, final String kickMessage) {
		try {
			final net.minecraft.server.v1_8_R3.IChatBaseComponent textComponent = new net.minecraft.server.v1_8_R3.ChatComponentText(
					kickMessage);
			final net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect packet = new net.minecraft.server.v1_8_R3.PacketPlayOutKickDisconnect(
					textComponent);
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection
					.sendPacket(packet);
			return;
		} catch (Exception | NoClassDefFoundError error) {
		}
	}

	public void disconnectChannel(Channel channel) {
		channel.flush();
		channel.close();
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
			final UnsecuredJsonReader unsecuredJsonReader = new UnsecuredJsonReader(
					"https://blackbox.ipinfo.app/lookup/" + clientAddress);
			final Boolean vpn = (Boolean) unsecuredJsonReader.get().equalsIgnoreCase("Y");
			return vpn;
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

	public Attack getServerAttack() {
		return serverAttack;
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
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == ':' || c == ',' || c == '_' || c == '.'
					|| c == '\u00b3' || c == '\u00b2' || c == '\'' || c == '*' || c == '+' || c == '~' || c == '-'
					|| c == '|' || c == '>' || c == '<' || c == '^' || c == '?' || c == '=' || c == ')' || c == '('
					|| c == '%' || c == '$' || c == '\"' || c == '!' || c == '&' || c == ' ' || c == '/'
					|| c == '\u00fc' || c == '\u00f6' || c == '\u00e4' || c == '_' || c == '1' || c == '2' || c == '3'
					|| c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0' || c == '#' || c == 'ß' || c == '\'' || c == ';')
				continue;
			return "" + c;
		}
		return "empty";
	}

	public Boolean hasOnlyLettersBoolean(String text) {
		for (char c : text.toCharArray()) {
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == ':' || c == ',' || c == '_' || c == '.'
					|| c == '\u00b3' || c == '\u00b2' || c == '\'' || c == '*' || c == '+' || c == '~' || c == '-'
					|| c == '|' || c == '>' || c == '<' || c == '^' || c == '?' || c == '=' || c == ')' || c == '('
					|| c == '%' || c == '$' || c == '\"' || c == '!' || c == '&' || c == ' ' || c == '/'
					|| c == '\u00fc' || c == '\u00f6' || c == '\u00e4' || c == '_' || c == '1' || c == '2' || c == '3'
					|| c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || c == '0' || c == '#' || c == 'ß' || c == '\'' || c == ';')
				continue;
			return false;
		}
		return true;
	}

	public Boolean hasInvalidTag(final ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}

		final NBTTagCompound tagCompound = itemStack.getTag();
		final Item item = itemStack.getItem();
		if (tagCompound == null) {
			return false;
		}

		if (item instanceof ItemWrittenBook || item instanceof ItemBookAndQuill) {
			if (String.valueOf(tagCompound.get("pages")).length() > 500) {
				tagCompound.remove("pages");
				tagCompound.remove("author");
				tagCompound.remove("title");
				itemStack.setTag(new NBTTagCompound());
				return true;
			}
			if (tagCompound.hasKey("author") && tagCompound.getString("author").length() > 20) {
				tagCompound.remove("pages");
				tagCompound.remove("author");
				tagCompound.remove("title");
				itemStack.setTag(new NBTTagCompound());
				return true;
			}
		}
		if (item instanceof ItemFireworks && tagCompound.toString().length() > 300) {
			return true;
		}
		if (item instanceof ItemFireworksCharge && tagCompound.toString().length() > 800) {
			return true;
		}
		final Set<String> keys = tagCompound.c();
		if (keys.size() > 20) {
			return true;
		}
		if (tagCompound.hasKey("pages")) {
			final NBTTagList pages = tagCompound.getList("pages", 8);
			if (pages.size() > 50) {
				tagCompound.remove("pages");
				tagCompound.remove("author");
				tagCompound.remove("title");
				itemStack.setTag(new NBTTagCompound());
				return true;
			}

			int similarPages = 0;
			String lastPage = "";
			for (int i = 0; i < pages.size(); ++i) {
				final String page = pages.getString(i);
				if (page.contains(
						"wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5")
						|| page.equalsIgnoreCase(
								"wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5")) {
					itemStack.setTag(new NBTTagCompound());
					return true;
				}
				if (page.length() > 900) {
					tagCompound.remove("pages");
					tagCompound.remove("author");
					tagCompound.remove("title");
					itemStack.setTag(new NBTTagCompound());
					return true;
				}
				if (page.split("extra").length > 8) {
					return true;
				}
				if (lastPage.equals(page)) {
					++similarPages;
				}
				lastPage = page;
				if (similarPages > 4) {
					tagCompound.remove("pages");
					tagCompound.remove("author");
					tagCompound.remove("title");
					itemStack.setTag(new NBTTagCompound());
					return true;
				}
				final String strippedPage = ChatColor.stripColor(page.replaceAll("\\+", ""));
				if (strippedPage == null || strippedPage.equals("null")) {
					return true;
				}
				if (strippedPage.length() > 256) {
					tagCompound.remove("pages");
					tagCompound.remove("author");
					tagCompound.remove("title");
					itemStack.setTag(new NBTTagCompound());
					return true;
				}
				if (15 > 0) {
					int tooBigChars = 0;
					for (int charI = 0; charI < page.length(); ++charI) {
						final char current = page.charAt(charI);
						if (String.valueOf(current).getBytes().length > 1 && ++tooBigChars > 15) {
							return true;
						}
					}
				}
			}

		}
		final String name = item.getName().toLowerCase();
		if (!name.contains("chest") && !name.contains("hopper") && !name.contains("shulker")) {
			final int length = String.valueOf(tagCompound).getBytes(StandardCharsets.UTF_8).length;
			if (length > 10000) {
				return true;
			}
		}
		if (tagCompound.hasKey("SkullOwner")) {
			final NBTTagCompound skullOwner = tagCompound.getCompound("SkullOwner");
			if (skullOwner.hasKey("Properties")) {
				final NBTTagCompound properties = skullOwner.getCompound("Properties");
				if (properties.hasKey("textures")) {
					final NBTTagList textures = properties.getList("textures", 10);
					for (int i = 0; i < textures.size(); ++i) {
						final NBTTagCompound entry = textures.get(i);
						if (entry.hasKey("Value")) {
							final String b64 = entry.getString("Value");
							String decoded;
							try {
								decoded = new String(Base64.getDecoder().decode(b64));
							} catch (IllegalArgumentException e) {
								break;
							}

							decoded = decoded.trim().replace(" ", "").replace("\"", "").toLowerCase();
							final Matcher matcher = pattern.matcher(decoded);
							while (matcher.find()) {
								final String url = decoded.substring(matcher.end() + 1);
								if (!url.startsWith("http://textures.minecraft.net")
										&& !url.startsWith("https://textures.minecraft.net")) {
									return true;
								}
							}
						}
					}
				}
			}
		}

		int listsAmount = 0;
		for (final String key : keys) {
			if (tagCompound.hasKeyOfType(key, 9)) {
				listsAmount = listsAmount + 1;
				if (++listsAmount > 10) {
					return true;
				}
				final NBTTagList list = tagCompound.getList(key, 8);
				final int size = list.size();
				if (size > 50) {
					tagCompound.remove(key);
					return true;
				}
				for (int i = 0; i < list.size(); ++i) {
					final String content = list.getString(i);
					// Fragt ab ob der Content der Seite null ist
					if (content == null || content.equalsIgnoreCase("null")) {
						return true;
					}
					// Fragt ab ob der Buch Content zu groß ist
					if (content.length() > 90) {
						return true;
					}
				}
			}
			if (tagCompound.hasKeyOfType(key, 11)) {
				if (tagCompound.getIntArray(key).length > 50) {
					return true;
				}
				continue;
			}

		}
		return false;
	}
}