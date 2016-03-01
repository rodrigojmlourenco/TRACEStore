package org.trace.store.middleware.drivers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;

public class SessionDriverImpl implements SessionDriver{

	private Connection conn;
	private SessionDriverImpl() {
		conn = MariaDBDriver.getMariaConnection();
	}
	
	private static SessionDriverImpl DRIVER = new SessionDriverImpl();
	
	public static SessionDriver getDriver(){ return DRIVER; }
	
	@Override
	public void openTrackingSession(int userId, String sessionToken) throws UnableToPerformOperation {
		try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO sessions (UserID, Session) VALUES (?,?)");
			stmt.setInt(1, userId);
			stmt.setString(2, sessionToken);
			stmt.executeQuery();
			stmt.close();
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
	}

	@Override
	public void closeTrackingSession(String sessionToken) throws UnableToPerformOperation {
		int modified = 0;
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET IsClosed=1 WHERE Session=?");
			stmt.setString(1, sessionToken);
			modified = stmt.executeUpdate();
			stmt.close();
			
			if(modified <= 0)
				System.err.println("Nothing was modified for session "+sessionToken);
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
	}

	@Override
	public boolean isTrackingSessionClosed(String sessionToken) throws UnableToPerformOperation, SessionNotFoundException {
		
		PreparedStatement stmt;
		
		try {
			stmt = conn.prepareStatement("SELECT IsClosed FROM sessions WHERE Session=?");
			stmt.setString(1, sessionToken);
			ResultSet results = stmt.executeQuery();
			
			stmt.close();
			
			if(results.next())
				return results.getBoolean(1);
			else
				throw new SessionNotFoundException(sessionToken);
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean trackingSessionExists(String sessionToken) throws UnableToPerformOperation {
		
		PreparedStatement stmt;
		
		try {
			stmt = conn.prepareStatement("SELECT * FROM sessions WHERE Session=?");
			stmt.setString(1, sessionToken);
			ResultSet results = stmt.executeQuery();
			
			stmt.close();
			
			return results.next();
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public void clearTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getAllTrackingSessions() throws UnableToPerformOperation {
		
		Statement stmt;
		List<String> sessions = new ArrayList<>();
		
		try {
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT Session FROM sessions");
			
			while(result.next()){
				sessions.add(result.getString(1));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
	}

	@Override
	public List<String> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<String> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session FROM sessions WHERE UserId=? AND IsClosed=1");
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();
			
			while(result.next()){
				sessions.add(result.getString(1));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<String> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<String> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session FROM sessions WHERE CreatedAt > ? AND IsClosed=1");
			stmt.setDate(1, new java.sql.Date(date.getTime()));
			ResultSet result = stmt.executeQuery();
			
			while(result.next()){
				sessions.add(result.getString(1));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<String> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<String> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session FROM sessions WHERE CreatedAt <= ? AND IsClosed=1");
			stmt.setDate(1, new java.sql.Date(date.getTime()));
			ResultSet result = stmt.executeQuery();
			
			while(result.next()){
				sessions.add(result.getString(1));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	
	@Override
	public List<String> getAllClosedTrackingSessions() throws UnableToPerformOperation{
		Statement stmt;
		List<String> sessions = new ArrayList<>();
		
		try {
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT Session FROM sessions WHERE IsClosed=1");
			
			while(result.next()){
				sessions.add(result.getString(1));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
		
	}
	
	protected void clearAll() throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeQuery("DELETE FROM sessions");
		stmt.close();
	}

	public static void main(String[] args){
		SessionDriverImpl driver = (SessionDriverImpl) SessionDriverImpl.getDriver();
		
		try {
			driver.clearAll();
			driver.openTrackingSession(10, "abcd1");
			driver.openTrackingSession(10, "abcd2");
			driver.openTrackingSession(10, "abcd3");
			driver.openTrackingSession(10, "abcd4");
			driver.closeTrackingSession("abcd2");
			driver.closeTrackingSession("abcd4");
			
			System.out.println("Listing closed sessions...");
			for(String s : driver.getAllUserTrackingSessions(10))
				System.out.println(s);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnableToPerformOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
