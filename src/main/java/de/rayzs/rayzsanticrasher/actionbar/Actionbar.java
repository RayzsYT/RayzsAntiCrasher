package de.rayzs.rayzsanticrasher.actionbar;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class Actionbar {

	public Actionbar(String text) {
		Bukkit.getOnlinePlayers().forEach(players -> sendActionbar(players, text));
	}

	public Actionbar(Player player, String text) {
		sendActionbar(player, text);
	}

	public Actionbar(String text, String permission) {
		for (Player players : Bukkit.getOnlinePlayers())
			if (players.hasPermission(permission))
				sendActionbar(players, text);
	}

	private <T> void sendActionbar(Player player, String text) {
		CraftPlayer craftplayer = (CraftPlayer) player;
		IChatBaseComponent ichatbasecomponent = ChatSerializer.a("{\"text\": \"" + text + "\"}");
		PacketPlayOutChat packetplayoutchat = new PacketPlayOutChat(ichatbasecomponent, (byte) 2);
		craftplayer.getHandle().playerConnection.sendPacket(packetplayoutchat);
	}
}
