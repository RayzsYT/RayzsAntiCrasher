package de.rayzs.rayzsanticrasher.crasher.impl.client;

import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;

public class Other extends ClientCheck {

	private Integer filteredMax, unfilteredMax;
	private List<String> packetList;

	public Other() {
		packetList = Arrays.asList(
				"PacketPlayInSetCreativeSlot", 
				"PacketPlayInArmAnimation", 
				"PacketPlayInSettings", 
				"PacketPlayInChat", 
				"PacketPlayInClientCommand", 
				"PacketPlayInSpectate", 
				"PacketPlayInChat", 
				"PacketPlayInBlockDig", 
				"PacketPlayInWindowClick", 
				"PacketPlayInUseEntity", 
				"PacketPlayInSettings");
		filteredMax = getFileManager("filteredMax", this).getInt(100);
		unfilteredMax = getFileManager("unfilteredMax", this).getInt(60);
	}

	@Override
	public boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet, Integer amount) {
		try {
			if (packetList.contains(packetName))
				return false;
			if (packetName.equals("PacketPlayInFlying$PacketPlayInPositionLook")
					|| packetName.equals("PacketPlayInFlying")
					|| packetName.equals("PacketPlayInFlying$PacketPlayInPosition")) {
				if (amount > filteredMax) {
					getAPI().kickPlayer(player, "Sending too much " + packetName + " packets");
					return true;
				}
				return false;
			}
			if (amount > unfilteredMax) {
				getAPI().kickPlayer(player, "Sending too much " + packetName + " packets");
				return true;
			}
		} catch (Exception error) { if(getInstance().useDebug()) error.printStackTrace(); }
		return false;
	}
}