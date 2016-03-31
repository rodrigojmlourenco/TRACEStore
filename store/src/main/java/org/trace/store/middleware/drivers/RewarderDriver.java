package org.trace.store.middleware.drivers;

import java.util.Date;
import java.util.List;

import org.trace.DBAPI.data.SimpleSession;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;

public interface RewarderDriver {
	
	/**
	 * Returns all the users that have traveled more than the specified kilometers.
	 * @param distance The minimum traveled distance in kilometers.
	 * @return List of all the users that have traveled more than the specified distance
	 * @throws UnableToPerformOperation
	 */
	public List<String> getUsersWithDistance(double distance) throws UnableToPerformOperation;
	
	public boolean registerDistanceBasedReward(int ownerId, double distance, String reward) throws UnableToPerformOperation;
}
