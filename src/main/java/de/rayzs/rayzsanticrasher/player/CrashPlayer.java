package de.rayzs.rayzsanticrasher.player;

import java.util.HashMap;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import de.rayzs.rayzsanticrasher.checks.Crasher;
import de.rayzs.rayzsanticrasher.checks.impl.server.ByteBufReader;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;

public class CrashPlayer {

	private Player player;
	private CraftPlayer craftplayer;
	private HashMap<String, Integer> packetHash;
	private Channel channel;

	public CrashPlayer(Player player) {
		this.player = player;
		this.craftplayer = (CraftPlayer) player;
		this.packetHash = new HashMap<>();
		final Channel channel = craftplayer.getHandle().playerConnection.networkManager.channel;
		if (channel.pipeline().get("decompress") != null)
			channel.pipeline().addAfter("decompress", "cf_decompress", new ByteBufReader(craftplayer));
		else
			channel.pipeline().addAfter("splitter", "cf_decompress", new ByteBufReader(craftplayer));
		channel.pipeline().addBefore("packet_handler", "invalid_packet_handler", new ByteBufReader(craftplayer));
	}

	public void addPacket(Packet<?> pack, String packet) {
		Integer result = 1;
		if (exist(packet))
			result = (getPacket(packet)) + (1);
		this.packetHash.put(packet, result);
		new Crasher(player, channel, packet, pack);
		new Crasher(player, channel, packet, pack, result);
	}

	public void clear() {
		this.packetHash = new HashMap<>();
	}
	
	public void uninject() {
		channel.pipeline().remove("cf_decompress");
		channel.pipeline().remove("invalid_packet_handler");
	}

	public Player getPlayer() {
		return this.player;
	}

	public CraftPlayer getCraftPlayer() {
		return this.craftplayer;
	}

	public Boolean exist(String packet) {
		return (packetHash.get(packet) != null);
	}

	public Boolean equals(String packet, String equalsWith) {
		return (packet.equals(equalsWith));
	}

	public Integer getPacket(String packet) {
		return packetHash.get(packet);
	}

	public HashMap<?, ?> getHashMap() {
		return packetHash;
	}
}