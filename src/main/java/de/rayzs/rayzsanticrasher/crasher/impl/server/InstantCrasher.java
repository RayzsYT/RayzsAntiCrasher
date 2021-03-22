package de.rayzs.rayzsanticrasher.crasher.impl.server;

import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketHandshakingInSetProtocol;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;

public class InstantCrasher extends ServerCheck {

	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		try {
			if (channel.remoteAddress() == null) {
				channel.flush();
				channel.close();
			}
		} catch (Exception error) {
		}
		try {
			if (packet instanceof PacketHandshakingInSetProtocol) {
				PacketHandshakingInSetProtocol inSetProtocol = (PacketHandshakingInSetProtocol) packet;
				Integer protocolVersion = inSetProtocol.b();
				if (protocolVersion >= 760 || protocolVersion < 1) {
					channel.flush();
					channel.close();
					return false;
				}
			}
		} catch (Exception error) { }
		if (packet instanceof PacketLoginInStart) {
			try {
				PacketLoginInStart inStart = (PacketLoginInStart) packet;
				if (inStart.a().getName() == null) {
					channel.flush();
					channel.close();
					getAPI().ipTable(channel.remoteAddress().toString(), true);
					return false;
				}
				if (inStart.a().getProperties() == null) {
					channel.flush();
					channel.close();
					getAPI().ipTable(channel.remoteAddress().toString(), true);
					return false;
				}
				String name = inStart.a().getName();
				if (name.length() > 16) {
					channel.flush();
					channel.close();
					getAPI().ipTable(channel.remoteAddress().toString(), true);
					return false;
				}
				String onlyLetters = RayzsAntiCrasher.getAPI().hasOnlyLetters(name);
				if (!onlyLetters.equals("empty")) {
					channel.flush();
					channel.close();
					getAPI().ipTable(channel.remoteAddress().toString(), true);
					return false;
				}
			} catch (Exception error) { }
		}

		return false;
	}
}