package de.rayzs.rayzsanticrasher.checks.impl.client;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInCustomPayload;

public class CustomPayload extends ClientCheck {

	private Integer max;

	public CustomPayload() {
		max = getFileManager("max", this).getInt(10);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		if (!(packet instanceof PacketPlayInCustomPayload))
			return false;
		try {
			PacketPlayInCustomPayload customPayload = (PacketPlayInCustomPayload) packet;
			String input = customPayload.a();
						
			if(input.equals("MC|AdvCdm")) {
				if (amount > max) {
					getAPI().kickPlayer(player, "Too fast sending custompayloads");
					return true;
				}
				if(player.hasPermission("rac.interact.cmd") || player.isOp()) return false; 
				getAPI().kickPlayer(player, "Permission denied for using commandblocks");
				return true;
			}
			
			if (input.equals("MC|BEdit") || input.equals("MC|BSign") || input.equals("MC|BOpen")) {
				if (amount > max) {
					getAPI().kickPlayer(player, "Too fast sending custompayloads");
					return true;
				}
				if (player.getItemInHand().getType() != Material.BOOK_AND_QUILL) {
					getAPI().kickPlayer(player, "Editing / Signing book without holding ones");
					return true;
				}
			}
			
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}