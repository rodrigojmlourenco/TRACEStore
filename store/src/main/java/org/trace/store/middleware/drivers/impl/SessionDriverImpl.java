package org.trace.store.middleware.drivers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

	/*
	 * VERSION 2.0 - New Ijsberg functions
	 */
	@Override
	public void registerTrackSummary(int userId, TrackSummary summary) throws UnableToPerformOperation {
		
		String create_session = "INSERT INTO sessions (UserID, Session) VALUES (?,?)";
		String create_summary = 
				"INSERT INTO sessions_details ("
					+ "session, "
					+ "startedAt, endedAt, "
					+ "elapsedTime, elapsedDistance, "
					+ "avgSpeed, topSpeed, "
					+ "points, "
					+ "modality) VALUES (?,?,?,?,?,?,?,?,?)";
					
		PreparedStatement stmtSession = null;
		PreparedStatement stmtSummary = null;
		
		LOG.debug("Registering the track : "+summary.toString());
		
		try{
			conn.setAutoCommit(false);
			
			stmtSession = conn.prepareStatement(create_session);
			stmtSummary = conn.prepareStatement(create_summary);
			
			//Phase 1 - Register the session
			stmtSession.setInt(1, userId);
			stmtSession.setString(2, summary.getSession());
			stmtSession.executeUpdate();
			
			//Phase 2 - Register the Track Summary 
			stmtSummary.setString(1, summary.getSession());
			stmtSummary.setTimestamp(2, new Timestamp(summary.getStartedAt()));
			stmtSummary.setTimestamp(3, new Timestamp(summary.getEndedAt()));
			stmtSummary.setInt(4, summary.getElapsedTime());
			stmtSummary.setDouble(5, summary.getElapsedDistance());
			stmtSummary.setFloat(6, summary.getAvgSpeed());
			stmtSummary.setFloat(7, summary.getTopSpeed());
			stmtSummary.setInt(8, summary.getPoints());
			stmtSummary.setInt(9, summary.getModality());
			stmtSummary.executeUpdate();
			
			//Phase 3 - Commit the transactions
			conn.commit();
			
		}catch(SQLException e){
			
			try{
			if(conn != null){
				LOG.warn("Transaction is being rollbacked : registerTrackSummary@SessionDriver");
				conn.rollback();
			}
			
				throw new UnableToPerformOperation(e.getMessage());
				
			}catch(SQLException e1){
				LOG.error(e1.getMessage());
				
			}
			
			
		} finally {
			
			try{
				if(stmtSession!=null)
					stmtSession.close();
				
				if(stmtSummary!=null)
					stmtSummary.close();
				
				conn.setAutoCommit(true);
			
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
	}
	

	@Override
	public TrackSummary getTrackSummary(String session) throws UnableToPerformOperation {
		
		TrackSummary summary = null;;
		
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
				summary = new TrackSummary();
				summary.setSession(set.getString(1));
				summary.setStartedAt(set.getTimestamp(2).getTime());
				summary.setEndedAt(set.getTimestamp(3).getTime());
				summary.setElapsedTime(set.getInt(4));
				summary.setElapsedDistance(set.getDouble(5));
				summary.setAvgSpeed(set.getFloat(6));
				summary.setTopSpeed(set.getFloat(7));
				summary.setPoints(set.getInt(8));
				summary.setModality(set.getInt(9));
			}else{
				throw new UnableToPerformOperation("Session '"+session+"' does not exist.");
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
		StringBuilder countQuery = new StringBuilder();
		StringBuilder completeSessionsQuery = new StringBuilder();
		//query.append("SELECT sessions.Session, startedAt, endedAt, elapsedTime, elapsedDistance, avgSpeed, topSpeed, points, modality ");
		//query.append("FROM sessions JOIN sessions_details ON sessions.Session = sessions_details.session ");
		//query.append("WHERE sessions.UserId = ?");
		
		countQuery.append("SELECT session AS id, count(*) as ActualPoints FROM sessions_traces GROUP BY session");
		
		completeSessionsQuery.append("SELECT A.* ");
		completeSessionsQuery.append("FROM sessions_details AS A left join ("+countQuery+") AS B ");
		completeSessionsQuery.append("ON A.session = B.id ");
		completeSessionsQuery.append("WHERE A.points = B.ActualPoints");
		
		query.append("select C.* from ("+completeSessionsQuery+") AS C JOIN sessions AS S ON C.session = S.Session WHERE S.UserId=?");
		
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			stmt.setInt(1, userId);
			
			ResultSet set = stmt.executeQuery();
			
			TrackSummary aux;
			while(set.next()){
				aux = new TrackSummary();
				
				aux.setSession(set.getString(1));
				aux.setStartedAt(set.getTimestamp(2).getTime());
				aux.setEndedAt(set.getTimestamp(3).getTime());
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
	public void addTrackTraceBatch(String session, Location[] trace) throws UnableToPerformOperation {
		
		UnableToPerformOperation exception = null;
		
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO sessions_traces (session, timestamp, latitude, longitude, attributes) ");
		query.append("VALUES (?,?,?,?,?)");
		
		try{
			conn.setAutoCommit(false);
			
			for(Location location : trace){
				PreparedStatement stmt = conn.prepareStatement(query.toString());
				stmt.setString(1, session);
				stmt.setTimestamp(2, new Timestamp(location.getTimestamp()));
				stmt.setDouble(3, location.getLatitude());
				stmt.setDouble(4, location.getLongitude());
				stmt.setString(5, location.getAttributes());
				stmt.executeUpdate();
				
				stmt.close();
			}
			
			conn.commit();
			
		}catch(SQLException e){
			
			try{
				if(conn != null){
					LOG.warn("Transaction is being rollbacked : registerTrackSummary@SessionDriver");
					conn.rollback();
				}
				
				exception = new UnableToPerformOperation(e.getMessage());
				
			}catch(SQLException e1){
				LOG.error(e1.getMessage());
			}
			
			
		} finally {
			
			try{
				conn.setAutoCommit(true);
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
		if(exception!=null) throw exception;
	}

	@Override
	public List<Location> getTrackTrace(String session) throws UnableToPerformOperation {
		
		List<Location> trace = new ArrayList<>();
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT timestamp, latitude, longitude, attributes FROM sessions_traces WHERE session = ?");
		
		try {
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			stmt.setString(1, session);
			
			ResultSet results = stmt.executeQuery();
			
			while(results.next()){
				Location location = new Location();
				location.setTimestamp(results.getTimestamp(1).getTime());
				location.setLatitude(results.getDouble(2));
				location.setLongitude(results.getDouble(3));
				location.setAttributes(results.getString(4));
				trace.add(location);
			}
			
			return trace;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	@Override
	public boolean isCompleteRoute(String session) throws UnableToPerformOperation {

		
		StringBuilder query = new StringBuilder();
		StringBuilder countQuery = new StringBuilder();
		StringBuilder secondOrderQuery = new StringBuilder();
		
		countQuery.append("select session, count(*) AS rPoints from sessions_traces group by session");
		secondOrderQuery.append("select A.session, A.points, B.rPoints from sessions_details AS A join ("+countQuery+") AS B on A.session = B.session");
		query.append("select session from ("+secondOrderQuery+") AS N WHERE session = ?");
		
		try{
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			stmt.setString(1, session);
			
			ResultSet results = stmt.executeQuery();
			
			return results.next();
			
		}catch(SQLException e){
			throw new UnableToPerformOperation(e.getMessage());
		}
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
