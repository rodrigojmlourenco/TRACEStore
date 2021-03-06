package org.trace.store.middleware.drivers;

import java.util.List;

import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.services.api.data.Shop;
import org.trace.store.services.api.data.ShopDetailed;
import org.trace.store.services.api.data.TraceReward;

public interface RewarderDriver {
	
	/**
	 * Returns all the users that have traveled more than the specified kilometers.
	 * @param distance The minimum traveled distance in kilometers.
	 * @return List of all the users that have traveled more than the specified distance
	 * @throws UnableToPerformOperation
	 */
	public List<String> getUsersWithDistance(double distance) throws UnableToPerformOperation;
	
	public int getShopId(int userId) throws UnableToPerformOperation;
	
	public List<Integer> getShopsIds(int userId) throws UnableToPerformOperation;
	
	public boolean registerDistanceBasedReward(int ownerId, double distance, String reward) throws UnableToPerformOperation;

	public List<TraceReward> getAllShopRewards(int shopId) throws UnableToPerformOperation;
	
	public List<TraceReward> getAllOwnerRewards(int ownerId) throws UnableToPerformOperation;

	public boolean ownsReward(int ownerId, int rewardId) throws UnableToPerformOperation;

	public boolean unregisterReward(int rewardId) throws UnableToPerformOperation;

	public double getUserDistance(int userId) throws UnableToPerformOperation;
	
	public int registerShop(int ownerId, String name, String branding, double latitude, double longitude) throws UnableToPerformOperation;
	public int registerShop(int ownerId, String name, String branding, String url, double latitude, double longitude) throws UnableToPerformOperation;

	public Shop getShop(int ownerId) throws UnableToPerformOperation;
	
	public List<Shop> getShops(int ownerId) throws UnableToPerformOperation;

	public boolean updateShop(int id, String name, String branding, double latitude, double longitude) throws UnableToPerformOperation;
	public boolean updateShop(int id, String name, String branding, String url, double latitude, double longitude) throws UnableToPerformOperation;

	public Shop getShopDetails(int shopId) throws UnableToPerformOperation;
	
	public List<Shop> getShopsDetails(List<Integer> shopsIds) throws UnableToPerformOperation;

	public List<TraceReward> getRewardDetails(int shopId) throws UnableToPerformOperation;
	
	public List<ShopDetailed> getDetailedShops(List<String> shopIds) throws UnableToPerformOperation;

	public boolean registerCycleToShopReward(int shopId, String reward) throws UnableToPerformOperation;
	
	public boolean deleteShop(int ownerId, int id) throws UnableToPerformOperation;
}
