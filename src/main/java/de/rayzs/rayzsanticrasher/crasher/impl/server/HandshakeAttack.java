package de.rayzs.rayzsanticrasher.crasher.impl.server;

import org.bukkit.Bukkit;
import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.crasher.meth.Attack;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketHandshakingInSetProtocol;

public class HandshakeAttack extends ServerCheck {

	Integer connectionsAllowed, maxConnections, maxSingleConnections;
	
	public HandshakeAttack() {
		connectionsAllowed = getFileManager("maxAllowedConnections", this).getInt(8);
		maxConnections = getFileManager("maxConnections", this).getInt(15);
		maxSingleConnections = getFileManager("maxSingleConnections", this).getInt(10);
	}
	
	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		if (!(packet instanceof PacketHandshakingInSetProtocol))
			return false;
		try {
			Attack attack = getAPI().getHandshakeAttack();
			String clientAddress = channel.remoteAddress().toString().split(":")[0].replace("/", "");
			Integer clientConnections = attack.getConnections(clientAddress);
			Integer totalConnections = attack.getConnections();

			if (attack.isBlacklisted(clientAddress)) {
				channel.flush();
				channel.close();
				return false;
			}

			attack.addConnection(clientAddress);
			attack.addConnection();

			if (!attack.isUnderAttack()) {
				if (totalConnections >= totalConnections) {
					onAttack(attack, connectionsAllowed);
					return false;
				}
			}

			if (clientConnections >= maxSingleConnections) {
				attack.addBlacklist(clientAddress);
				getAPI().ipTable(clientAddress, true);
				channel.flush();
				channel.close();
			}

			if (attack.isUnderAttack())
				if (!attack.isWhitelisted(clientAddress)) {
					Bukkit.getScheduler().runTaskAsynchronously(getInstance(), new Runnable() {
						@Override
						public void run() {
							if (getAPI().isVPN(clientAddress)) {
								attack.addBlacklist(clientAddress);
								getAPI().ipTable(clientAddress, true);
							}
						}
					});
					channel.flush();
					channel.close();
				}
		} catch (Exception error) { }
		return false;
	}

	private void onAttack(Attack attack, Integer saveAmount) {
		attack.setState(true, true);
		(new Thread(() -> {
			while (attack.isUnderAttack()) {
				if (attack.getConnections() <= saveAmount) {
					attack.setState(false, true);
					for (String currentIP : attack.getBlacklist()) {
						attack.ipTable(currentIP, true);
						attack.removeBlacklist(currentIP);
					}
					return;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException error) { }
			}
		})).start();
	}
}