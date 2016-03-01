package org.trace.store.middleware.drivers;

import java.util.Date;
import java.util.List;

import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;

public interface SessionDriver {
	
	/**
	 * Creates a new tracking session for the specified user.
	 * @param username The user's username.
	 * @param sessionToken The tracking session pseudonym.
	 * @throws UnableToPerformOperation
	 */
	public void openTrackingSession(String username, String sessionToken) throws UnableToPerformOperation;
	
	/**
	 * Closes a currently active tracking session identified by its session token.
	 * @param sessionToken The session's token.
	 * @throws UnableToPerformOperation
	 */
	public void closeTrackingSession(String sessionToken) throws UnableToPerformOperation;
	
	/**
	 * Checks if the tracking session identified by the session token is closed or still active.
	 * @param sessionToken The tracking session's token.
	 * @return True is the tracking session is closed, false otherwise.
	 */
	public boolean isTrackingSessionClosed(String sessionToken) throws UnableToPerformOperation;
	
	/**
	 * Checks is a tracking session exists for the specified session token.
	 * @param sessionToken The tracking session token.
	 * @return True if the tracking session is registered, false otherwise.
	 * 
	 * @throws UnableToPerformOperation
	 */
	public boolean trackingSessionExists(String sessionToken) throws UnableToPerformOperation;
	
	/**
	 * Removes all tracking sessions created before the specified date.
	 * @param date The date.
	 * @throws UnableToPerformOperation
	 */
	public void clearTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions' tokens.
	 * @return List of all tracking sessions.
	 * @throws UnableToPerformOperation
	 */
	public List<String> getAllTrackingSessions() throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions' tokens associeted with a specific user.
	 * @param userId The user's identifier.
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<String> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions created after the specified date.
	 * @param date The date
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<String> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions created before the specified date.
	 * @param date The date
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<String> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation;

}
