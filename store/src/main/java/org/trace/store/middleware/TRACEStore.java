package org.trace.store.middleware;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.trace.DBAPI.data.SimpleSession;
import org.trace.DBAPI.data.TraceVertex;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.TRACEPlannerDriver;
import org.trace.store.middleware.drivers.TRACERewardDriver;
import org.trace.store.middleware.drivers.TRACETrackingDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.TRACEPlannerQuery;
import org.trace.store.services.api.TRACEPlannerResultSet;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TRACEResultSet;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.data.Attributes;
import org.trace.store.services.api.data.Beacon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TRACEStore implements TRACETrackingDriver, TRACERewardDriver, TRACEPlannerDriver{

	private static final Logger LOG = Logger.getLogger(TRACEStore.class);
	
	private static TRACEStore MANAGER = new TRACEStore();
	
	private final GraphDB graph = GraphDB.getConnection();
	private final SessionDriver sessionDriver = SessionDriverImpl.getDriver();
	private final UserDriver userDriver = UserDriverImpl.getDriver();
	
	
	private TRACEStore(){
		if(graph.isEmptyGraph()){
			LOG.info("Graph database is empty, populating the map with OSM data before the server is initiated.");
			graph.populateFromOSM(new File("/var/otp"));
		}
	}
		
	
	public static TRACEStore getTRACEStore(){
		return MANAGER;
	}


	/*
	 *************************************************************************
	 *************************************************************************
	 ****** 					TRACE Urban Planning					******
	 *************************************************************************
	 *************************************************************************
	 */
	
	@Override
	public TRACEPlannerResultSet get(TRACEPlannerQuery query) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
	}


	/*
	 *************************************************************************
	 *************************************************************************
	 ****** 					TRACE 3rd Party Rewards					******
	 *************************************************************************
	 *************************************************************************
	 */
	@Override
	public boolean setLocation(String identifier, float latitude, float longitude) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setLocation(String identifier, Beacon beacon) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean setReward(String identifier, String description, RewardingPolicy policy) {
		// TODO Auto-generated method stub
		return false;
	}


	/*
	 *************************************************************************
	 *************************************************************************
	 ****** 					TRACE Tracking							******
	 *************************************************************************
	 *************************************************************************
	 */
	
	@Override
	public boolean put(String session, Date timestamp, float latitude, float longitude) {
//		return graph.getTrackingAPI().put(session, timestamp, latitude, longitude);		
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(String session, Date timestamp, float latitude, float longitude, Attributes attributes) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(String session, Date timestamp, Beacon beacon) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(String session, Date timestamp, Beacon beacon, Attributes attributes) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(String session, TraceTrack track) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public TRACEResultSet query(TRACEQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
	
		
	@Override
	public boolean registerUser(String username, String name, String address){
//		return graph.getTrackingAPI().register(username, name, address);
		// TODO Auto-generated method stub
		return false;
	}


	private JsonObject vertexAsJson(TraceVertex vertex){
		JsonObject result = new JsonObject();
		
		result.addProperty("name", vertex.getName());
		result.addProperty("type", vertex.getType());
		result.addProperty("latitude", vertex.getLatitude());
		result.addProperty("longitude", vertex.getLongitude());
		
		String beaconID = vertex.getBeaconID();
		if(beaconID != null)
			result.addProperty("beaconId", beaconID);
		
		
		return result;
	}
	
	@Override
	public JsonArray getRouteBySession(String sessionId) {

		JsonArray results = new JsonArray();
		
		List<TraceVertex> vertices = graph.getTrackingAPI().getRouteBySession(sessionId);
		
		for(TraceVertex vertex : vertices)
			results.add(vertexAsJson(vertex));
		
		return results;
	}


	@Override
	public JsonArray getUserSessions(String username) {
		
		JsonArray results = new JsonArray();
		
		//List<String> sessions = graph.getTrackingAPI().getUserSessions(username);
		int userId;
		try {
			userId = userDriver.getUserID(username);
			List<SimpleSession> sessions = sessionDriver.getAllUserTrackingSessions(userId);
			
			for(SimpleSession session : sessions)
				results.add(session.toString());
			
		} catch (UnknownUserException | UnableToPerformOperation e) {
			LOG.error(e);
		}
		
		return results;
	}
	
	@Override
	public JsonArray getUserSessionsAndDates(String username) {
		
		JsonArray results = new JsonArray();
		
		int userId;
		try {
			userId = userDriver.getUserID(username);
			List<SimpleSession> sessions = sessionDriver.getAllUserTrackingSessions(userId);
			
			for(SimpleSession session : sessions)
				results.add(session.toString());
			
		} catch (UnknownUserException | UnableToPerformOperation e) {
			LOG.error(e);
		}
		
		return results;
	}

	
	@Override
	public JsonArray getAllSessions() {
		JsonArray results = new JsonArray();
		
		//List<String> sessions = graph.getTrackingAPI().getAllSessions();
		List<SimpleSession> sessions;
		try {
			sessions = sessionDriver.getAllTrackingSessions();
			for(SimpleSession session : sessions)
				results.add(session.toString());
		} catch (UnableToPerformOperation e) {
			LOG.error(e);
		}
		
		
		
		return results;
	}
}
