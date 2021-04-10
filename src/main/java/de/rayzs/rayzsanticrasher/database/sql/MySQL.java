package de.rayzs.rayzsanticrasher.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;

import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class MySQL {

	public Connection connection;
	String host, database, username, password;
	int port;

	public MySQL(String host, int port, String username, String password, String database) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
		this.port = port;
		if (RayzsAntiCrasher.getInstance().useMySQL())
			connect();
	}

	private void connect() {
		if (!isConnected()) {
			try {
				connection = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username,
						password);
				Bukkit.getConsoleSender().sendMessage("[RayzsAPI | " + database
						+ "] Die Verbindung zur MySQL konnte erfolgreich hergestellt werden!");
			} catch (SQLException error) {
				Bukkit.getConsoleSender()
						.sendMessage("[RayzsAPI | " + database
								+ "] Die Verbindung zur MySQL konnte nicht hergestellt werden\n[RayzsAPI] Grund: "
								+ error.getMessage());
			}
		}
	}

	public void disconnectFromMySQL() {
		if (isConnected())
			try {
				connection.close();
				Bukkit.getConsoleSender().sendMessage("[RayzsAPI] Die MySQL Verbindung wurde getrennt!");
			} catch (SQLException error) {
			}
		else
			Bukkit.getConsoleSender().sendMessage("[RayzsAPI] Die MySQL Verbindung war nie vorhanden!");
	}

	public ResultSet result(String query) {
		ResultSet resultSet = null;
		try {
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
		} catch (SQLException error) {
		}
		return resultSet;
	}

	public void set(String qry) {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(qry);
			statement.close();
		} catch (SQLException error) {
			connect();
			System.err.println(error);
		}
	}

	public boolean isConnected() {
		return (connection != null);
	}

	public Connection getConnection() {
		return connection;
	}
}