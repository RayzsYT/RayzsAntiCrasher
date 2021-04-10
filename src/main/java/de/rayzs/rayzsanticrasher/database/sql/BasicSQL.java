package de.rayzs.rayzsanticrasher.database.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.Bukkit;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class BasicSQL {

	String tableName = "";
	String subSettings = "";
	private RayzsAntiCrasher instance;

	public BasicSQL(String tableName, String headSettings, String subSettings) {
		this.instance = RayzsAntiCrasher.getInstance();
		this.subSettings = subSettings;
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				instance.getMySQL().set("CREATE TABLE IF NOT EXISTS " + tableName + " (" + headSettings + ");");
			}
		});
		this.tableName = tableName;
	}

	public boolean exist(UUID uuid) {
		String uid = uuid.toString();
		try {
			ResultSet rs = instance.getMySQL().result("SELECT * FROM " + tableName + " WHERE UUID= '" + uid + "'");
			if (rs.next())
				return (rs.getString("UUID") != null);
			return false;
		} catch (SQLException sQLException) { return false; }
	}

	public void create(UUID uuid) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (exist(uuid))
					return;
				instance.getMySQL()
						.set("INSERT INTO " + tableName + " VALUES('" + uuid.toString() + "', " + subSettings + ");");
			}
		});
	}

	public Integer getInteger(UUID uuid, String what) {
		Integer result = 0;
		if (exist(uuid)) {
			create(uuid);
			return result;
		}
		try {
			ResultSet rs = instance.getMySQL()
					.result("SELECT * FROM " + tableName + " WHERE UUID= '" + uuid.toString() + "'");
			if (rs.next())
				result = rs.getInt(what);
		} catch (SQLException error) { }
		return result;
	}

	public String getString(UUID uuid, String what) {
		String result = "//ERROR//";
		if (exist(uuid)) {
			create(uuid);
			return result;
		}
		try {
			ResultSet rs = instance.getMySQL()
					.result("SELECT * FROM " + tableName + " WHERE UUID= '" + uuid.toString() + "'");
			if (rs.next())
				result = rs.getString(what);
		} catch (SQLException error) { }
		return result;
	}

	public void set(UUID uuid, String type, Object obj) {
		Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				if (exist(uuid)) {
					create(uuid);
					set(uuid, type, obj);
					return;
				}
				instance.getMySQL().set("UPDATE " + tableName + " SET " + type + "= '" + obj + "' WHERE UUID= '"
						+ uuid.toString() + "';");
				return;
			}
		});
	}
}