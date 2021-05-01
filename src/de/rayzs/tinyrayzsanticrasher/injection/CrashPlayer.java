package de.rayzs.tinyrayzsanticrasher.injection;

import org.bukkit.entity.Player;
import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.packet.PacketCounter;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import de.rayzs.tinyrayzsanticrasher.reader.PacketReader$1;
import de.rayzs.tinyrayzsanticrasher.reader.PacketReader$2;
import io.netty.channel.Channel;

public class CrashPlayer {

	private TinyRayzsAntiCrasher instance;
	private TinyRayzsAntiCrasherAPI api;
	private Player player;
	private Channel channel;
	private PacketCounter packetCounter;
	private Boolean isRegistered;

	public CrashPlayer(final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api, final Player player) {
		this.instance = instance;
		this.api = api;
		this.player = player;
		this.channel = instance.getReflection().getChannel(player);
		this.packetCounter = new PacketCounter();
		isRegistered = false;
	}

	public void register() {
		if(isRegistered) return;
		isRegistered = true;
		channel.pipeline().addBefore("packet_handler", "rac-handler", new PacketReader$1(player, instance, api, packetCounter));
		if (channel.pipeline().get("decompress") != null)
			channel.pipeline().addAfter("decompress", "rac-decompress", new PacketReader$2(player, instance, api));
		else channel.pipeline().addAfter("splitter", "rac-decompress", new PacketReader$2(player, instance, api));
	}

	public void unregister() {
		if(!isRegistered) return;
		isRegistered = false;
		if (channel.pipeline().get("packet_handler") != null)
			channel.pipeline().remove("rac-handler");
		if (channel.pipeline().get("decompress") != null)
			channel.pipeline().remove("decompress");
	}
	
	public Channel getChannel() { return this.channel; }
}