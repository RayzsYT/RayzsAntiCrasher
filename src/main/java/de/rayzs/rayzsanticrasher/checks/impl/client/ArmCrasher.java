package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
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
		if (amount > max) {
			getAPI().kickPlayer(player, "Sending to much armanimation packets");
			return true;
		}
		return false;
	}
}