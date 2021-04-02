package de.rayzs.rayzsanticrasher.checks.impl.server;

import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ByteBufReader extends ByteToMessageDecoder {

	private RayzsAntiCrasherAPI api;
	private CraftPlayer player;

	public ByteBufReader(CraftPlayer player) {
		api = RayzsAntiCrasher.getAPI();
		this.player = player;
	}

	@Override
	protected void decode(ChannelHandlerContext channel, ByteBuf byteBuf, List<Object> list) throws Exception {
		try {
			
			if(channel == null) {
				return;
			}
			
			if(channel.channel() == null) {
				return;
			}
			
			if(!channel.channel().isActive()) {
				api.disconnectChannel(channel.channel());
				return;
			}
			
			if(!channel.channel().isOpen()) {
				api.disconnectChannel(channel.channel());
				return;
			}
			
			if(!channel.channel().isWritable()) {
				api.disconnectChannel(channel.channel());
				return;
			}
			
			if(channel.channel().remoteAddress() == null) {
				api.disconnectChannel(channel.channel());
				return;
			}
			
			final String clientAddress = channel.channel().remoteAddress().toString().split(":")[0].replace("/", "");
			
			if (byteBuf instanceof EmptyByteBuf) {
				list.add(byteBuf.readBytes(byteBuf.readableBytes()));
				return;
			}
			
			if (byteBuf.array().length > 5000) {
				api.kickPlayer(player, "Sending a packet with an too big input");
				api.disconnectChannel(channel.channel());
				api.doNotify(RayzsAntiCrasher.getInstance().getCrashReportMessage(clientAddress, byteBuf.array().length,
						this.getClass().getSimpleName(), byteBuf.toString()), player);
			}
			list.add(byteBuf.readBytes(byteBuf.readableBytes()));
		}catch (Exception error) { }
	}
}