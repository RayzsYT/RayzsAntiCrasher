package de.rayzs.rayzsanticrasher.crasher.ext;

import org.bukkit.entity.Player;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.file.FileManager;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;

public abstract class ClientCheck {
	
	public abstract boolean onCheck(Channel channel, Player player, String packetName, Packet<?> packet,
			Integer amount);

	public RayzsAntiCrasherAPI getAPI() {
		return RayzsAntiCrasher.getAPI();
	}

	public RayzsAntiCrasher getInstance() {
		return RayzsAntiCrasher.getInstance();
	}

	public FileManager getFileManager(String search, ClientCheck checkClass) {
		return getInstance().getCheckFile().search(
				"settings.client." + checkClass.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + search);
	}
}