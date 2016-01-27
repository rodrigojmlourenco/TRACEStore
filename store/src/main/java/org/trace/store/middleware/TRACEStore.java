package org.trace.store.middleware;

import java.util.Date;

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

public class TRACEStore implements TRACETrackingDriver, TRACERewardDriver, TRACEPlannerDriver{

	private static TRACEStore MANAGER = new TRACEStore();
	
	private final GraphDB graph = GraphDB.getConnection();
	
	private TRACEStore(){
		//TODO: check if the graph is empty, and if so, populate the graph
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
		// TODO Auto-generated method stub
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
}
