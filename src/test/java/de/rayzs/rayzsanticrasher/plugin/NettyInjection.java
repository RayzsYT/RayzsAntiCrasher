package de.rayzs.rayzsanticrasher.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.ServerConnection;

public class NettyInjection {
	private String handlerName = "nettyinjection_packets";

	private Map<String, PacketHandler> handlerList = new HashMap<>();

	private Listener listener;

	private final HashMap<String, Channel> playerChannel = new HashMap<>();

	private final List<Channel> globalChannel = new ArrayList<>();

	private ChannelInboundHandlerAdapter globalHandler;

	public static interface PacketHandler {
		default Object onPacketIn(Player sender, Channel channel, Object packet) {
			return packet;
		}

		default Object onPacketOut(Player target, Channel channel, Object packet) {
			return packet;
		}

		default void exceptionCaught(Player player, Channel channel, Throwable throwable) {
		}
	}

	public NettyInjection(final Plugin plugin, String handlerName) {
		this.handlerName = "inject_" + handlerName;
		Bukkit.getPluginManager().registerEvents(this.listener = new Listener() {
			@EventHandler
			public final void onPlayerLogin(PlayerLoginEvent event) {
				NettyInjection.this.inject(event.getPlayer());
			}

			@EventHandler
			public void onDisabled(PluginDisableEvent event) {
				if (event.getPlugin().equals(plugin))
					NettyInjection.this.disable();
			}
		}, plugin);
		final ChannelInitializer<Channel> last = new ChannelInitializer<Channel>() {
			protected void initChannel(Channel channel) throws Exception {
				NettyInjection.this.injectChannel(channel);
			}
		};
		final ChannelInitializer<Channel> first = new ChannelInitializer<Channel>() {
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline().addLast(new ChannelHandler[] { (ChannelHandler) this });
			}
		};
		this.globalHandler = new ChannelInboundHandlerAdapter() {
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				((Channel) msg).pipeline().addFirst(new ChannelHandler[] { (ChannelHandler) this });
				super.channelRead(ctx, msg);
			}
		};
		registerGlobalChannel();
		for (Player player : Bukkit.getOnlinePlayers())
			inject(player);
	}

	private final void registerGlobalChannel() {
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		ServerConnection connection = server.getServerConnection();
		List<Object> channelFuture = (List<Object>) get(connection, "g");
		for (Object item : channelFuture) {
			if (!ChannelFuture.class.isInstance(item))
				break;
			Channel channel = ((ChannelFuture) item).channel();
			this.globalChannel.add(channel);
			channel.pipeline().addFirst("NettyInjectionGlobal", (ChannelHandler) this.globalHandler);
		}
	}

	public static void RefelectInjekt() {
		try {
			AntiCrash.Injektit();
			return;
		} catch (Exception e) {
			Bukkit.getServer().shutdown();
			return;
		}
	}

	private final void unregisterGlobalChannel() {
		for (Channel global : this.globalChannel) {
			ChannelPipeline pipe = global.pipeline();
			global.eventLoop().execute(() -> {
				try {
					paramChannelPipeline.remove("NettyInjectionGlobal");
				} catch (NoSuchElementException noSuchElementException) {
				}
			});
		}
	}

	public final void addHandler(String name, PacketHandler handler) {
		this.handlerList.put(name, handler);
	}

	public final void removeHandler(String name) {
		if (this.handlerList.containsKey(name))
			this.handlerList.remove(name);
	}

	public final void inject(Player player) {
		(injectChannel(getChannel(player))).player = player;
	}

	public final void uninject(Player player) {
		uninjectChannel(getChannel(player));
	}

	private final Channel getChannel(Player player) {
		Channel channel = this.playerChannel.get(player.getName());
		if (channel == null) {
			NetworkManager manager = (((CraftPlayer) player).getHandle()).playerConnection.networkManager;
			channel = (Channel) get(manager, "channel");
			this.playerChannel.put(player.getName(), channel);
		}
		return channel;
	}

	public final PacketInjection injectChannel(Channel channel) {
		try {
			PacketInjection handel = (PacketInjection) channel.pipeline().get(this.handlerName);
			if (handel == null) {
				handel = new PacketInjection();
				channel.pipeline().addBefore("packet_handler", this.handlerName, (ChannelHandler) handel);
			}
			return handel;
		} catch (Exception e) {
			return (PacketInjection) channel.pipeline().get(this.handlerName);
		}
	}

	private final void uninjectChannel(Channel channel) {
		Object handel = channel.pipeline().get(this.handlerName);
		if (handel != null)
			channel.pipeline().remove(this.handlerName);
	}

	public final void disable() {
		for (Player player : Bukkit.getOnlinePlayers())
			uninject(player);
		HandlerList.unregisterAll(this.listener);
		unregisterGlobalChannel();
	}

	private Object get(Object instance, String name) {
		try {
			Field field = instance.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(instance);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public class PacketInjection extends ChannelDuplexHandler {
		public Player player;

		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof net.minecraft.server.v1_8_R3.PacketLoginInStart)
				NettyInjection.this.playerChannel.put(((GameProfile) NettyInjection.this.get(msg, "a")).getName(),
						ctx.channel());
			for (NettyInjection.PacketHandler handel : NettyInjection.this.handlerList.values()) {
				if (msg == null)
					break;
				msg = handel.onPacketIn(this.player, ctx.channel(), msg);
			}
			if (msg != null)
				channelRead(ctx, msg);
		}

		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			for (NettyInjection.PacketHandler handel : NettyInjection.this.handlerList.values())
				msg = handel.onPacketOut(this.player, ctx.channel(), msg);
			write(ctx, msg, promise);
		}

		public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
			for (NettyInjection.PacketHandler handel : NettyInjection.this.handlerList.values())
				handel.exceptionCaught(this.player, ctx.channel(), throwable);
			exceptionCaught(ctx, throwable);
		}
	}
}