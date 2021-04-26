package de.rayzs.rayzsanticrasher.checks;

import java.net.SocketAddress;
import org.bukkit.entity.Player;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
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
	private Object object;

	public Crasher(Channel channel, SocketAddress connector, String packetName, Packet<?> packet, Object object, Integer amount) {
		this.api = RayzsAntiCrasher.getAPI();
		this.channel = channel;
		this.packetName = packetName;
		this.packet = packet;
		this.amount = amount;
		this.object = object;
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
				if(channel != null) api.disconnectChannel(channel);
				try {
					api.doNotify(RayzsAntiCrasher.getInstance().getCrashReportMessage(player.getName(), amount, currentCheck.getClass().getSimpleName(), packetName), player);
				}catch (Exception error) { }
				return;
			}
		}
	}

	private void onClientSourceCheck() {
		for (ClientSourceCheck currentCheck : api.getClientSourceChecks()) {
			boolean check;
			check = currentCheck.onCheck(channel, player, packetName, packet);
			if (check) {
				if(channel != null) api.disconnectChannel(channel);
				api.doNotify(RayzsAntiCrasher.getInstance().getCrashReportMessage(player.getName(), amount,
						currentCheck.getClass().getSimpleName(), packetName), player);
				return;
			}
		}
	}

	private void onServerCheck() {
		for (ServerCheck currentCheck : api.getServerChecks()) {
			boolean check;
			check = currentCheck.onCheck(channel, packetName, packet, object, amount);
			if (check) {
				String address = channel.remoteAddress().toString().split(":")[0];
				if(channel != null) api.disconnectChannel(channel);
				try {
					api.doNotify(RayzsAntiCrasher.getInstance().getCrashReportMessage(address, amount,
							currentCheck.getClass().getSimpleName(), packetName), player);
					return;
				} catch (Exception error) {
					if(channel != null) api.disconnectChannel(channel);
				}
				return;
			}
		}
	}
}