package de.rayzs.rayzsanticrasher.crasher;

import java.net.SocketAddress;
import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.crasher.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.crasher.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.crasher.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;

public class Crasher {
	
	private RayzsAntiCrasherAPI api;
	private Player player;
	private Channel channel;
	private String packetName;
	private Packet<?> packet;
	private Integer amount;

	public Crasher(Channel channel, SocketAddress connector, String packetName, Packet<?> packet) {
		this.api = RayzsAntiCrasher.getAPI();
		this.channel = channel;
		this.packetName = packetName;
		this.packet = packet;
		onServerCheck();
	}

	public Crasher(Player player, Channel channel, String packetName, Packet<?> packet, Integer amount) {
		this.api = RayzsAntiCrasher.getAPI();
		this.player = player;
		this.channel = channel;
		this.packetName = packetName;
		this.packet = packet;
		this.amount = amount;
		onClientCheck();
	}

	public Crasher(Player player, Channel channel, String packetName, Packet<?> packet) {
		this.api = RayzsAntiCrasher.getAPI();
		this.player = player;
		this.channel = channel;
		this.packetName = packetName;
		this.packet = packet;
		onClientSourceCheck();
	}

	private void onClientCheck() {
		for (ClientCheck currentCheck : api.getClientChecks()) {
			boolean check;
			check = currentCheck.onCheck(channel, player, packetName, packet, amount);
			if (check) {
				channel.flush();
				channel.close();
				api.doNotify("§8[§9R§bA§9C§8] §b" + player.getName() + " §8» §b"
						+ currentCheck.getClass().getSimpleName() + " §8┊┊ §b" + amount + "§9x §b§n§o" + packetName,
						player);
				return;
			}
		}
	}

	private void onClientSourceCheck() {
		for (ClientSourceCheck currentCheck : api.getClientSourceChecks()) {
			boolean check;
			check = currentCheck.onCheck(channel, player, packetName, packet);
			if (check) {
				channel.flush();
				channel.close();
				api.doNotify("§8[§9R§bA§9C§8] §b" + player.getName() + " §8» §b"
						+ currentCheck.getClass().getSimpleName() + " §8┊┊ §b" + amount + "§9x §b§n§o" + packetName,
						player);
				return;
			}
		}
	}

	private void onServerCheck() {
		for (ServerCheck currentCheck : api.getServerChecks()) {
			boolean check;
			check = currentCheck.onCheck(channel, packetName, packet);
			if (check) {
				String address = channel.remoteAddress().toString().split(":")[0];
				channel.flush();
				channel.close();
				try {
					api.doNotify("§8[§9R§bA§9C§8] §b" + address + " §8» §b" + currentCheck.getClass().getSimpleName()
							+ " §8┊┊ §b§n§o" + packetName, player);
					return;
				} catch (Exception error) { }
				return;
			}
		}
	}
}