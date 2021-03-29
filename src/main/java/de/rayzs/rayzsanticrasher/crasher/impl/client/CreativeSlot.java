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
			if (player.getGameMode() != GameMode.CREATIVE) {
				getAPI().kickPlayer(player, "Sending creativeslot packets without creative mode");
				return true;
			}
			if (amount > 3500) {
				getAPI().kickPlayer(player, "Too fast sending creativeslot packets");
				return true;
			}
			PacketPlayInSetCreativeSlot creativeSlot = (PacketPlayInSetCreativeSlot) packet;
			Integer slot = creativeSlot.a();
			ItemStack itemstack = creativeSlot.getItemStack();
			if(itemstack == null)
				return false;
			if (!(slot instanceof Integer) || slot == null) {
				getAPI().kickPlayer(player, "Interacting with an invalid creative slot");
				return true;
			}
			if (slot >= 100 || slot < -1) {
				getAPI().kickPlayer(player, "Using illegal slot");
				return true;
			}
			if(!itemstack.hasTag())
				return false;
			if(itemstack.getTag().toString().length() > 5000) {
				getAPI().kickPlayer(player, "Taking item with too big nbttag");
				return true;
			}
			if (getAPI().hasInvalidTag(itemstack.getTag())) {
				getAPI().kickPlayer(player, "Taking item with invalid nbttag");
				return true;
			}
		}catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}