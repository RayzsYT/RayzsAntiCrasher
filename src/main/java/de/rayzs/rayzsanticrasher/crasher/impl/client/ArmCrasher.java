package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;

public class ArmCrasher extends ClientCheck {

	private Integer max;

	public ArmCrasher() {
		max = getFileManager("max", this).getInt(200);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInArmAnimation))
			return false;
		if (amount > max)
			return true;
		return false;
	}
}