package de.rayzs.rayzsanticrasher.server;

import java.util.HashMap;
import io.netty.channel.Channel;

public class PacketCounter {
	
	private HashMap<String, Long> packetTime;
	private HashMap<String, Integer> packetAmount;
	
	public PacketCounter(Channel channel) {
		this.packetTime = new HashMap<>();
		this.packetAmount = new HashMap<>();
	}
	
	public void addPacket(String packet) {
		final long calculatedTime = System.currentTimeMillis() - getLastPacket(packet);
		final int amount = getPacketAmount(packet) + 1;
		if(calculatedTime > 1000) {
			packetAmount.put(packet, 0);
			packetTime.put(packet, System.currentTimeMillis());
			return;
		}
		packetAmount.put(packet, amount);
	}
	
	public long getLastPacket(String packet) {
		try { return packetTime.get(packet);
		}catch (Exception error) { return 0; }
	}
	
	public int getPacketAmount(String packet) {
		try { return packetAmount.get(packet);
		}catch (Exception error) { return 0; }
	}
}