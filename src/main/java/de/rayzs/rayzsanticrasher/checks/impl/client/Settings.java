package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInSettings;

public class Settings extends ClientCheck {

	private Integer max;

	public Settings() {
		max = getFileManager("max", this).getInt(400);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInSettings))
			return false;
		try {
			PacketPlayInSettings settings = (PacketPlayInSettings) packet;
			if (settings.a() == null) {
				getAPI().kickPlayer(player, "Sending an invalid setting packet");
				return true;
			}
			if (amount > max) {
				getAPI().kickPlayer(player, "Sending too much setting packets");
				return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}