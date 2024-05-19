package de.rayzs.tinyrayzsanticrasher.reflection;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.rayzs.tinyrayzsanticrasher.enums.ReflectionType;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import io.netty.channel.Channel;

public class Reflection {

	private TinyRayzsAntiCrasher instance;
	private String version;

	public Reflection(final TinyRayzsAntiCrasher instance) {
		this.instance = instance;
		final String packageName = Bukkit.getServer().getClass().getPackage().getName();
		version = packageName.substring(packageName.lastIndexOf('.') + 1);
	}

	public Field getFirstFieldByType(final Class<?> clazz, final Class<?> type) {
		Field[] arrayOfField;
		int max = (arrayOfField = clazz.getDeclaredFields()).length;
		for (int i = 0; i < max; i++) {
			Field field = arrayOfField[i];
			field.setAccessible(true);
			if (field.getType() == type) {
				return field;
			}
		}

		return null;
	}

	public void setValue(final Object object, final String name, final Object value) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(object, value);
		} catch (Exception exception) {
			instance.getLogger().fine("Error set the value \"" + name + "\"!");
			exception.printStackTrace();
		}
	}

	public Object getValue(final Object object, final String name) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		} catch (Exception exception) {
			instance.getLogger().fine("Error finding the value \"" + name + "\"!");
			exception.printStackTrace();
		}
		return null;
	}

	public Class<?> getNMSClass(final String name, final ReflectionType reflectionType) {
		String path = "";
		if (reflectionType.equals(ReflectionType.MINECRAFT))
			path = "net.minecraft.server.";
		if (reflectionType.equals(ReflectionType.CRAFTBUKKIT))
			path = "org.bukkit.craftbukkit.";
		try {
			return Class.forName(path + version + "." + name);
		} catch (Exception exception) {
		}
		return null;
	}

	public void sendPacket(final Player player, final Object packet) {
		try {
			final Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
			final Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet", ReflectionType.MINECRAFT))
					.invoke(playerConnection, packet);
		} catch (Exception exception) {
			instance.getLogger().fine("Error sending packet to player!");
			exception.printStackTrace();
		}
	}

	public Channel getChannel(final Player player) {
		try {
			final Object craftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer")
					.cast(player);
			final Object handle = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
			final Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			final Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
			return (Channel) networkManager.getClass().getField("channel").get(networkManager);
		} catch (Exception exception) {
			instance.getLogger().fine("Error getting channel of player!");
			exception.printStackTrace();
		}
		return null;
	}

	public String getVersion() {
		return version;
	}
}