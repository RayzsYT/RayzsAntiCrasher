package de.rayzs.tinyrayzsanticrasher.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.enums.ReflectionType;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import io.netty.channel.Channel;

public class CrashClient {
	
	private TinyRayzsAntiCrasher instance;
	private TinyRayzsAntiCrasherAPI api;
	
	private List<?> networkManagers;
	private Class<?> networkClass;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CrashClient(final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api) {
		this.instance = instance;
		this.api = api;
		this.networkClass = instance.getReflection().getNMSClass("NetworkManager", ReflectionType.MINECRAFT);
		try {
			final Object server = Bukkit.getServer();
			final Object craftServer = instance.getReflection().getNMSClass("CraftServer", ReflectionType.CRAFTBUKKIT).cast(server);
			final Field console = craftServer.getClass().getDeclaredField("console"); console.setAccessible(true);
			final Class<?> minecraftServerClass = instance.getReflection().getNMSClass("MinecraftServer", ReflectionType.MINECRAFT);
			final Object castedMinecraftServer = minecraftServerClass.cast(console.get(craftServer));
			final Object minecraftServer = castedMinecraftServer.getClass().getMethod("getServer").invoke(castedMinecraftServer);
			final Object serverConnection = minecraftServer.getClass().getMethod("getServerConnection").invoke(minecraftServer);
			this.networkManagers = Collections.synchronizedList((List) getNetworkManagerList(serverConnection));
		} catch (Exception exception) { exception.printStackTrace(); }
		new Thread(() -> { while(true) injectConnections(); }).start();
	}
	
	protected void injectConnections() {
		try {
			Field field = instance.getReflection().getFirstFieldByType(networkClass, Channel.class);
			if(field == null) return;
			field.setAccessible(true);
			if(networkManagers.isEmpty()) return;
			for (Object manager : this.networkManagers) {
				Channel channel = (Channel) field.get(manager);
				if(channel == null) return;
				if(channel.remoteAddress() == null) {
					channel.close(); return;
				}
				if (api.isPlayerChannel(channel)) {
					final Player player = api.getPlayerPerChannel(channel);
					if(player == null) return;
					api.getPlayer(player).register();
					return;
				}
			}
		} catch (Exception exception) { exception.printStackTrace(); }
	}

	protected Object getNetworkManagerList(Object serverConnection) {
		try {
			Method[] arrayOfMethod;
			final Integer maxLenght = (arrayOfMethod = serverConnection.getClass().getDeclaredMethods()).length;
			for (int i = 0; i < maxLenght; i++) {
				Method method = arrayOfMethod[i];
				method.setAccessible(true);
				if (method.getReturnType() == List.class) { return method.invoke((Object) null, new Object[] { serverConnection }); }
			}
		} catch (Exception exception) { exception.printStackTrace(); }
		return null;
	}
}