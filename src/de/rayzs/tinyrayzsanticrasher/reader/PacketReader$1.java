package de.rayzs.tinyrayzsanticrasher.reader;

import org.bukkit.entity.Player;

import de.rayzs.tinyrayzsanticrasher.api.TinyRayzsAntiCrasherAPI;
import de.rayzs.tinyrayzsanticrasher.enums.LanguageEnum;
import de.rayzs.tinyrayzsanticrasher.enums.ReflectionType;
import de.rayzs.tinyrayzsanticrasher.packet.PacketCounter;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class PacketReader$1 extends ChannelDuplexHandler {
	
	private TinyRayzsAntiCrasher instance;
	private TinyRayzsAntiCrasherAPI api;
	private Player player;
	private PacketCounter packetCounter;

	public PacketReader$1(final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api) {
		this.instance = instance;
		this.api = api;
	}
	
	public PacketReader$1(final Player player, final TinyRayzsAntiCrasher instance, final TinyRayzsAntiCrasherAPI api, PacketCounter packetCounter) {
		this.player = player;
		this.packetCounter = packetCounter;
		this.instance = instance;
		this.api = api;
	}

	@Override
	public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
		
		if (object != null) super.channelRead(channelHandlerContext, object);
			
		final Channel channel = channelHandlerContext.channel();
		
		try {
		
			final String packetName = object.getClass().getSimpleName();
			final ByteBuf byteBuf = Unpooled.buffer();
			final Class<?> packetDataSerializerClass = instance.getReflection().getNMSClass("PacketDataSerializer", ReflectionType.MINECRAFT);
			final Object packetDataSerializer = packetDataSerializerClass
												.getDeclaredConstructor(ByteBuf.class)
												.newInstance(byteBuf);
			final Class<?> packetClass =  instance.getReflection().getNMSClass(packetName, ReflectionType.MINECRAFT);
			if(packetClass == null) return;
	        final Object packet = packetClass.cast(object);
			packetClass.getDeclaredMethod("b", packetDataSerializerClass).invoke(packet, packetDataSerializer);
			if(api.check(byteBuf, channel)) { 
				if(player != null) {
					api.punish(player, channel); 
					return;
				}
				channel.close();
				return; 
			}
			
			if(player == null) return;
			
			packetCounter.addPacket(packetName);
			if(packetCounter.getPacketAmount(packetName) > 5000) { api.punish(player, channel); return; }
			
			if(api.hasLanguage(player)) return;
			if(packetName.equals("PacketPlayInSettings")) {
				final Object languageObject = instance.getReflection().getNMSClass(packetName, ReflectionType.MINECRAFT).cast(object);
				final String rawLanguage = (String) instance.getReflection().getValue(languageObject, "a");
				final String language = rawLanguage.split("_")[0].toUpperCase();
				try { api.setLanguage(player, LanguageEnum.valueOf(language));
				}catch (Exception exception) { api.setLanguage(player, LanguageEnum.EN);}
			}
		}catch (Exception exception) { 
			if(player != null) {
			api.punish(player, channel); return; 
		}
		channel.close(); }
	}
}