package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInCustomPayload;

public class CustomPayload extends ClientCheck {

	private Integer max;

	public CustomPayload() {
		max = getFileManager("max", this).getInt(10);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInCustomPayload))
			return false;
		try {
			PacketPlayInCustomPayload customPayload = (PacketPlayInCustomPayload) packet;
			String doBook = customPayload.a();
			if (doBook.equals("MC|BEdit") || doBook.equals("MC|BSign")) {
				if (!player.getItemInHand().getType().toString().contains("BOOK"))
					return true;
				if (amount > max)
					return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}