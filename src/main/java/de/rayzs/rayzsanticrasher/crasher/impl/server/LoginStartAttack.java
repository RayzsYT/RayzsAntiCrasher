package de.rayzs.rayzsanticrasher.crasher.impl.server;

import java.util.List;
import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.crasher.meth.Attack;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;

public class LoginStartAttack extends ServerCheck {

	Integer connectionsAllowed, maxConnections, maxSingleConnections;
	
	public LoginStartAttack() {
		connectionsAllowed = getFileManager("maxAllowedConnections", this).getInt(8);
		maxConnections = getFileManager("maxConnections", this).getInt(10);
		maxSingleConnections = getFileManager("maxSingleConnections", this).getInt(10);
	}
	
	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		if (!(packet instanceof PacketLoginInStart))
			return false;
		try {
			Attack attack = getAPI().getLoginAttack();
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
				if (totalConnections >= maxConnections) {
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
					attack.addWaiting(clientAddress);
					channel.flush();
					channel.close();
				}
		} catch (Exception error) {
			return false;
		}
		return false;
	}

	private void onAttack(Attack attack, Integer saveAmount) {
		attack.setState(true, true);
		(new Thread(() -> {
			while (attack.isUnderAttack()) {
				if (attack.getConnections() <= saveAmount) {
					attack.setState(false, true);
					if (getAPI().doIPTable()) {
						List<String> tempBlockedIPs = attack.getBlacklist();
						for (String currentIP : tempBlockedIPs) {
							attack.ipTable(currentIP, true);
							attack.removeBlacklist(currentIP);
						}
						List<String> tempWaitingIPs = attack.getWaitinglist();
						for (String currentIP : tempWaitingIPs) {
							if (!getAPI().isVPN(currentIP))
								continue;
							attack.ipTable(currentIP, true);
							attack.removeWaiting(currentIP);
						}
					}
					return;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException error) {
				}
			}
		})).start();
	}
}