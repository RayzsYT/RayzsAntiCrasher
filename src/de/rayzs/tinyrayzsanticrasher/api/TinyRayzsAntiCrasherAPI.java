package de.rayzs.tinyrayzsanticrasher.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.rayzs.tinyrayzsanticrasher.enums.LanguageEnum;
import de.rayzs.tinyrayzsanticrasher.enums.MessageEnum;
import de.rayzs.tinyrayzsanticrasher.enums.ReflectionType;
import de.rayzs.tinyrayzsanticrasher.injection.CrashPlayer;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class TinyRayzsAntiCrasherAPI {
	
	private TinyRayzsAntiCrasher instance;
	
	private List<Channel> channelList;
	private HashMap<Player, Channel> channelPlayerHash;
	private HashMap<Channel, Player> playerChannelHash;
	private HashMap<Player, CrashPlayer> crashPlayerHash;
	private HashMap<String, LanguageEnum> languageHash;
	private Boolean silentMode;
	private LanguageEnum consoleLanguage;
	private Integer serverPort;
	
	public TinyRayzsAntiCrasherAPI(TinyRayzsAntiCrasher instance, Boolean silentMode, LanguageEnum consoleLanguage) {
		this.instance = instance;
		this.channelList = new ArrayList<>(); 
		this.channelPlayerHash = new HashMap<>();
		this.playerChannelHash = new HashMap<>();
		this.languageHash = new HashMap<>();
		this.silentMode = silentMode;
		this.consoleLanguage = consoleLanguage;
		this.crashPlayerHash = new HashMap<>();
		this.serverPort = Bukkit.getServer().getPort();
	}
	
	public void createPlayer(final Player player) {
		if(hasPlayer(player)) return;
		final CrashPlayer crashPlayer = new CrashPlayer(instance, this, player);
		final Channel channel = crashPlayer.getChannel();
		if(!channelList.contains(channel)) channelList.add(channel);
		channelPlayerHash.put(player, channel);
		playerChannelHash.put(channel, player);
		crashPlayerHash.put(player, crashPlayer);
	}
	
	public void deletePlayer(final Player player) {
		if(!hasPlayer(player)) return;
		final CrashPlayer crashPlayer = getPlayer(player);
		final Channel channel = crashPlayer.getChannel();
		if(channelList.contains(channel)) channelList.remove(channel);
		channelPlayerHash.remove(player);
		playerChannelHash.remove(channel);
		crashPlayer.unregister();
		crashPlayerHash.remove(player);
	}
	
	public void setLanguage(final Player player, final LanguageEnum languageEnum) {
		if(hasLanguage(player)) return;
		final String uuid = player.getUniqueId().toString();
		languageHash.put(uuid, languageEnum);
	}
	
	public void deleteLanguage(final Player player) {
		if(!hasLanguage(player)) return;
		final String uuid = player.getUniqueId().toString();
		languageHash.remove(uuid);
	}
	
	public LanguageEnum getLanguage(final Player player) { 
		final String uuid = player.getUniqueId().toString();
		return languageHash.get(uuid);
	} 
	
	public CrashPlayer getPlayer(final Player player) {
		return crashPlayerHash.get(player);
	}
	
	public Integer getServerPort() { return serverPort; }
	
	public Boolean isPlayerChannel(final Channel channel) { return channelList.contains(channel); }
	
	public Boolean hasLanguage(final Player player) { 
		return (getLanguage(player) != null);
	}
	
	public Boolean hasPlayer(final Player player) {
		return (getPlayer(player) != null);
	}
	
	public CrashPlayer getCrashPlayer(final Player player) { return crashPlayerHash.get(player); }
	
	public Channel getChannelPerPlayer(final Player player) { return channelPlayerHash.get(player); }
	
	public Player getPlayerPerChannel(final Channel channel) { return playerChannelHash.get(channel); }
	
	
	
	public void punish(final Player player, final Channel channel) {
		kickPlayer(player);
		if(channel.isOpen()) {
			channel.close();
			if(silentMode) return;
			final String consoleMessage = instance.getLanguageManager().getMessage(consoleLanguage, MessageEnum.DETECTED_KICK_MESSAGE).replace("%PLAYER%", player.getName());
			Bukkit.getConsoleSender().sendMessage(consoleMessage);
			Bukkit.getOnlinePlayers().forEach(players -> {
				if(players.hasPermission("trac.notify") || players.isOp()) {
					final String message = instance.getLanguageManager().getMessage(getLanguage(player), MessageEnum.DETECTED_KICK_MESSAGE).replace("%PLAYER%", player.getName());
					players.sendMessage(message);
				}
			});
		}
	}
	
	public Boolean check(final ByteBuf byteBuf, final Channel channel) {		
		final Integer a = byteBuf.array().length;
		final Integer b = byteBuf.readableBytes();
		final Integer c = byteBuf.capacity();
		final Integer d = byteBuf.refCnt();
		return    channel == null ? true 
				: !channel.isActive() ? true
				: !channel.isOpen() ? true
				: !channel.isWritable() ? true
				: channel.remoteAddress() == null ? true
				: a < 0 ? true
				: a > 5000 ? true
				: c < 0 ? true
				: d < 1 ? true
				: c > 16000 ? true
				: b > 16000 ? true
				: false;
	}
	
	protected void kickPlayer(final Player player) {
		if(!silentMode)
			try {
				if(player == null) return;
				final LanguageEnum languageEnum = getLanguage(player);
				if(languageEnum == null) return;
				final String kickMessage = instance.getLanguageManager().getMessage(languageEnum, MessageEnum.DETECTED_KICK_REASON);
				final Object packet = instance.getReflection().getNMSClass("PacketPlayOutKickDisconnect", ReflectionType.MINECRAFT)
									  .getDeclaredConstructor(instance.getReflection().getNMSClass("IChatBaseComponent", ReflectionType.MINECRAFT))
									  .newInstance(instance.getReflection().getNMSClass("ChatComponentText", ReflectionType.MINECRAFT)
									  .getDeclaredConstructor(String.class)
									  .newInstance(kickMessage));
				instance.getReflection().sendPacket(player, packet);
			} catch (Exception exception) { exception.printStackTrace(); }
	}
}