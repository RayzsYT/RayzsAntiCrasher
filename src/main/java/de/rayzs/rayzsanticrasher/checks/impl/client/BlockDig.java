package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;

public class BlockDig extends ClientCheck {

	private Integer max;

	public BlockDig() {
		max = getFileManager("max", this).getInt(100);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInBlockDig))
			return false;
		try {
			if (amount > max) {
				getAPI().kickPlayer(player, "Sending to much blockdig packets");
				return true;
			}
			PacketPlayInBlockDig blockDig = (PacketPlayInBlockDig) packet;
			if (blockDig.a() == null || blockDig.b() == null || blockDig.c() == null) {
				getAPI().kickPlayer(player, "Breaking block with invalid data");
				return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}