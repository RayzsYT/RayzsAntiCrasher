package de.rayzs.rayzsanticrasher.checks.ext;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.file.FileManager;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.Packet;

public abstract class ServerCheck {

	public abstract boolean onCheck(Channel channel, String packetName, Packet<?> packet, Object object, Integer amount);

	public RayzsAntiCrasherAPI getAPI() {
		return RayzsAntiCrasher.getAPI();
	}

	public RayzsAntiCrasher getInstance() {
		return RayzsAntiCrasher.getInstance();
	}

	public FileManager getFileManager(String search, ServerCheck checkClass) {
		return getInstance().getCheckFile()
				.search("settings.server." + checkClass.getClass().getSimpleName().toLowerCase().split("@")[0] + "." + search);
	}
}