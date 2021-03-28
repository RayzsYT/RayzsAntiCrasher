package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUpdateSign;

public class SignUpdater extends ClientCheck {

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInUpdateSign))
			return false;
		PacketPlayInUpdateSign playInUpdateSignPacket  = (PacketPlayInUpdateSign) packet;
		int x = playInUpdateSignPacket.a().getX();
		int y = playInUpdateSignPacket.a().getY();
		int z = playInUpdateSignPacket.a().getZ();
		World world = player.getWorld();
		Block block = world.getBlockAt(new Location(world, x, y, z));
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			getAPI().kickPlayer(player, "Editing a sign with too big distance");
			return true;
		}
		return false;
	}
}