package de.rayzs.rayzsanticrasher.player;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.Crasher;
import de.rayzs.rayzsanticrasher.crasher.impl.server.InputOverflow;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;

public class CrashPlayer {

	private RayzsAntiCrasher instance;
	private Player player;
	private CraftPlayer craftplayer;
	private HashMap<String, Integer> packetHash;
	private Channel channel;
	private Integer task;

	public CrashPlayer(Player player) {
		this.instance = RayzsAntiCrasher.getInstance();
		this.player = player;
		this.craftplayer = (CraftPlayer) player;
		this.packetHash = new HashMap<>();
	}

	public void start() {
		startScheduler();
		inject();
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(task);
		uninject();
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

	@SuppressWarnings("deprecation")
	private void startScheduler() {
		task = Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new Runnable() {
			@Override
			public void run() {
				clear();
			}
		}, 15, 15);
	}

	private void inject() {
		try {
			channel = craftplayer.getHandle().playerConnection.networkManager.channel;
			channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
				@Override
				protected void decode(ChannelHandlerContext arg0, Packet<?> packet, List<Object> arg2)
						throws Exception {
					arg2.add(packet);
					readPackets(packet);
				}
			});

			if (channel.pipeline().get("decompress") != null)
				channel.pipeline().addAfter("decompress", "cf_decompress", new InputOverflow(craftplayer));
			else
				channel.pipeline().addAfter("splitter", "cf_decompress", new InputOverflow(craftplayer));
			;
		} catch (Exception error) { }
	}

	private void uninject() {
		try {
			if (channel.pipeline().get("PacketInjector") != null) {
				channel.pipeline().remove("cf_decompress");
				channel.pipeline().remove("PacketInjector");
			}
		} catch (Exception error) { }
	}

	private void readPackets(Packet<?> packet) {
		String multiPacklet = packet.toString();
		multiPacklet = multiPacklet.replace("net.minecraft.server.v1_8_R3.", "").split("@")[0];
		String[] packets = multiPacklet.split("$");
		for (String currentPacket : packets)
			addPacket(packet, currentPacket);
	}
}