package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;

public class TabComplete extends ClientCheck {

	private Integer max;

	public TabComplete() {
		max = getFileManager("max", this).getInt(60);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInTabComplete))
			return false;
		try {
			if (amount > max) {
				getAPI().kickPlayer(player, "Sending an too much tabcomplete packets");
				return true;
			}
			PacketPlayInTabComplete tabComplete = (PacketPlayInTabComplete) packet;
			String input = tabComplete.a();
			if (input == null) {
				getAPI().kickPlayer(player, "Sending an invalid tabcomplete packet");
				return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}