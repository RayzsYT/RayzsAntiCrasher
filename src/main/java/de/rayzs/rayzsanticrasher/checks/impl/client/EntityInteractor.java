package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

public class EntityInteractor extends ClientCheck {

	private Integer max;

	public EntityInteractor() {
		max = getFileManager("max", this).getInt(150);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInUseEntity))
			return false;
		if (amount > max) {
			getAPI().kickPlayer(player, "Too fast interacting with entity");
			return true;
		}
		return false;
	}
}