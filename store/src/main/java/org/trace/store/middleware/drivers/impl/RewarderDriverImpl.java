package org.trace.store.middleware.drivers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.trace.DBAPI.data.SimpleSession;
import org.trace.store.middleware.drivers.RewarderDriver;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;

public class RewarderDriverImpl implements RewarderDriver{

	private Logger LOG = Logger.getLogger(RewarderDriverImpl.class);
	
	private Connection conn;
	private RewarderDriverImpl() {
		conn = MariaDBDriver.getMariaConnection();
	}
	
	private static RewarderDriverImpl DRIVER = new RewarderDriverImpl();
	
	public static RewarderDriver getDriver(){ return DRIVER; }

	@Override
	public List<String> getUsersWithDistance(double distance) throws UnableToPerformOperation {
		
		PreparedStatement stmt;
		List<String> userIDs = new ArrayList<>();
		try {
			stmt = conn.prepareStatement("SELECT a.UserId FROM (SELECT UserId, sum(distance) AS TotalDistance FROM sessions GROUP BY UserId) AS a WHERE a.TotalDistance >= ?;");
			stmt.setDouble(1, distance);
			ResultSet result = stmt.executeQuery();
			
			String userId;
			while(result.next()){
				userIDs.add(result.getString(1));
			}
			stmt.close();
			return userIDs;
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

//	@Override
//	public List<String> getUsersWithKilometers(double kilometers) throws UnableToPerformOperation {
//		try {
//			PreparedStatement stmt = conn.prepareStatement("INSERT INTO sessions (UserID, Session) VALUES (?,?)");
//			stmt.setInt(1, userId);
//			stmt.setString(2, sessionToken);
//			stmt.executeQuery();
//			stmt.close();
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
	
//	@Override
//	public void openTrackingSession(int userId, String sessionToken) throws UnableToPerformOperation {
//		try {
//			PreparedStatement stmt = conn.prepareStatement("INSERT INTO sessions (UserID, Session) VALUES (?,?)");
//			stmt.setInt(1, userId);
//			stmt.setString(2, sessionToken);
//			stmt.executeQuery();
//			stmt.close();
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//		
//	}
//
//	@Override
//	public void closeTrackingSession(String sessionToken) throws UnableToPerformOperation {
//		int modified = 0;
//		try {
//			PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET IsClosed=1 WHERE Session=?");
//			stmt.setString(1, sessionToken);
//			modified = stmt.executeUpdate();
//			stmt.close();
//			
//			if(modified <= 0)
//				LOG.error("Nothing was modified in session "+sessionToken);
//			else
//				LOG.info(sessionToken +" successfully closed.");
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//		
//	}
//	
//	@Override
//	public void reopenTrackingSession(String sessionToken) throws UnableToPerformOperation {
//		
//		int modified = 0;
//		try {
//			PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET IsClosed=0 WHERE Session=?");
//			stmt.setString(1, sessionToken);
//			modified = stmt.executeUpdate();
//			stmt.close();
//			
//			if(modified <= 0)
//				LOG.error("Nothing was modified in session "+sessionToken);
//			else
//				LOG.info(sessionToken +" successfully reopenened.");
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//
//	@Override
//	public boolean isTrackingSessionClosed(String sessionToken) throws UnableToPerformOperation, SessionNotFoundException {
//		
//		PreparedStatement stmt;
//		
//		try {
//			stmt = conn.prepareStatement("SELECT IsClosed FROM sessions WHERE Session=?");
//			stmt.setString(1, sessionToken);
//			ResultSet results = stmt.executeQuery();
//			
//			stmt.close();
//			
//			if(results.next())
//				return results.getBoolean(1);
//			else
//				throw new SessionNotFoundException(sessionToken);
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//
//	@Override
//	public boolean trackingSessionExists(String sessionToken) throws UnableToPerformOperation {
//		
//		PreparedStatement stmt;
//		
//		try {
//			stmt = conn.prepareStatement("SELECT * FROM sessions WHERE Session=?");
//			stmt.setString(1, sessionToken);
//			ResultSet results = stmt.executeQuery();
//			
//			stmt.close();
//			
//			return results.next();
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//
//	@Override
//	public void clearTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public List<SimpleSession> getAllTrackingSessions() throws UnableToPerformOperation {
//		
//		Statement stmt;
//		List<SimpleSession> sessions = new ArrayList<>();
//		
//		try {
//			stmt = conn.createStatement();
//			ResultSet result = stmt.executeQuery("SELECT Session, CreatedAt FROM sessions");
//			
//			String session;
//			Long timestamp;
//			while(result.next()){
//				session = result.getString(1);
//				timestamp = result.getDate(2).getTime();
//				sessions.add(new SimpleSession(session, timestamp));
//			}
//			
//			stmt.close();
//			
//			return sessions;
//			
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//		
//	}
//
//	@Override
//	public List<SimpleSession> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation {
//		PreparedStatement stmt;
//		List<SimpleSession> sessions = new ArrayList<>();
//		
//		try {
//			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE UserId=? AND IsClosed=1 ORDER BY CreatedAt DESC");
//			stmt.setInt(1, userId);
//			ResultSet result = stmt.executeQuery();
//			
//			String session;
//			Long timestamp;
//			while(result.next()){
//				session = result.getString(1);
//				timestamp = result.getDate(2).getTime();
//				sessions.add(new SimpleSession(session, timestamp));
//			}
//			
//			stmt.close();
//			
//			return sessions;
//			
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//
//	@Override
//	public List<SimpleSession> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation {
//		PreparedStatement stmt;
//		List<SimpleSession> sessions = new ArrayList<>();
//		
//		try {
//			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE CreatedAt > ? AND IsClosed=1");
//			stmt.setDate(1, new java.sql.Date(date.getTime()));
//			ResultSet result = stmt.executeQuery();
//			
//			String session;
//			Long timestamp;
//			while(result.next()){
//				session = result.getString(1);
//				timestamp = result.getDate(2).getTime();
//				sessions.add(new SimpleSession(session, timestamp));
//			}
//			
//			stmt.close();
//			
//			return sessions;
//			
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//
//	@Override
//	public List<SimpleSession> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
//		PreparedStatement stmt;
//		List<SimpleSession> sessions = new ArrayList<>();
//		
//		try {
//			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE CreatedAt <= ? AND IsClosed=1");
//			stmt.setDate(1, new java.sql.Date(date.getTime()));
//			ResultSet result = stmt.executeQuery();
//			
//			String session;
//			Long timestamp;
//			while(result.next()){
//				session = result.getString(1);
//				timestamp = result.getDate(2).getTime();
//				sessions.add(new SimpleSession(session, timestamp));
//			}
//			
//			stmt.close();
//			
//			return sessions;
//			
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//	}
//	
//	
//	@Override
//	public List<SimpleSession> getAllClosedTrackingSessions() throws UnableToPerformOperation{
//		Statement stmt;
//		List<SimpleSession> sessions = new ArrayList<>();
//		
//		try {
//			stmt = conn.createStatement();
//			ResultSet result = stmt.executeQuery("SELECT Session, CreatedAt FROM sessions WHERE IsClosed=1");
//			
//			String session;
//			Long timestamp;
//			while(result.next()){
//				session = result.getString(1);
//				timestamp = result.getDate(2).getTime();
//				sessions.add(new SimpleSession(session, timestamp));
//			}
//			
//			stmt.close();
//			
//			return sessions;
//			
//			
//		} catch (SQLException e) {
//			throw new UnableToPerformOperation(e.getMessage());
//		}
//		
//		
//	}
//	
//	protected void clearAll() throws SQLException {
//		Statement stmt = conn.createStatement();
//		stmt.executeQuery("DELETE FROM sessions");
//		stmt.close();
//	}
//
//	public static void main(String[] args){
//		RewarderDriverImpl driver = (RewarderDriverImpl) RewarderDriverImpl.getDriver();
//		
//		try {
//			driver.clearAll();
//			driver.openTrackingSession(10, "abcd1");
//			driver.openTrackingSession(10, "abcd2");
//			driver.openTrackingSession(10, "abcd3");
//			driver.openTrackingSession(10, "abcd4");
//			driver.closeTrackingSession("abcd2");
//			driver.closeTrackingSession("abcd4");
//			
//			System.out.println("Listing closed sessions...");
//			for(SimpleSession s : driver.getAllUserTrackingSessions(10))
//				System.out.println(s.toString());
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnableToPerformOperation e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

	
}
