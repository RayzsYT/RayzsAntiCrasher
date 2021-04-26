package de.rayzs.rayzsanticrasher.plugin.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import de.rayzs.rayzsanticrasher.addon.Addon;
import de.rayzs.rayzsanticrasher.api.RayzsAntiCrasherAPI;
import de.rayzs.rayzsanticrasher.checks.ext.ClientCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ClientSourceCheck;
import de.rayzs.rayzsanticrasher.checks.ext.ServerCheck;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class SetupCommand implements CommandExecutor {

	private RayzsAntiCrasher instance;
	private RayzsAntiCrasherAPI api;
	private List<String> alreadyExecuted;
	private Command command;

	public SetupCommand(RayzsAntiCrasher instance, RayzsAntiCrasherAPI api) {
		this.instance = instance;
		this.api = api;
		alreadyExecuted = new ArrayList<>();
		instance.getCommand("rac").setExecutor(this);
		instance.getCommand("rayzsanticrasher").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		command = cmd;
		if (!sender.hasPermission("rayzsanticrasher.use")) {
			sender.sendMessage(instance.getStandartMessage());
			return false;
		}
		if (args.length < 1) {
			sendHelp(sender);
			return true;
		}

		final String command = args[0].toLowerCase();

		if (sender instanceof Player) {
			Player player = (Player) sender;
			final String uuid = player.getUniqueId().toString();
			if (alreadyExecuted.contains(uuid)) {
				player.sendMessage(
						"§8[§9R§bA§9C§8] §7You§8'§7ve to §e§nwait§8, §7until be able executing this command again§8!");
				return true;
			}
			if (!alreadyExecuted.contains(uuid))
				alreadyExecuted.add(uuid);
			Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
				@Override
				public void run() {
					if (alreadyExecuted.contains(uuid))
						alreadyExecuted.remove(uuid);
				}
			}, 20);

			if (command.equals("notify")) {
				if (!sender.hasPermission("rayzsanticrasher.notify")) {
					sender.sendMessage("§8[§4R§cA§4C§8] §7You§8'§7re not allowed to execute that§8!");
					return false;
				}
				final Integer getNotification = api.getNotify(player);
				Integer setNotification = 0;
				switch (getNotification) {
				case 0:
					player.sendMessage("§8[§9R§bA§9C§8] §7You will now be notified of §b§o§nCrash§8-§b§o§nAttacks§8!");
					setNotification = 1;
					break;
				case 1:
					player.sendMessage(
							"§8[§9R§bA§9C§8] §7You will no longer be notified of §b§o§nCrash§8-§b§o§nAttacks§8!");
					setNotification = 0;
					break;
				}
				api.setNotify(player, setNotification);
				if (instance.useMySQL())
					instance.getNotifySQL().set(player.getUniqueId(), "NOTIFY", api.getNotify(player));
				return true;
			}
		}
		
		if (command.equals("tps")) {
			if (!sender.hasPermission("rayzsanticrasher.tps")) {
				sender.sendMessage("§8[§4R§cA§4C§8] §7You§8'§7re not allowed to execute that§8!");
				return false;
			}
			sender.sendMessage("§8[§6R§eA§6C§8] §7Calculating§8...");
			Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
				@Override
				public void run() {
					final Double tps = api.getServerTPS();
					final String message = "§8[§9R§bA§9C§8] §7Current TPS §8[§b" + tps + "§8]";
					sender.sendMessage(message);
				}
			});
			return true;
		}
		
		if (command.equals("unblockips")) {
			if (!sender.hasPermission("rayzsanticrasher.unblockips")) {
				sender.sendMessage("§8[§4R§cA§4C§8] §7You§8'§7re not allowed to execute that§8!");
				return false;
			}

			api.getServerAttack().clearBlacklist();
			sender.sendMessage("§8[§9R§bA§9C§8] §7All blocked ip§8'§7s got unblocked§8!");
			return true;
		}

		if (command.equals("checks")) {
			if (!sender.hasPermission("rayzsanticrasher.checks")) {
				sender.sendMessage("§8[§4R§cA§4C§8] §7You§8'§7re not allowed to execute that§8!");
				return false;
			}
			sender.sendMessage("§8[§9R§bA§9C§8] §7Here are all §aenabled §7checks listed§8:");
			sender.sendMessage("§7§oClient checks§8:");
			sender.sendMessage(" §8- §bRAC-STANDARD §7§oCannot be deactivated");
			for (ClientCheck currentCheck : api.getClientChecks()) {
				String checkName = currentCheck.getClass().getSimpleName().split("@")[0].toUpperCase();
				sender.sendMessage(" §8- §b" + checkName);
			}
			sender.sendMessage("§7§oServer checks§8:");
			for (ServerCheck currentCheck : api.getServerChecks()) {
				String checkName = currentCheck.getClass().getSimpleName().split("@")[0].toUpperCase();
				sender.sendMessage(" §8- §b" + checkName);
			}
			sender.sendMessage("§7§oListener checks§8:");
			for (Listener currentCheck : api.getListenerChecks()) {
				String checkName = currentCheck.getClass().getSimpleName().split("@")[0].toUpperCase();
				sender.sendMessage(" §8- §b" + checkName);
			}
			sender.sendMessage("§7§oClientsource checks§8:");
			for (ClientSourceCheck currentCheck : api.getClientSourceChecks()) {
				String checkName = currentCheck.getClass().getSimpleName().split("@")[0].toUpperCase();
				sender.sendMessage(" §8- §b" + checkName);
			}
			return true;
		}
		if (command.equals("addons")) {
			if (!sender.hasPermission("rayzsanticrasher.addons")) {
				sender.sendMessage("§8[§4R§cA§4C§8] §7You§8'§7re not allowed to execute that§8!");
				return false;
			}
			String result = "§8[§4ADDONSYSTEM§8] §7All addons§8:\n§8» ";
			if (instance.getAddonManager().listAddons().isEmpty()) {
				result += "§c§oEmpty";
				sender.sendMessage(result);
				return false;
			}
			for (Addon addon : instance.getAddonManager().listAddons()) {
				if (addon.isEnabled()) {
					result += "§a" + addon.getName() + " ";
					continue;
				}
				result += "§c" + addon.getName() + " ";
				continue;
			}
			sender.sendMessage(result);
			return true;
		}
		sendHelp(sender);
		return true;
	}

	void sendHelp(CommandSender sender) {
		sender.sendMessage(
				"§8[§9R§bA§9C §8- §b§o§n" + api.getVersion() + "§8] §7Here are all §aavailable §7commands listed§8:");
		sender.sendMessage("§8 - §8/§9" + command.getName() + " §b" + "notify");
		sender.sendMessage("§8 - §8/§9" + command.getName() + " §b" + "checks");
		sender.sendMessage("§8 - §8/§9" + command.getName() + " §b" + "tps");
		sender.sendMessage("§8 - §8/§9" + command.getName() + " §b" + "addons");
		sender.sendMessage("§8 - §8/§9" + command.getName() + " §b" + "unblockips");
	}
}