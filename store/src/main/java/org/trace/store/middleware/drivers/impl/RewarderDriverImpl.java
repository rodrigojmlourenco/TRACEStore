package org.trace.store.middleware.drivers.impl;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.trace.store.middleware.drivers.RewarderDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.services.api.data.Reward;
import org.trace.store.services.api.data.Shop;
import org.trace.store.services.api.data.ShopDetailed;
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
	
	private String createCycleToShopCondition() {
		JsonObject condition = new JsonObject();
		return condition.toString();
	}

	@Override
	public boolean registerDistanceBasedReward(int shopId, double distance, String reward)
			throws UnableToPerformOperation {

		PreparedStatement stmt;

		try {
			stmt = conn.prepareStatement("INSERT INTO challenges (shopId, Conditions, Reward) VALUES (?,?,?)");

			stmt.setInt(1, shopId);
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
	public boolean registerCycleToShopReward(int shopId, String reward) throws UnableToPerformOperation {

		PreparedStatement stmt;

		try {
			stmt = conn.prepareStatement("INSERT INTO challenges (shopId, Conditions, Reward) VALUES (?,?,?)");

			stmt.setInt(1, shopId);
			stmt.setString(2, createCycleToShopCondition());
			stmt.setString(3, reward);

			ResultSet set = stmt.executeQuery();
			stmt.close();

			return true;

		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<TraceReward> getAllShopRewards(int shopId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<TraceReward> rewards = new ArrayList<>();
		try {
			stmt = conn.prepareStatement("SELECT Id, Conditions, Reward FROM challenges WHERE shopId=?");
			stmt.setInt(1, shopId);
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
			stmt = conn.prepareStatement(
					"SELECT * FROM shops join challenges on shops.id = challenges.shopId where ownerId=? and challenges.id=?");
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
			stmt = conn.prepareStatement("DELETE FROM challenges WHERE Id=?");
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

	@Override
	public int getShopId(int userId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		boolean owns = false;

		try {
			stmt = conn.prepareStatement("SELECT id FROM shops WHERE ownerId=?");
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();

			owns = result.next();
			stmt.close();

			if (owns) {
				return result.getInt(1);
			} else {
				return -1;
			}
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<Integer> getShopsIds(int userId) throws UnableToPerformOperation {
		throw new UnsupportedOperationException();
	}

	@Override
	public Shop getShopDetails(int shopId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		boolean exists = false;
		Shop shop = null;

		try {
			stmt = conn.prepareStatement("SELECT ownerId, name, branding, latitude, longitude FROM shops WHERE id=?");
			stmt.setInt(1, shopId);
			ResultSet result = stmt.executeQuery();

			exists = result.next();
			stmt.close();

			if (exists) {
				int ownerId = result.getInt(1);
				String name = result.getString(2);
				String branding = result.getString(3);
				double latitude = result.getDouble(4);
				double longitude = result.getDouble(5);

				shop = new Shop(shopId, ownerId, name, branding, latitude, longitude);
				return shop;
			}
			throw new UnableToPerformOperation("Shop doesn't exit. (shopId: " + shopId + ")");
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	// returns a list of Shop objects, with their corresponding details, based
	// on the given list of shops ids.
	@Override
	public List<Shop> getShopsDetails(List<Integer> shopsIds) throws UnableToPerformOperation {
		PreparedStatement stmt;
		boolean exists = false;
		List<Shop> shops = new ArrayList<>();

		// cancel in case the list is empty
		if (shopsIds == null || shopsIds.isEmpty()) {
			throw new UnableToPerformOperation("shopsIds list is empty!");
		}

		String ids = "";

		for (int count = 0; count < shopsIds.size() - 1; count++) {
			ids += "id = " + shopsIds.get(count) + " or ";

		}
		ids += "id = " + shopsIds.get(shopsIds.size() - 1);

		try {
			stmt = conn.prepareStatement(
					"SELECT id, ownerId, name, branding, latitude, longitude FROM shops WHERE " + ids);
			ResultSet result = stmt.executeQuery();

			stmt.close();

			while (result.next()) {
				shops.add(new Shop(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4),
						result.getDouble(5), result.getDouble(6)));
			}
			return shops;

		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<TraceReward> getRewardDetails(int shopId) throws UnableToPerformOperation {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ShopDetailed> getDetailedShops(List<String> shopIds) throws UnableToPerformOperation {

		PreparedStatement stmt;
		StringBuilder builder = new StringBuilder();
		List<ShopDetailed> detailedShops = new ArrayList<ShopDetailed>();

		for (String id : shopIds)
			builder.append("?,");
		builder.deleteCharAt(builder.length() - 1);

		try {
			String query = "Select shops.Id as shopId, challenges.Id as rewardId, Name, Branding, Latitude, Longitude, conditions, reward, shops.LogoUrl as logoUrl, shops.MapUrl as mapUrl "
					+ "FROM shops JOIN challenges ON shops.Id = challenges.ShopId " + "WHERE shops.Id IN (" + builder
					+ ") " + "ORDER BY shops.Id";

			stmt = conn.prepareStatement(query);

			int index = 1;
			for (String id : shopIds) {
				stmt.setInt(index, Integer.valueOf(id));
				index++;
			}

			ResultSet resultSet = stmt.executeQuery();

			ShopDetailed auxShopDetailed = null;
			int auxShopId = -1, shopId, rewardId;
			double latitude, longitude;
			String name, branding, conditions, reward, logoUrl, mapUrl;

			while (resultSet.next()) {

				shopId = resultSet.getInt("shopId");

				// System.out.println("+++------+++++++shopId: " + shopId);

				if (shopId != auxShopId) { // New Shop

					// Step 1 - Create the shop
					rewardId = resultSet.getInt("rewardId");
					name = resultSet.getString("Name");
					branding = resultSet.getString("Branding");
					latitude = resultSet.getDouble("Latitude");
					longitude = resultSet.getDouble("Longitude");
					conditions = resultSet.getString("conditions");
					reward = resultSet.getString("reward");
					logoUrl = resultSet.getString("logoUrl");
					mapUrl = resultSet.getString("mapUrl");
					
					
					auxShopDetailed = new ShopDetailed(shopId, name, branding, latitude, longitude, logoUrl, mapUrl);

					// Step 2 - Add the current reward
					auxShopDetailed.addReward(new Reward(rewardId, conditions, reward));

					// Step 3 - Add the new shop to the shop list
					detailedShops.add(auxShopDetailed);

					// Step 4 - Update the shop auxiliar id (that identifies new
					// shop)
					auxShopId = shopId;

				} else { // The shop is not new, then just add the reward to the
							// current shop

					rewardId = resultSet.getInt("rewardId");
					conditions = resultSet.getString("conditions");
					reward = resultSet.getString("reward");

					auxShopDetailed.addReward(new Reward(rewardId, conditions, reward));
				}
			}

			return detailedShops;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
}
