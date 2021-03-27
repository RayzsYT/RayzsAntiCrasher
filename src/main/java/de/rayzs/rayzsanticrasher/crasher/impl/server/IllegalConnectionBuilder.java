package de.rayzs.rayzsanticrasher.crasher.impl.server;

import com.mojang.authlib.GameProfile;
import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketHandshakingInSetProtocol;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;
import net.minecraft.server.v1_8_R3.PacketStatusInPing;

public class IllegalConnectionBuilder extends ServerCheck {

	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		
		if (channel == null) {
			getAPI().disconnectChannel(channel);
			return false;
		}

		if (!channel.isActive()) {
			getAPI().disconnectChannel(channel);
			return false;
		}
		
		if (!channel.isOpen()) {
			getAPI().disconnectChannel(channel);
			return false;
		}
		
		if (!channel.isRegistered()) {
			getAPI().disconnectChannel(channel);
			return false;
		}
		
		if (channel.remoteAddress() == null) {
			getAPI().disconnectChannel(channel);
			return false;
		}

		if (packet instanceof PacketHandshakingInSetProtocol) {
			PacketHandshakingInSetProtocol handshakingInSetProtocolPacket = (PacketHandshakingInSetProtocol) packet;
			if (handshakingInSetProtocolPacket.hostname == null) {
				getAPI().disconnectChannel(channel);
				return false;
			}
			if (!(handshakingInSetProtocolPacket.a() instanceof EnumProtocol)) {
				getAPI().disconnectChannel(channel);
				return false;
			}
			if (handshakingInSetProtocolPacket.b() > 800 || handshakingInSetProtocolPacket.b() < 1) {
				getAPI().disconnectChannel(channel);
				return false;
			}
		}

		if (packet instanceof PacketLoginInStart) {
			PacketLoginInStart loginInStartPacket = (PacketLoginInStart) packet;
			if (loginInStartPacket.a() == null) {
				getAPI().disconnectChannel(channel);
			}

			final GameProfile gameProfile = loginInStartPacket.a();
			
			if (gameProfile.getName().length() >= 17) {
				getAPI().disconnectChannel(channel);
				return false;
			}
		}

		if (packet instanceof PacketStatusInPing) {
			PacketStatusInPing statusInPingPacket = (PacketStatusInPing) packet;
			if(statusInPingPacket.a() > Integer.MAX_VALUE) {
				getAPI().disconnectChannel(channel);
				return false;
			}
		}
		return false;
	}
}