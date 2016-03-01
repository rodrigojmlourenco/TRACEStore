package org.trace.store.middleware.drivers.impl;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;

public class SessionDriverImpl implements SessionDriver{

	private Connection conn;
	private SessionDriverImpl() {
		conn = MariaDBDriver.getMariaConnection();
	}
	
	private static SessionDriverImpl DRIVER = new SessionDriverImpl();
	
	public static SessionDriver getDriver(){ return DRIVER; }
	
	@Override
	public void openTrackingSession(String username, String sessionToken) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeTrackingSession(String sessionToken) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTrackingSessionClosed(String sessionToken) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean trackingSessionExists(String sessionToken) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getAllTrackingSessions() throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return null;
	}

}
