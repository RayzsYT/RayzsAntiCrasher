package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInSetCreativeSlot;

public class CreativeSlot extends ClientCheck {

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInSetCreativeSlot))
			return false;
		try {
			if (player.getGameMode() != GameMode.CREATIVE)
				return true;
			if (amount > 3500)
				return true;
			PacketPlayInSetCreativeSlot creativeSlot = (PacketPlayInSetCreativeSlot) packet;
			Integer slot = creativeSlot.a();
			ItemStack itemstack = creativeSlot.getItemStack();
			if (!(slot instanceof Integer) || slot == null)
				return true;
			if (slot > 100)
				return true;
			if (getAPI().hasInvalidTag(itemstack.getTag()))
				player.getInventory().removeItem(player.getItemInHand());
		} catch (Exception error) { }
		return false;
	}
}