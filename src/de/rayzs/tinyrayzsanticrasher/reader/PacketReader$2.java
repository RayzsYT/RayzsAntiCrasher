
package de.rayzs.tinyrayzsanticrasher.reader;

import java.util.List;

import org.bukkit.entity.Player;

import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketReader$2 extends ByteToMessageDecoder {

	private TinyRayzsAntiCrasherAPI api;
	private Player player;
	
	public PacketReader$2(final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api) {
		this.api = api;
	}

	public PacketReader$2(final Player player, final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api) {
		this.player = player;
		this.api = api;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
		final Channel channel = channelHandlerContext.channel();
		try {
			if (byteBuf instanceof EmptyByteBuf) {
				list.add(byteBuf.readBytes(byteBuf.readableBytes()));
				return;
			}
			
			if (byteBuf.array().length > 6500) { 
				if(player != null) {
					api.punish(player, channel); return; 
				}
				channel.close();
			}
			list.add(byteBuf.readBytes(byteBuf.readableBytes()));
		}catch (Exception exception) {
			if(player != null) {
				api.punish(player, channel); return; 
			}
			channel.close();
		}
	}
}