package de.rayzs.rayzsanticrasher.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class FileManager {

	private File file;
	private YamlConfiguration yaml;
	private String search;
	private RayzsAntiCrasher instance;

	public FileManager(File file) {
		instance = RayzsAntiCrasher.getInstance();
		this.file = file;
		yaml = YamlConfiguration.loadConfiguration(file);
		decodeToUTF8();
	}

	public File getFile() {
		return file;
	}

	public boolean exist() {
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public FileManager search(String search) {
		this.search = search;
		return this;
	}

	public int getInt(int defaultInteger) {
		if (yaml.getString(search) != null) {

			return yaml.getInt(search);

		} else {
			setInt(search, defaultInteger);
			return getInt(defaultInteger);
		}
	}

	public YamlConfiguration getYAML() {
		return yaml;
	}

	public String getString(String defaultMessage) {
		try {
			if (yaml.getString(search) != null) {
				String result = ChatColor.translateAlternateColorCodes('�', yaml.getString(search));
				return result;
			} else {
				setString(search, defaultMessage);
				return getString(defaultMessage);
			}
		} catch (Exception e) {
			if (yaml.getString(search) != null) {
				String result = yaml.getString(search);
				return result;
			} else {
				setString(search, defaultMessage);
				return getString(defaultMessage);
			}
		}
	}
	
	public String getString() {
		try {
			if (yaml.getString(search) != null) {
				String result = ChatColor.translateAlternateColorCodes('&', yaml.getString(search));
				return result;
			} else {
				setString(search, "empty");
				return getString("�cerror �8[�4" + search + "�8]");
			}
		} catch (Exception e) {
			if (yaml.getString(search) != null) {
				String result = yaml.getString(search);
				return result;
			} else {
				setString(search, "empty");
				return getString("�cerror �8[�4" + search + "�8]");
			}
		}
	}

	public void set(String path, Object obj) {
		yaml.set(path, obj);
		save();
	}

	public List<String> getStringList(List<String> defaultList) {
		if (yaml.getStringList(search) != null) {
			List<String> result = yaml.getStringList(search);
			return result;
		} else {
			yaml.set(search, defaultList);
			return getStringList(defaultList);
		}
	}

	public boolean getBoolean(boolean defaultBoolean) {
		try {
			if (yaml.getBoolean(search) == true || yaml.getBoolean(search) == false) {
				return yaml.getBoolean(search);
			} else {
				yaml.set(search, defaultBoolean);
				save();
				return defaultBoolean;
			}
		} catch (Exception e) {
			yaml.set(search, defaultBoolean);
			save();
			return defaultBoolean;
		}
	}

	public Material getMaterial(Material defaultMaterial) {
		if (yaml.getString(search) != null) {

			return Material.getMaterial(yaml.getString(search));

		} else {
			setMaterial(search, defaultMaterial);
			return defaultMaterial;
		}
	}

	public Location getLocation() {
		World world = Bukkit.getWorld(yaml.getString(search + "." + "world"));
		double x = yaml.getDouble(search + "." + "x");
		double y = yaml.getDouble(search + "." + "y");
		double z = yaml.getDouble(search + "." + "z");
		int yaw = yaml.getInt(search + "." + "yaw");
		int pitch = yaml.getInt(search + "." + "pitch");
		return new Location(world, x, y, z, yaw, pitch);
	}

	public void setString(String path, String name) {
		try {

			yaml.set(path, name);
			save();

		} catch (Exception e) {
			instance.getLogger().info("Error saving datas!");
		}
	}

	public void setInt(String path, Integer zahl) {
		try {

			yaml.set(path, zahl);
			save();

		} catch (Exception e) {
			instance.getLogger().info("Error saving datas!");
		}
	}

	public void setBoolean(String path, Boolean wahrheit) {
		try {

			yaml.set(path, wahrheit);
			save();

		} catch (Exception e) {
			instance.getLogger().info("Error saving datas!");
		}
	}

	public void setLocation(String path, String name, Location location) {
		yaml.set(path + "." + name + "." + "world", location.getWorld().getName());
		yaml.set(path + "." + name + "." + "x", location.getX());
		yaml.set(path + "." + name + "." + "y", location.getY());
		yaml.set(path + "." + name + "." + "z", location.getZ());
		yaml.set(path + "." + name + "." + "yaw", location.getYaw());
		yaml.set(path + "." + name + "." + "pitch", location.getPitch());
		save();
	}

	public void setMaterial(String path, Material material) {
		try {

			yaml.set(path, material.toString());
			save();

		} catch (Exception e) {
			instance.getLogger().info("Error saving datas!");
		}
	}

	public void save() {
		try {
			yaml.save(file);
			decodeToUTF8();
		} catch (IOException e1) {
			System.err.println("Error saving file " + file.getName() + "!");
		}
	}

	public void decodeToUTF8() {
		try {

			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
			w.write(yaml.saveToString());
			w.close();

		} catch (IOException e) {

		}
	}

	public boolean stringToBooleanConverter(String string) {
		if (string.equalsIgnoreCase("true")) {
			return true;
		} else if (string.equalsIgnoreCase("false")) {
			return false;
		} else {
			return false;
		}
	}
}