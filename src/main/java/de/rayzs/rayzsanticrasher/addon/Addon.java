package de.rayzs.rayzsanticrasher.addon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class Addon {

	private RayzsAntiCrasher instance;
	private File file;
	private String addonName, mainPath;
	private Object object;
	private Boolean enabled;

	public Addon(RayzsAntiCrasher instance, File file, String addonName) {
		this.instance = instance;
		this.file = file;
		this.addonName = addonName;
		this.enabled = false;
	}

	public String getName() {
		return this.addonName;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getAddonName() {
		return addonName;
	}
	
	public String getMainPath() {
		return mainPath;
	}

	public Object getObject() {
		return object;
	}

	public Boolean enable() {
		if (isEnabled()) {
			instance.logger("§8[§6" + addonName + "§8] §7Already enabled§8.");
			return false;
		}
		try {
			instance.logger("§8[§e" + addonName + "§8] §7Loading§8...");
			String fileInput = readFile(file);
			if (fileInput.startsWith("%ERROR%")) {
				instance.logger(fileInput.split("%ERROR%")[1]);
				return false;
			}
			if (!fileInput.startsWith("main: ")) {
				instance.logger("§8[§a" + addonName + "§8] §7Invalid §c§nrac.yml§8! §8[§7Main is missing§8]");
				return false;
			}
				String fileMainPath = fileInput.split("main: ")[1];
				mainPath = fileMainPath;
				interactAddon("onEnable");
				instance.logger("§8[§a" + addonName + "§8] §7Loaded§8!");
				enabled = true;
				return true;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException error) {
			error.printStackTrace();
			return false;
		}
	}

	public Boolean disable() {
		if (!isEnabled()) {
			instance.logger("§8[§6" + addonName + "§8] §7Already disabled§8.");
			return false;
		}
		try {
			interactAddon("onDisable");
			enabled = false;
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException error) {
			error.printStackTrace();
			return false;
		}
	}

	public Boolean isEnabled() {
		return this.enabled;
	}

	@SuppressWarnings({ "deprecation" })
	private void interactAddon(String methode)
			throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ClassLoader classLoader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() },
				getClass().getClassLoader());
		Class<?> clazz = classLoader.loadClass(mainPath);
		Method method = clazz.getDeclaredMethod(methode);
		Object object = clazz.newInstance();
		object = method.invoke(object);
	}

	@SuppressWarnings("resource")
	private String readFile(File file) {
		try {
			JarFile jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("addon.rac");
			InputStream stream = null;
			if (entry == null)
				return "%ERROR%§8[§c" + addonName + "§8] §7Empty §8/ §7not existing §4§nrac.yml§8!";
			stream = jar.getInputStream(entry);
			StringBuilder stringBuilder = new StringBuilder();
			int i;
			while ((i = stream.read()) != -1)
				stringBuilder.append((char) i);
			return stringBuilder.toString();
		} catch (Exception error) {
			return "%ERROR%§8[§c" + addonName + "§8] §7Empty §8/ §7not existing §4§nrac.yml§8!";
		}
	}
}