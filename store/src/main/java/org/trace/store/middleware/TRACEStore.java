package org.trace.store.middleware;

import java.io.File;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.TRACEPlannerDriver;
import org.trace.store.middleware.drivers.TRACERewardDriver;
import org.trace.store.middleware.drivers.TRACETrackingDriver;
import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.TRACEPlannerQuery;
import org.trace.store.services.api.TRACEPlannerResultSet;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TRACEResultSet;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.data.Attributes;
import org.trace.store.services.api.data.Beacon;
import org.trace.store.services.api.data.Session;

import com.google.gson.JsonArray;


public class TRACEStore implements TRACETrackingDriver, TRACERewardDriver, TRACEPlannerDriver{

	private static final Logger LOG = LoggerFactory.getLogger(TRACEStore.class);
	
	private static TRACEStore MANAGER = new TRACEStore();
	
	private final GraphDB graph = GraphDB.getConnection();
	
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
	public boolean put(Session session, Date timestamp, float latitude, float longitude) {
		//TODO:
		
		return false;
	}


	@Override
	public boolean put(Session session, Date timestamp, float latitude, float longitude, Attributes attributes) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(Session session, Date timestamp, Beacon beacon) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(Session session, Date timestamp, Beacon beacon, Attributes attributes) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean put(Session session, TraceTrack track) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public TRACEResultSet query(TRACEQuery query) {
		// TODO Auto-generated method stub
		return null;
	}
	
		
	@Override
	public boolean registerUser(String username, String password, String name, String address){
		return graph.getTrackingAPI().register(username, password, name, address);
	}


	@Override
	public JsonArray getRouteBySession(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JsonArray getUserSessions(String username) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JsonArray getAllSessions() {
		// TODO Auto-generated method stub
		return null;
	}
}
