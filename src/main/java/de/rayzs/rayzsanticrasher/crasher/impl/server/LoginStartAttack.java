package de.rayzs.rayzsanticrasher.crasher.impl.server;

import org.bukkit.Bukkit;

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
		}
		return false;
	}

	private void onAttack(Attack attack, Integer saveAmount) {
		attack.setState(true, true);
		(new Thread(() -> {
			while (attack.isUnderAttack()) {
				Bukkit.getScheduler().runTaskAsynchronously(getInstance(), new Runnable() {
					@Override
					public void run() {
						for (String currentIP : attack.getWaitinglist())
							if (getAPI().isVPN(currentIP)) {
								attack.ipTable(currentIP, true);
								attack.removeWaiting(currentIP);
							}
					}
				});
				if (attack.getConnections() <= saveAmount) {
					attack.setState(false, true);
					return;
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException error) { }
			}
		})).start();
	}
}