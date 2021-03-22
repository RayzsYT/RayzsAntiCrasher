package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;

public class TabComplete extends ClientCheck {

	private Integer max;

	public TabComplete() {
		max = getFileManager("max", this).getInt(20);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInTabComplete))
			return false;
		try {
			if (amount > max)
				return true;
			PacketPlayInTabComplete tabComplete = (PacketPlayInTabComplete) packet;
			String input = tabComplete.a();
			if (input == null)
				return true;
		} catch (Exception error) { }
		return false;
	}
}