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
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.services.api.Location;
import org.trace.store.services.api.data.TrackSummary;

public class SessionDriverImpl implements SessionDriver{

	private Logger LOG = Logger.getLogger(SessionDriverImpl.class);
	
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
				LOG.error("Nothing was modified in session "+sessionToken);
			else
				LOG.info(sessionToken +" successfully closed.");
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	@Override
	public void updateSessionDistance(String sessionToken, double distance) throws UnableToPerformOperation {
		int modified = 0;
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET Distance=? WHERE Session=?");
			stmt.setDouble(1, distance);
			stmt.setString(2, sessionToken);

			modified = stmt.executeUpdate();
			stmt.close();
			
			if(modified <= 0)
				LOG.error("Nothing was modified in session "+sessionToken);
			else
				LOG.info(sessionToken +" successfully closed.");
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	@Override
	public void reopenTrackingSession(String sessionToken) throws UnableToPerformOperation {
		
		int modified = 0;
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET IsClosed=0 WHERE Session=?");
			stmt.setString(1, sessionToken);
			modified = stmt.executeUpdate();
			stmt.close();
			
			if(modified <= 0)
				LOG.error("Nothing was modified in session "+sessionToken);
			else
				LOG.info(sessionToken +" successfully reopenened.");
			
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
	public List<SimpleSession> getAllTrackingSessions() throws UnableToPerformOperation {
		
		Statement stmt;
		List<SimpleSession> sessions = new ArrayList<>();
		
		try {
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT Session, CreatedAt FROM sessions");
			
			String session;
			Long timestamp;
			while(result.next()){
				session = result.getString(1);
				timestamp = result.getDate(2).getTime();
				sessions.add(new SimpleSession(session, timestamp));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
	}

	@Override
	public List<SimpleSession> getAllUserTrackingSessions(int userId) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<SimpleSession> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE UserId=? AND IsClosed=1 ORDER BY CreatedAt DESC");
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();
			
			String session;
			Long timestamp;
			while(result.next()){
				session = result.getString(1);
				timestamp = result.getDate(2).getTime();
				sessions.add(new SimpleSession(session, timestamp));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<SimpleSession> getAllTrackingSessionsCreatedAfter(Date date) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<SimpleSession> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE CreatedAt > ? AND IsClosed=1");
			stmt.setDate(1, new java.sql.Date(date.getTime()));
			ResultSet result = stmt.executeQuery();
			
			String session;
			Long timestamp;
			while(result.next()){
				session = result.getString(1);
				timestamp = result.getDate(2).getTime();
				sessions.add(new SimpleSession(session, timestamp));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public List<SimpleSession> getAllTrackingSessionsCreatedBefore(Date date) throws UnableToPerformOperation {
		PreparedStatement stmt;
		List<SimpleSession> sessions = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("SELECT Session, CreatedAt FROM sessions WHERE CreatedAt <= ? AND IsClosed=1");
			stmt.setDate(1, new java.sql.Date(date.getTime()));
			ResultSet result = stmt.executeQuery();
			
			String session;
			Long timestamp;
			while(result.next()){
				session = result.getString(1);
				timestamp = result.getDate(2).getTime();
				sessions.add(new SimpleSession(session, timestamp));
			}
			
			stmt.close();
			
			return sessions;
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	
	@Override
	public List<SimpleSession> getAllClosedTrackingSessions() throws UnableToPerformOperation{
		Statement stmt;
		List<SimpleSession> sessions = new ArrayList<>();
		
		try {
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT Session, CreatedAt FROM sessions WHERE IsClosed=1");
			
			String session;
			Long timestamp;
			while(result.next()){
				session = result.getString(1);
				timestamp = result.getDate(2).getTime();
				sessions.add(new SimpleSession(session, timestamp));
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
	
	@Override
	public void registerTrackSummary(TrackSummary summary) throws UnableToPerformOperation {
		
		try {
			
			PreparedStatement stmt = 
					conn.prepareStatement(
							"INSERT INTO session_details ("
							+ "session, "
							+ "startedAt, endedAt, "
							+ "elapsedTime, elapsedDistance, "
							+ "avgSpeed, topSpeed, "
							+ "point, "
							+ "modality) VALUES (?,?,?,?,?,?,?,?,?)");
			
			stmt.setString(1, summary.getSession());
			stmt.setLong(2, summary.getStartedAt());
			stmt.setLong(3, summary.getEndedAt());
			stmt.setInt(4, summary.getElapsedTime());
			stmt.setInt(5, summary.getElapsedDistance());
			stmt.setFloat(6, summary.getAvgSpeed());
			stmt.setFloat(7, summary.getTopSpeed());
			stmt.setInt(8, summary.getPoints());
			stmt.setInt(9, summary.getModality());
		
			stmt.execute();
			
			stmt.close();
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
	}
	
	
	/*
	 * VERSION 2.0 - New Ijsberg functions
	 */
	@Override
	public TrackSummary getTrackSummary(String session) throws UnableToPerformOperation {
		
		TrackSummary summary = new TrackSummary();
		
		try {
			PreparedStatement stmt = 
					conn.prepareStatement("SELECT "
							+ "session, startedAt, endedAt, "
							+ "elapsedTime, elapsedDistance, "
							+ "avgSpeed, topSpeed, "
							+ "points, "
							+ "modality "
							+ "FROM sessions_details WHERE session = ?");
			
			stmt.setString(1, session);
			
			ResultSet set = stmt.executeQuery();
					
			if(set.next()){
				summary.setSession(set.getString(1));
				summary.setStartedAt(set.getLong(2));
				summary.setEndedAt(set.getLong(3));
				summary.setElapsedTime(set.getInt(4));
				summary.setElapsedTime(set.getInt(5));
				summary.setAvgSpeed(set.getFloat(6));
				summary.setPoints(set.getInt(7));
				summary.setModality(set.getInt(8));
			}
			
			stmt.close();
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
		return summary;
	}
	
	@Override
	public List<TrackSummary> getUsersTrackSummaries(int userId) throws UnableToPerformOperation {
		
		List<TrackSummary> summaries = new ArrayList<>();
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT Sessions.Session, startedAt, endedAt, elapsedTime, elapsedDistance, avgSpeed, topSpeed, points, modality ");
		query.append("FROM sessions JOIN sessions_details ON sessions.Session = sessions_details.session ");
		query.append("WHERE sessions.UserId = ?");
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			stmt.setInt(1, userId);
			
			ResultSet set = stmt.executeQuery();
			
			TrackSummary aux;
			while(set.next()){
				aux = new TrackSummary();
				
				aux.setSession(set.getString(1));
				aux.setStartedAt(set.getLong(2));
				aux.setEndedAt(set.getLong(3));
				aux.setElapsedTime(set.getInt(4));
				aux.setElapsedDistance(set.getInt(5));
				aux.setAvgSpeed(set.getFloat(6));
				aux.setTopSpeed(set.getFloat(7));
				aux.setPoints(set.getInt(8));
				aux.setModality(set.getInt(9));
				
				summaries.add(aux);
			}
			
			
			stmt.close();
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
		return summaries;
	}

	@Override
	public void deleteTrackSummary(String session) throws UnableToPerformOperation {
		
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM sessions_details WHERE session = ?");
		
		try {
			PreparedStatement stmt = conn.prepareStatement("");
			stmt.setString(1, session);
			
			int affected = stmt.executeUpdate();
			
			if(affected <= 0){
				throw new UnableToPerformOperation("No session with session identifier "+session+" was found : deleteTrackSummary@SessionDriverImpl");
			}
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
		
		
	}

	@Override
	public void deleteUserTrackSummaries(int userId) throws UnableToPerformOperation {
		
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM sessions_details WHERE session IN ( ");
		query.append("SELECT sessions.Session as session FROM sessions WHERE UserId = ?)" );
		
		try {
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			stmt.setInt(1, userId);
			
			int affected = stmt.executeUpdate();
			
			if(affected <= 0){
				throw new UnableToPerformOperation("No sessions for user "+userId+" were found : deleteUserTrackSummaries@SessionDriverImpl");
			}
			
			
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage()); 
		}
		
		
	}

	@Override
	public void addTrackTraceBatch(String session, List<Location> trace) throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Location> getTrackTrace() throws UnableToPerformOperation {
		// TODO Auto-generated method stub
		return null;
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
			for(SimpleSession s : driver.getAllUserTrackingSessions(10))
				System.out.println(s.toString());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnableToPerformOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
