package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInAbilities;

public class FlyFaker extends ClientCheck {

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInAbilities))
			return false;
		try {
			PacketPlayInAbilities inAbilities = (PacketPlayInAbilities) packet;
			Boolean isFlying = inAbilities.isFlying();
			if (isFlying == true && player.getGameMode() != GameMode.CREATIVE) {
				if (!player.getAllowFlight()) {
					return true;
				}
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}