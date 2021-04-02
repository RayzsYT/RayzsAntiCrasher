package de.rayzs.rayzsanticrasher.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;

import de.rayzs.rayzsanticrasher.checks.Crasher;
import de.rayzs.rayzsanticrasher.server.Reflection.FieldAccessor;
import de.rayzs.rayzsanticrasher.server.Reflection.MethodInvoker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.Packet;

public class ServerInjector {
	
	// Code from TinyProtocol
	// Exactly from this link here:
	// -> https://github.com/dmulloy2/ProtocolLib/blob/master/Examples/TinyProtocol/src/main/java/com/comphenix/tinyprotocol/Reflection.java
	// This is not my creation! :)
	
	private static final AtomicInteger ID = new AtomicInteger(0);
	private static final Class<Object> minecraftServerClass = Reflection.getUntypedClass("{nms}.MinecraftServer");
	private static final Class<Object> serverConnectionClass = Reflection.getUntypedClass("{nms}.ServerConnection");
	private static final FieldAccessor<Object> getMinecraftServer = Reflection.getField("{obc}.CraftServer",
			minecraftServerClass, 0);
	private static final FieldAccessor<Object> getServerConnection = Reflection.getField(minecraftServerClass,
			serverConnectionClass, 0);
	private static final MethodInvoker getNetworkMarkers = Reflection.getTypedMethod(serverConnectionClass, null,
			List.class, serverConnectionClass);
	private static final Class<?> PACKET_LOGIN_IN_START = Reflection.getMinecraftClass("PacketLoginInStart");
	private static final FieldAccessor<GameProfile> getGameProfile = Reflection.getField(PACKET_LOGIN_IN_START,
			GameProfile.class, 0);
	private Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
	private Set<Channel> uninjectedChannels = Collections
			.newSetFromMap(new MapMaker().weakKeys().<Channel, Boolean>makeMap());
	private List<Object> networkManagers;
	private List<Channel> serverChannels = Lists.newArrayList();
	private ChannelInboundHandlerAdapter serverChannelHandler;
	private ChannelInitializer<Channel> beginInitProtocol;
	private ChannelInitializer<Channel> endInitProtocol;

	private String handlerName;

	protected volatile boolean closed;
	protected Plugin plugin;

	public ServerInjector(final Plugin plugin) {
		this.plugin = plugin;
		this.handlerName = getHandlerName();
		try {
			registerChannelHandler();
		} catch (IllegalArgumentException ex) {
			new BukkitRunnable() {
				@Override
				public void run() {
					registerChannelHandler();
				}
			}.runTask(plugin);
		}
	}

	private void createServerChannelHandler() {
		endInitProtocol = new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel channel) throws Exception {
				try {
					synchronized (networkManagers) {
						if (!closed) {
							injectChannelInternal(channel);
						}
					}
				} catch (Exception error) { }
			}

		};
		beginInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline().addLast(endInitProtocol);
			}

		};

		serverChannelHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				Channel channel = (Channel) msg;
				channel.pipeline().addFirst(beginInitProtocol);
				ctx.fireChannelRead(msg);
			}

		};
	}

	@SuppressWarnings("unchecked")
	private void registerChannelHandler() {
		Object mcServer = getMinecraftServer.get(Bukkit.getServer());
		Object serverConnection = getServerConnection.get(mcServer);
		boolean looking = true;
		networkManagers = (List<Object>) getNetworkMarkers.invoke(null, serverConnection);
		createServerChannelHandler();
		for (int i = 0; looking; i++) {
			List<Object> list = Reflection.getField(serverConnection.getClass(), List.class, i).get(serverConnection);

			for (Object item : list) {
				if (!ChannelFuture.class.isInstance(item))
					break;
				Channel serverChannel = ((ChannelFuture) item).channel();
				serverChannels.add(serverChannel);
				serverChannel.pipeline().addFirst(serverChannelHandler);
				looking = false;
			}
		}
	}

	private void unregisterChannelHandler() {
		if (serverChannelHandler == null)
			return;
		for (Channel serverChannel : serverChannels) {
			final ChannelPipeline pipeline = serverChannel.pipeline();
			serverChannel.eventLoop().execute(new Runnable() {
				@Override
				public void run() {
					try {
						pipeline.remove(serverChannelHandler);
					} catch (NoSuchElementException | NoClassDefFoundError error) { }
				}

			});
		}
	}

	public Object onPacketOutAsync(Player reciever, Channel channel, Object packet) {
		return packet;
	}

	public void sendPacket(Channel channel, Object packet) {
		channel.pipeline().writeAndFlush(packet);
	}

	public void receivePacket(Channel channel, Object packet) {
		channel.pipeline().context("encoder").fireChannelRead(packet);
	}

	protected String getHandlerName() {
		return "rac-" + plugin.getName() + "-" + ID.incrementAndGet();
	}

	public void injectChannel(Channel channel) {
		injectChannelInternal(channel);
	}

	private PacketInterceptor injectChannelInternal(Channel channel) {
		try {
			PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);
			if (interceptor == null) {
				interceptor = new PacketInterceptor();
				channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
				uninjectedChannels.remove(channel);
			}

			return interceptor;
		} catch (IllegalArgumentException e) {
			return (PacketInterceptor) channel.pipeline().get(handlerName);
		}
	}

	public void uninjectChannel(final Channel channel) {
		if (!closed) {
			uninjectedChannels.add(channel);
		}

		channel.eventLoop().execute(new Runnable() {

			@Override
			public void run() {
				channel.pipeline().remove(handlerName);
			}

		});
	}

	public boolean hasInjected(Channel channel) {
		return channel.pipeline().get(handlerName) != null;
	}

	public final void close() {
		if (!closed) {
			closed = true;
			unregisterChannelHandler();
		}
	}

	private final class PacketInterceptor extends ChannelDuplexHandler {
		public volatile Player player;

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			final Channel channel = ctx.channel();
			Packet<?> packet = (Packet<?>) msg;
			String packetName = msg.toString().split("@")[0].replace("net.minecraft.server.v1_8_R3.", "");
			new Crasher(channel, channel.remoteAddress(), packetName, packet);
			handleLoginStart(channel, msg);
			if (msg != null) {
				super.channelRead(ctx, msg);
			}
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			try {
				msg = onPacketOutAsync(player, ctx.channel(), msg);
			} catch (Exception error) {
			}

			if (msg != null) {
				super.write(ctx, msg, promise);
			}
		}

		private void handleLoginStart(Channel channel, Object packet) {
			if (PACKET_LOGIN_IN_START.isInstance(packet)) {
				GameProfile profile = getGameProfile.get(packet);
				channelLookup.put(profile.getName(), channel);
			}
		}
	}
}