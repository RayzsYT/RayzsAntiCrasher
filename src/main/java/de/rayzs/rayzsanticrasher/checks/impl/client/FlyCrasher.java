package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

public class FlyCrasher extends ClientCheck {

	private Integer max;

	public FlyCrasher() {
		max = getFileManager("max", this).getInt(120);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInFlying))
			return false;
		if (amount > max) {
			getAPI().kickPlayer(player, "Too fast by sending fly packets");
			return true;
		}
		return false;
	}
}