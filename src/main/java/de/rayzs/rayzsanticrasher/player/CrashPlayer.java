package de.rayzs.rayzsanticrasher.player;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import de.rayzs.rayzsanticrasher.checks.impl.server.ByteBufReader;
import io.netty.channel.Channel;

public class CrashPlayer {

	private Player player;
	private CraftPlayer craftplayer;
	private Channel channel;

	public CrashPlayer(Player player) {
		this.player = player;
		this.craftplayer = (CraftPlayer) player;
		channel = craftplayer.getHandle().playerConnection.networkManager.channel;
		if (channel.pipeline().get("decompress") != null)
			channel.pipeline().addAfter("decompress", "cf_decompress", new ByteBufReader(craftplayer));
		else
			channel.pipeline().addAfter("splitter", "cf_decompress", new ByteBufReader(craftplayer));
		channel.pipeline().addBefore("packet_handler", "invalid_packet_handler", new ByteBufReader(craftplayer));
	}

	public Player getPlayer() {
		return this.player;
	}

	public CraftPlayer getCraftPlayer() {
		return this.craftplayer;
	}

	public Boolean equals(String packet, String equalsWith) {
		return (packet.equals(equalsWith));
	}
}