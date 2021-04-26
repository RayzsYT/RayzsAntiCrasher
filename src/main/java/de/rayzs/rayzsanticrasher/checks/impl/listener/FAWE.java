package de.rayzs.rayzsanticrasher.checks.impl.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

public class FAWE implements Listener {
	
	@EventHandler
	public void onPlayerChatTabComplete(final PlayerChatTabCompleteEvent event) {
		try {
			final Player player = event.getPlayer();
			if (player.hasPermission("rayzsanticrasher.bypass") || player.isOp())
				return;
			final String tabCommand = event.getChatMessage().toLowerCase();
			final String tabbedCommand = tabCommand.split(" ")[0];
			if(tabbedCommand.startsWith("/to") || tabbedCommand.startsWith("/fastasyncworldedit:to")) {
				event.getTabCompletions().clear();
				System.out.println("KIK");
			}
		}catch (Exception exception) { }
	}
}