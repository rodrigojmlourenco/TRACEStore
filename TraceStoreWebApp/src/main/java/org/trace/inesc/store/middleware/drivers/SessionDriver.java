package org.trace.inesc.store.middleware.drivers;

import java.util.Date;
import java.util.List;

import org.trace.inesc.store.middleware.exceptions.SessionNotFoundException;
import org.trace.inesc.store.middleware.exceptions.UnableToPerformOperation;
import org.trace.inesc.store.services.data.Location;
import org.trace.inesc.store.services.data.TrackSummary;
import org.trace.inesc.store.storage.data.SimpleSession;

public interface SessionDriver {
	
	/**
	 * Creates a new tracking session for the specified user.
	 * @param username The user's username.
	 * @param sessionToken The tracking session pseudonym.
	 * @throws UnableToPerformOperation
	 */
	public void openTrackingSession(int userId, String sessionToken) throws UnableToPerformOperation;
	
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
	 * @throws SessionNotFoundException 
	 */
	public boolean isTrackingSessionClosed(String sessionToken) throws UnableToPerformOperation, SessionNotFoundException;
	
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
	 * Fetches all tracking sessions' tokens.
	 * @return List of all tracking sessions.
	 * @throws UnableToPerformOperation
	 */
	public List<SimpleSession> getAllTrackingSessions() throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions' tokens.
	 * @return List of all tracking sessions.
	 * @throws UnableToPerformOperation
	 */
	public List<SimpleSession> getAllClosedTrackingSessions() throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions' tokens associeted with a specific user.
	 * @param userId The user's identifier.
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<SimpleSession> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions created after the specified date.
	 * @param date The date
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<SimpleSession> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation;
	
	/**
	 * Fetches all closed tracking sessions created before the specified date.
	 * @param date The date
	 * @return List of tracking session tokens.
	 * @throws UnableToPerformOperation
	 */
	public List<SimpleSession> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation;

	void reopenTrackingSession(String sessionToken) throws UnableToPerformOperation;

	void updateSessionDistance(String sessionToken, double distance) throws UnableToPerformOperation;

	/*
	 * VERSION 2.0 - New Ijsberg functions
	 */
	public void registerTrackSummary(int userId, TrackSummary summary) throws UnableToPerformOperation;
	
	public TrackSummary getTrackSummary(String session) throws UnableToPerformOperation;
	
	public List<TrackSummary> getUsersTrackSummaries(int userId) throws UnableToPerformOperation;
	
	public void deleteTrackSummary(String session) throws UnableToPerformOperation;
	
	public void deleteUserTrackSummaries(int userId) throws UnableToPerformOperation;
	
	public void addTrackTraceBatch(String session, Location[] trace) throws UnableToPerformOperation;
	
	public List<Location> getTrackTrace(String session) throws UnableToPerformOperation;
	
	public boolean isCompleteRoute(String session) throws UnableToPerformOperation;
}
