package org.trace.store.middleware.drivers;

import java.util.List;

import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.services.api.data.Shop;
import org.trace.store.services.api.data.TraceReward;

public interface RewarderDriver {
	
	/**
	 * Returns all the users that have traveled more than the specified kilometers.
	 * @param distance The minimum traveled distance in kilometers.
	 * @return List of all the users that have traveled more than the specified distance
	 * @throws UnableToPerformOperation
	 */
	public List<String> getUsersWithDistance(double distance) throws UnableToPerformOperation;
	
	public int getUserShop(int userId) throws UnableToPerformOperation;
	
	public List<Integer> getUserShops(int userId) throws UnableToPerformOperation;
	
	public boolean registerDistanceBasedReward(int ownerId, double distance, String reward) throws UnableToPerformOperation;

	public List<TraceReward> getAllOwnerRewards(int ownerId) throws UnableToPerformOperation;

	public boolean ownsReward(int ownerId, int rewardId) throws UnableToPerformOperation;

	public boolean unregisterReward(int rewardId) throws UnableToPerformOperation;

	public double getUserDistance(int userId) throws UnableToPerformOperation;
	
	public int registerShop(int ownerId, String name, String branding, double latitude, double longitude) throws UnableToPerformOperation;

	public Shop getShop(int ownerId) throws UnableToPerformOperation;
	
	public boolean updateShop(int ownerId, String name, String branding, double latitude, double longitude) throws UnableToPerformOperation;

}
