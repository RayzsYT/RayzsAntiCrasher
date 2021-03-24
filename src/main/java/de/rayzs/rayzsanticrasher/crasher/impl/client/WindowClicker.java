package de.rayzs.rayzsanticrasher.crasher.impl.client;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;

public class WindowClicker extends ClientCheck {

	private Integer max, maxNBTLenght;

	public WindowClicker() {
		max = getFileManager("max", this).getInt(500);
		maxNBTLenght = getFileManager("maxNBTLenght", this).getInt(1000);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInWindowClick))
			return false;
		try {
			if (amount > max) return true;
			PacketPlayInWindowClick windowClick = (PacketPlayInWindowClick) packet;
			net.minecraft.server.v1_8_R3.ItemStack stack = windowClick.e();
			if(stack == null) return false;
			if (stack.getTag() == null) return false;
			if (stack.getTag().toString().length() > maxNBTLenght) return true;
			CraftItemStack craftStack = CraftItemStack.asNewCraftStack(stack.getItem());
			if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType().equals(Material.BOOK_AND_QUILL)) return true;
			if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType() != player.getItemInHand().getType()) return true;
			
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}