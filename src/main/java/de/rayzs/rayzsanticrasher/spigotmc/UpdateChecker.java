package de.rayzs.rayzsanticrasher.spigotmc;

import org.bukkit.Bukkit;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

	private RayzsAntiCrasher instance;
	private int resourceId = 90435;

	public UpdateChecker(RayzsAntiCrasher instance) {
		this.instance = instance;
	}

	public void getVersion(final Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
			try (InputStream inputStream = new URL(
					"https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
					Scanner scanner = new Scanner(inputStream)) {
				if (scanner.hasNext()) {
					consumer.accept(scanner.next());
				}
			} catch (IOException exception) {
				instance.logger("§8[§4R§cA§4C§8] §7Error searching for a new update§8: §c" + exception.getMessage());
			}
		});
	}
}