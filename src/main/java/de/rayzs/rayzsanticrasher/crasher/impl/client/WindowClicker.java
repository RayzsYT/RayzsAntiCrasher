package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;

public class WindowClicker extends ClientCheck {

	private Integer max;
	private Integer maxNBTLenght;

	public WindowClicker() {
		max = getFileManager("max", this).getInt(500);
		maxNBTLenght = getFileManager("maxNBTLenght", this).getInt(1000);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInWindowClick))
			return false;
		try {
			if (amount > max)
				return true;
			PacketPlayInWindowClick windowClick = (PacketPlayInWindowClick) packet;
			if (windowClick.e().getTag() != null)
				if (windowClick.e().getTag().toString().length() > maxNBTLenght)
					return true;
		} catch (Exception error) { }
		return false;
	}
}