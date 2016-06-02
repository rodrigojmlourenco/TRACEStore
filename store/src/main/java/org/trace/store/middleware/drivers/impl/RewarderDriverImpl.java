package org.trace.store.middleware.drivers.impl;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.trace.store.middleware.drivers.RewarderDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.services.api.data.Shop;
import org.trace.store.services.api.data.TraceReward;

import com.google.gson.JsonObject;

public class RewarderDriverImpl implements RewarderDriver {

	private Logger LOG = Logger.getLogger(RewarderDriverImpl.class);

	private Connection conn;

	private RewarderDriverImpl() {
		conn = MariaDBDriver.getMariaConnection();
	}

	private static RewarderDriverImpl DRIVER = new RewarderDriverImpl();

	public static RewarderDriver getDriver() {
		return DRIVER;
	}

	@Override
	public List<String> getUsersWithDistance(double distance) throws UnableToPerformOperation {

		PreparedStatement stmt;
		List<String> userIDs = new ArrayList<>();
		try {
			stmt = conn.prepareStatement(
					"SELECT a.UserId FROM (SELECT UserId, sum(distance) AS TotalDistance FROM sessions GROUP BY UserId) AS a WHERE a.TotalDistance >= ?;");
			stmt.setDouble(1, distance);
			ResultSet result = stmt.executeQuery();

			while (result.next()) {
				userIDs.add("" + result.getInt(1));
			}
			stmt.close();
			return userIDs;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public double getUserDistance(int userId) throws UnableToPerformOperation {

		PreparedStatement stmt;
		List<String> userIDs = new ArrayList<>();
		try {
			// stmt = conn.prepareStatement("SELECT a.UserId FROM (SELECT
			// UserId, sum(distance) AS TotalDistance FROM sessions GROUP BY
			// UserId) AS a WHERE a.TotalDistance >= ?;");
			stmt = conn.prepareStatement("SELECT sum(distance) FROM sessions WHERE UserId = ?;");

			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();

			result.next();
			double distance = result.getDouble(1);
			stmt.close();
			return distance;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	private String createDistanceCondition(double distance) {
		JsonObject condition = new JsonObject();
		condition.addProperty("distance", distance);
		return condition.toString();

	}

	@Override
	public boolean registerDistanceBasedReward(int userId, double distance, String reward)
			throws UnableToPerformOperation {

		PreparedStatement stmt;

		try {
			stmt = conn.prepareStatement("INSERT INTO rewards (OwnerId, Conditions, Reward) VALUES (?,?,?)");

			stmt.setInt(1, userId);
			stmt.setString(2, createDistanceCondition(distance));
			stmt.setString(3, reward);

			ResultSet set = stmt.executeQuery();
			stmt.close();

			return true;

		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<TraceReward> getAllOwnerRewards(int ownerId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<TraceReward> rewards = new ArrayList<>();
		try {
			stmt = conn.prepareStatement("SELECT Id, Conditions, Reward FROM rewards WHERE OwnerId=?");
			stmt.setInt(1, ownerId);
			ResultSet result = stmt.executeQuery();

			while (result.next()) {
				int id = result.getInt(1);
				String conditions = result.getString(2);
				String reward = result.getString(3);
				rewards.add(new TraceReward(id, conditions, reward));
			}
			stmt.close();
			return rewards;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean ownsReward(int ownerId, int rewardId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		boolean owns = false;

		try {
			stmt = conn.prepareStatement("SELECT * FROM rewards WHERE OwnerId=? AND Id=?");
			stmt.setInt(1, ownerId);
			stmt.setInt(2, rewardId);
			ResultSet result = stmt.executeQuery();

			owns = result.next();

			stmt.close();
			return owns;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean unregisterReward(int rewardId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("DELETE FROM rewards WHERE Id=?");
			stmt.setInt(1, rewardId);
			int result = stmt.executeUpdate();
			stmt.close();
			return result >= 1;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public int registerShop(int ownerId, String name, String branding, double latitude, double longitude)
			throws UnableToPerformOperation {

		PreparedStatement stmt;

		try {
			stmt = conn.prepareStatement(
					"INSERT INTO shops (OwnerId, Name, Branding, Latitude, Longitude) VALUES (?,?,?,?,?)",
					java.sql.Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1, ownerId);
			stmt.setString(2, name);
			stmt.setString(3, branding);
			stmt.setDouble(4, latitude);
			stmt.setDouble(5, longitude);

			ResultSet set = stmt.executeQuery();
			int genKey = -1;

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					genKey = generatedKeys.getInt(1);
				} else {
					throw new SQLException("Creating user failed, no ID obtained.");
				}
			}

			// Log.info("genKey:" + genKey);
			stmt.close();

			return genKey;

		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public Shop getShop(int ownerId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		Shop shop = null;

		try {
			stmt = conn.prepareStatement("SELECT * FROM TRACE.shops where OwnerId=?;");

			stmt.setInt(1, ownerId);

			ResultSet result = stmt.executeQuery();

			if (result.next()) {

				int id = result.getInt(1);
				String name = result.getString(3);
				String branding = result.getString(4);
				double latitude = result.getDouble(5);
				double longitude = result.getDouble(6);

				shop = new Shop(id, ownerId, name, branding, latitude, longitude);
			}
			stmt.close();
			return shop;

		} catch (SQLException e) {
			Log.error("SQLException: " + e.getMessage());
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean updateShop(int ownerId, String name, String branding, double latitude, double longitude)
			throws UnableToPerformOperation {
		PreparedStatement stmt;

		try {
			stmt = conn
					.prepareStatement("UPDATE shops SET Name=?, Branding=?, Latitude=?, Longitude=? where OwnerId=?;");
			stmt.setString(1, name);
			stmt.setString(2, branding);
			stmt.setDouble(3, latitude);
			stmt.setDouble(4, longitude);
			stmt.setInt(5, ownerId);

			int result = stmt.executeUpdate();
			stmt.close();

			return result >= 1;

		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}

	}
}
