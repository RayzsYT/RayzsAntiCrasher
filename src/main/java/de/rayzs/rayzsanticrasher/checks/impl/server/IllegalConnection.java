package de.rayzs.rayzsanticrasher.checks.impl.server;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.mojang.authlib.GameProfile;
import de.rayzs.rayzsanticrasher.actionbar.Actionbar;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.checks.meth.Attack;
import de.rayzs.rayzsanticrasher.packet.PacketCounter;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketHandshakingInSetProtocol;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;
import net.minecraft.server.v1_8_R3.PacketStatusInPing;

public class IllegalConnection extends ServerCheck {

	private Channel channel;
	private String clientAddress;
	private Attack attack;
	private PacketCounter packetCounter;
	private Integer size, bannedIPs, maxConnections, totalBlocked, serverPort, startIPTabling, endIPTabling;
	private List<String> blacklistNames;
	private List<String> whitelistedIPs;
	private Boolean strongAttack;

	public IllegalConnection() {
		getInstance().registerEvent(new PlayerLogin());
		attack = getAPI().getServerAttack();
		packetCounter = getAPI().getAttackPacketCounter();
		size = 0;
		bannedIPs = 0;
		totalBlocked = 0;
		serverPort = Bukkit.getServer().getPort();
		blacklistNames = getFileManager("", this).getYAML()
				.getStringList("settings.server." + this.getClass().getSimpleName().toLowerCase() + ".blackListNames");
		if (blacklistNames.isEmpty())
			getFileManager("", this).getYAML().set(
					"settings.server." + this.getClass().getSimpleName().toLowerCase() + ".blackListNames",
					Arrays.asList("dropbot", "mcdrop", "mcrage", "lauren", "mcspam"));

		whitelistedIPs = getFileManager("", this).getYAML()
				.getStringList("settings.server." + this.getClass().getSimpleName().toLowerCase() + ".whitelistedIPs");
		if (whitelistedIPs.isEmpty())
			getFileManager("", this).getYAML().set(
					"settings.server." + this.getClass().getSimpleName().toLowerCase() + ".whitelistedIPs",
					Arrays.asList("127.0.0.1"));
		attack.setWhitelist(whitelistedIPs);
		maxConnections = getFileManager("maxConnections", this).getInt(100);
		startIPTabling = getFileManager("startIPTabling", this).getInt(500);
		endIPTabling = getFileManager("endIPTabling", this).getInt(200);
		strongAttack = false;
	}

	@Override
	public boolean onCheck(Channel channel, String packetName, Packet<?> packet, Object object, Integer amount) {
		try {
			if (!(packet instanceof PacketLoginInStart || packet instanceof PacketLoginInStart
					|| packet instanceof PacketStatusInPing || packet instanceof PacketStatusInPing))
				return false;

			this.channel = channel;
			this.clientAddress = channel.remoteAddress().toString().split(":")[0].replace("/", "");
			
			if (!strongAttack && size >= startIPTabling)
				strongAttack = true;
			if (strongAttack && size <= endIPTabling)
				strongAttack = false;

			if (attack.isWhitelisted(clientAddress))
				return false;

			size = packetCounter.getPacketAmount("attackPacket :3");
			
			if (size > maxConnections && !attack.isUnderAttack())
				startAttack();
			packetCounter.addPacket("attackPacket :3");

			if (channel.remoteAddress() 
			!= null) checkBlock(packet);
			else punish();
			if (attack.isUnderAttack() && !attack.isWhitelisted(clientAddress))
				channel.close();

			if (attack.isUnderAttack())
				new Actionbar(getInstance().getLiveAttackMessage(attack, size, totalBlocked, bannedIPs),
						"rayzsanticrasher.attack");

			return false;
		} catch (Exception exception) {
			punish();
			return false;
		}
	}

	protected void checkBlock(Packet<?> packet) {
		if (packet instanceof PacketLoginInStart)
			blockLoginInStart((PacketLoginInStart) packet);
		if (packet instanceof PacketHandshakingInSetProtocol)
			blockHandshakingInSetProtocol((PacketHandshakingInSetProtocol) packet);
		if (packet instanceof PacketStatusInPing)
			blockStatusInPing((PacketStatusInPing) packet);
	}

	public void blockStatusInPing(PacketStatusInPing statusInPing) {
		try {
			if (statusInPing.a() > Integer.MAX_VALUE || statusInPing.a() < Integer.MIN_VALUE) {
				punish();
				return;
			}
		} catch (Exception exception) {
			punish();
		}
	}

	protected void startAttack() {
		attack.setState(true);
		(new Thread(() -> {
			while (attack.isUnderAttack()) {
				try {
					if (attack.isUnderAttack() && size < maxConnections)
						attack.setState(false);
					Thread.sleep(2000);
				} catch (InterruptedException error) {
				}
			}
		})).start();
	}

	protected void blockHandshakingInSetProtocol(PacketHandshakingInSetProtocol handshakingInSetProtocol) {

		try {
			if (handshakingInSetProtocol.a() == null) {
				punish();
				return;
			}

			if (!(handshakingInSetProtocol.a() instanceof EnumProtocol)) {
				punish();
				return;
			}

			if (handshakingInSetProtocol.hostname == null) {
				punish();
				return;
			}

			if (handshakingInSetProtocol.port != serverPort || handshakingInSetProtocol.port > 65535
					|| handshakingInSetProtocol.port < 1) {
				punish();
				return;
			}

			if (handshakingInSetProtocol.a() == EnumProtocol.PLAY && !attack.isWhitelisted(clientAddress)) {
				punish();
				return;
			}

			if (!(handshakingInSetProtocol.a() instanceof EnumProtocol)) {
				getAPI().disconnectChannel(channel);
				return;
			}

			final Integer protocol = handshakingInSetProtocol.b();

			if (protocol <= 40 || protocol >= 800) {
				punish();
				return;
			}
		} catch (Exception exception) {
			punish();
		}
	}

	protected void blockLoginInStart(PacketLoginInStart packetLoginStart) {

		if (packetLoginStart.a() == null) {
			punish();
			return;
		}

		try {
			final GameProfile gameProfile = packetLoginStart.a();

			if (gameProfile.getName() == null) {
				punish();
				return;
			}

			if (gameProfile.getName().trim().isEmpty()) {
				punish();
				return;
			}

			final String playerName = gameProfile.getName();
			if (playerName.length() > 16) {
				punish();
				return;
			}

			if (playerName.length() < 3) {
				punish();
				return;
			}

			if (playerName.contains(" ")) {
				punish();
				return;
			}

			if (!getAPI().hasOnlyLettersBoolean(playerName)) {
				punish();
				return;
			}

			for (String currentName : blacklistNames) {
				if (!playerName.toLowerCase().contains(currentName))
					continue;
				punish();
				break;
			}
		} catch (Exception exception) {
			punish();
		}
	}

	protected void punish() {
		getAPI().getServerAttack().addBlacklist(clientAddress);
		channel.flush();
		channel.close();
		if(strongAttack) getAPI().ipTable(clientAddress, true);
		totalBlocked++;
	}

}

class PlayerLogin implements Listener {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private List<String> blacklistNames;

	public PlayerLogin() {
		instance = RayzsAntiCrasher.getInstance();
		api = RayzsAntiCrasher.getAPI();
		blacklistNames = instance.getCheckFile().getYAML()
				.getStringList("settings.server." + "illegalconnection" + ".blackListNames");
		if (blacklistNames.isEmpty())
			instance.getCheckFile().getYAML().set("settings.server." + "illegalconnection" + ".blackListNames",
					Arrays.asList("dropbot", "mcspawm", "mcrage", "lauren"));
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		final String clientAddress = event.getAddress().getHostAddress();
		final String playerName = player.getName();

		if (playerName.length() > 16) {
			punish(event, clientAddress);
			return;
		}

		if (playerName.length() < 3) {
			punish(event, clientAddress);
			return;
		}

		if (playerName.contains(" ")) {
			punish(event, clientAddress);
			return;
		}

		if (!api.hasOnlyLettersBoolean(playerName)) {
			punish(event, clientAddress);
			return;
		}
		for (String currentName : blacklistNames) {
			if (!playerName.toLowerCase().contains(currentName))
				continue;
			punish(event, clientAddress);
			break;
		}
	}

	protected void punish(final PlayerLoginEvent event, String clientAddress) {
		api.getServerAttack().addBlacklist(clientAddress);
		event.disallow(Result.KICK_OTHER, "Disconnected");
	}
}