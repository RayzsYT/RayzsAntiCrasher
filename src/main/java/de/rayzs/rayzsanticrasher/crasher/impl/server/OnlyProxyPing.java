
package de.rayzs.rayzsanticrasher.crasher.impl.server;

import org.bukkit.Bukkit;

import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketHandshakingInSetProtocol;

public class OnlyProxyPing extends ServerCheck {

	private Boolean enabled;
	private String serverAddress;

	public OnlyProxyPing() {
		enabled = Boolean.parseBoolean(getFileManager("enabled", this).getString("false"));
		serverAddress = getFileManager("serverAddress", this).getString("localhost:" + Bukkit.getServer().getPort());
	}

	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		if(!enabled)
			return false;
		if (!(packet instanceof PacketHandshakingInSetProtocol))
			return false;
		PacketHandshakingInSetProtocol handshakingInSetProtocol = (PacketHandshakingInSetProtocol) packet;

		final String[] serverArgs = serverAddress.toLowerCase().split(":");
		final String serverAddress = serverArgs[0];
		Integer serverPort = 25565;
		try {
			serverPort = Integer.parseInt(serverArgs[1]);
		} catch (Exception error) {
		}
		final String clientServerAddress = handshakingInSetProtocol.hostname;
		final Integer clientServerPort = handshakingInSetProtocol.port;
		final String fullOriginalServerAdress = serverAddress + ":" + serverPort;
		final String fullClientServerAdress = clientServerAddress + ":" + clientServerPort;
		if (!fullOriginalServerAdress.equals(fullClientServerAdress))
			getAPI().disconnectChannel(channel);
		return false;
	}
}