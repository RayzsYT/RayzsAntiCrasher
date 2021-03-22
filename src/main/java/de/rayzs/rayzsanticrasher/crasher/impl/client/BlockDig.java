package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;

public class BlockDig extends ClientCheck {

	private Integer max;

	public BlockDig() {
		max = getFileManager("max", this).getInt(50);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInBlockDig))
			return false;
		try {
			if (amount > max)
				return true;
			PacketPlayInBlockDig blockDig = (PacketPlayInBlockDig) packet;
			if (blockDig.a() == null || blockDig.b() == null || blockDig.c() == null)
				return true;
		} catch (Exception error) { }
		return false;
	}
}