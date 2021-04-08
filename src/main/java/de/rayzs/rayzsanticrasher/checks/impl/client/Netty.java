package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;

public class Netty extends ClientCheck {

	private Integer max, nbttagLenght;
	private Boolean bookCheck, invalidItemCheck;

	public Netty() {
		max = getFileManager("max", this).getInt(100);
		nbttagLenght = getFileManager("nbttagLenght", this).getInt(5000);
		bookCheck = Boolean.parseBoolean(getFileManager("bookCheck", this).getString("true"));
		invalidItemCheck = Boolean.parseBoolean(getFileManager("invalidItemCheck", this).getString("true"));
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInBlockPlace))
			return false;
		try {
			
			if(amount > max) {
				getAPI().kickPlayer(player, "Sending too many blockplace packets");
				return true;
			}
			
			PacketPlayInBlockPlace blockPlace = (PacketPlayInBlockPlace) packet;
			net.minecraft.server.v1_8_R3.ItemStack stack = blockPlace.getItemStack();
			if(stack == null) return false;
			if (stack.getTag() == null) return false;
			if (stack.getTag().toString().length() > nbttagLenght) {
				getAPI().kickPlayer(player, "Placing a item with too an big nbttag");
				return true;	
			}
			CraftItemStack craftStack = CraftItemStack.asNewCraftStack(stack.getItem());
			if (bookCheck)
				if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType().equals(Material.BOOK_AND_QUILL)) {
					getAPI().kickPlayer(player, "Using a book without holding ones");
					return true;
				}
			if (invalidItemCheck) {
				if (getAPI().hasInvalidTag(stack.getTag()) && craftStack.getType() != player.getItemInHand().getType()) {
					getAPI().kickPlayer(player, "Using a item without holding it");
					return true;
				}
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}