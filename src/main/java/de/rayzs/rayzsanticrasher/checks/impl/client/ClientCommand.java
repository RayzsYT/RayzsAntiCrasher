package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;

public class ClientCommand extends ClientCheck {

	private Integer max;

	public ClientCommand() {
		max = getFileManager("max", this).getInt(120);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInClientCommand))
			return false;
		if (amount > max) {
			getAPI().kickPlayer(player, "Sending too much clientcommand packets (open inventory)");
			return true;
		}
		return false;
	}
}