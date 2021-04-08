package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInChat;

public class Chat extends ClientCheck {

	private Integer max;

	public Chat() {
		max = getFileManager("max", this).getInt(50);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInChat))
			return false;
		try {
			PacketPlayInChat pa = (PacketPlayInChat) packet;
			String input = pa.a();
			if (input == null || input == "") {
				getAPI().kickPlayer(player, "Sending an empty message");
				return true;
			}
			if (amount > max) {
				getAPI().kickPlayer(player, "Sending to much chat packets");
				return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}