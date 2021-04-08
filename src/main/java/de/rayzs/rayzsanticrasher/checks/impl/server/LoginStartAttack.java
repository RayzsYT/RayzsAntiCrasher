package de.rayzs.rayzsanticrasher.checks.impl.server;

import com.mojang.authlib.GameProfile;

import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.checks.meth.Attack;
import de.rayzs.rayzsanticrasher.checks.meth.LiveAttackCounter;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;

public class LoginStartAttack extends ServerCheck {

	private Boolean liveActionbar, checkWaitingPlayers;
	private Integer connectionsAllowed, maxConnections, maxSingleConnections;
	
	public LoginStartAttack() {
		checkWaitingPlayers = Boolean.parseBoolean(getFileManager("checkWaitingPlayers", this).getString("true"));
		liveActionbar = Boolean.parseBoolean(getFileManager("liveActionbar", this).getString("false"));
		connectionsAllowed = getFileManager("maxAllowedConnections", this).getInt(8);
		maxConnections = getFileManager("maxConnections", this).getInt(10);
		maxSingleConnections = getFileManager("maxSingleConnections", this).getInt(10);
	}

	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet) {
		if (!(packet instanceof PacketLoginInStart))
			return false;
		
		PacketLoginInStart loginInStartPacket = (PacketLoginInStart) packet;
		if (loginInStartPacket.a() == null) {
			getAPI().disconnectChannel(channel);
		}

		final GameProfile gameProfile = loginInStartPacket.a();

		if (gameProfile.getName().length() >= 17 || gameProfile.getName().length() <= 1) {
			getAPI().disconnectChannel(channel);
		}
		
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
					if (!attack.isWaiting(clientAddress)) attack.addWaiting(clientAddress);

					channel.flush();
					channel.close();
				}
		}catch (Exception error) { }
		
		return false;
	}

	private void onAttack(Attack attack, Integer saveAmount) {
		if (attack.isUnderAttack())
			return;
		if (getInstance().useLiveAttackCounter() && liveActionbar)
			new LiveAttackCounter(attack, 1000);
		attack.setState(true);
		(new Thread(() -> {
			while (attack.isUnderAttack()) {
				if (attack.getConnections() <= saveAmount) {
					attack.setState(false);
					return;
				}
				try {
					if (!attack.getBlacklist().isEmpty()) {
						for (String currentAddress : attack.getBlacklist()) {
							attack.ipTable(currentAddress, true);
							attack.removeBlacklist(currentAddress);
						}
					}
				} catch (Exception error) {
				}
				if (checkWaitingPlayers)
					try {
						if (!attack.getWaitinglist().isEmpty()) {
							try {
								for (String currentAddress : attack.getWaitinglist()) {
									if (!getAPI().isProxy(currentAddress)) {
										attack.addWhitelist(currentAddress);
										attack.removeWaiting(currentAddress);
										continue;
									}
									attack.ipTable(currentAddress, true);
									attack.removeWaiting(currentAddress);
								}
							} catch (Exception | OutOfMemoryError error) { }
						}
					} catch (Exception error) { }
				try {
					Thread.sleep(1000);
				} catch (InterruptedException error) {
				}
			}
		})).start();
	}
}