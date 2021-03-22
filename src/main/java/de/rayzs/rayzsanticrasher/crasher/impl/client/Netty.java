package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;

public class Netty extends ClientCheck {

	private Integer max;
	private Boolean bookCheck;
	private Boolean invalidItemCheck;

	public Netty() {
		max = getFileManager("max", this).getInt(5000);
		bookCheck = getFileManager("bookCheck", this).getBoolean(true);
		invalidItemCheck = getFileManager("invalidItemCheck", this).getBoolean(true);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInBlockPlace))
			return false;
		try {
			PacketPlayInBlockPlace blockPlace = (PacketPlayInBlockPlace) packet;
			net.minecraft.server.v1_8_R3.ItemStack stack = blockPlace.getItemStack();
			if (stack.getTag() == null)
				return false;
			if (stack.getTag().toString().length() > max) {
				player.getInventory().removeItem(player.getItemInHand());
				return true;
			}
			CraftItemStack craftStack = CraftItemStack.asNewCraftStack(stack.getItem());
			if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType().toString().contains("BOOK") && bookCheck)
				return true;
			if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType() != player.getItemInHand().getType()
					&& invalidItemCheck)
				return true;
		} catch (Exception error) { }
		return false;
	}
}