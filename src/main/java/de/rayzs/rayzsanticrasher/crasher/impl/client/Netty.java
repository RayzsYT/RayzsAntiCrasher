package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.Material;
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
		bookCheck = Boolean.parseBoolean(getFileManager("bookCheck", this).getString("true"));
		invalidItemCheck = Boolean.parseBoolean(getFileManager("invalidItemCheck", this).getString("true"));
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInBlockPlace))
			return false;
		try {
			PacketPlayInBlockPlace blockPlace = (PacketPlayInBlockPlace) packet;
			net.minecraft.server.v1_8_R3.ItemStack stack = blockPlace.getItemStack();
			if(stack == null) return false;
			if (stack.getTag() == null) return false;
			if (stack.getTag().toString().length() > max) {
				channel.close();
				return true;	
			}
			CraftItemStack craftStack = CraftItemStack.asNewCraftStack(stack.getItem());
			if (bookCheck)
				if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType().equals(Material.BOOK_AND_QUILL)) {
					channel.close();
					return true;
				}
			if (invalidItemCheck) {
				if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType() != player.getItemInHand().getType()) {
					channel.close();
					return true;
				}
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}