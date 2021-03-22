package de.rayzs.rayzsanticrasher.crasher.impl.server;

import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class InputOverflow extends ByteToMessageDecoder {

	private RayzsAntiCrasherAPI api;
	private CraftPlayer player;

	public InputOverflow(CraftPlayer player) {
		this.api = RayzsAntiCrasher.getAPI();
		this.player = player;
	}

	@Override
	protected void decode(ChannelHandlerContext channel, ByteBuf byteBuf, List<Object> list) throws Exception {
		try {
			if (byteBuf instanceof EmptyByteBuf) {
				list.add(byteBuf.readBytes(byteBuf.readableBytes()));
				return;
			}
			String clientAddress = channel.channel().remoteAddress().toString().split(":")[0].replace("/", "");
			if (byteBuf.array().length > 5000) {
				channel.flush();
				channel.close();
				api.doNotify(
						"§8[§9R§bA§9C§8] §b" + clientAddress + " §8» §b" + "§b" + this.getClass().getSimpleName()
								+ " §8┊┊ §b" + byteBuf.array().length + "§9x §b§n§o" + byteBuf.toString(),
						player.getPlayer());
			}
			list.add(byteBuf.readBytes(byteBuf.readableBytes()));
		} catch (Exception error) {
		}
	}
}