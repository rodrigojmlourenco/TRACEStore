package org.trace.store.middleware.drivers;

import java.util.Date;

import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TRACEResultSet;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.data.Attributes;
import org.trace.store.services.api.data.Beacon;
import org.trace.store.services.api.data.Session;

/**
 * In order for higher-level information to be acquired, the data acquired by
 * the tracking applications must  be  aggregated  and interpreted  in  a  
 * centralized  component  –  the TRACEPlannerDriver. This  API specifies the
 * set of operations supported by TRACEPlannerDriver for the uploading and
 * querying of information.
 */
public interface TRACETrackingDriver {

	/**
	 * Enables a tracking application to report its geographical location,
	 * at a specific moment in time.
	 *  
	 * @param session Session to identify the session currently active on the tracking application.
	 * @param timestamp Identifies the specific moment in time that tracking data was registered.
	 * @param latitude The user's latitude
	 * @param longitude The user's longitude
	 * 
	 * @return True if the user's location was successfully added, false otherwise.
	 */
	public boolean put(Session session, Date timestamp, float latitude, float longitude);
	
	/**
	 * Enables a tracking application to report its location, at a specific
	 * moment in time. The location is also associated with a set of fields.
	 * These fields may be used to convey the transportation means, road 
	 * condition, and other such information.
	 *  
	 * @param session Session to identify the session currently active on the tracking application.
	 * @param timestamp Identifies the specific moment in time that tracking data was registered.
	 * @param latitude The user's latitude
	 * @param longitude The user's longitude
	 * @param attributes Associate semantic data to the registered location.
	 * 
	 * @return True if the user's location was successfully added, false otherwise.
	 */
	public boolean put(Session session, Date timestamp, float latitude, float longitude, Attributes attributes);
	
	/**
	 * Enables a participating tracking application to report its location with relation to a 
	 * beaconID, at a specific moment in time.
	 *  
	 * @param session Session to identify the session currently active on the tracking application.
	 * @param timestamp Identifies the specific moment in time that tracking data was registered.
	 * @param beacon The beacon to which the user was next to.
	 * 
	 * @return True if the user's location was successfully added, false otherwise.
	 */
	public boolean put(Session session, Date timestamp, Beacon beacon);
	
	/**
	 * Enables a tracking application to report its location, at a specific
	 * moment in time. The location is also associated with a set of fields.
	 * These fields may be used to convey the transportation means, road 
	 * condition, and other such information.
	 *  
	 * @param session Session to identify the session currently active on the tracking application.
	 * @param timestamp Identifies the specific moment in time that tracking data was registered.
	 * @param beacon The beacon to which the user was next to.
	 * @param attributes Associate semantic data to the registered location.
	 * 
	 * @return True if the user's location was successfully added, false otherwise.
	 */
	public boolean put(Session session, Date timestamp, Beacon beacon, Attributes attributes);
	
	/**
	 *  Enables a tracking application to report a traced tracked, as a whole.
	 *  A track can be perceived as an ordered sequence of locations that are
	 *  associated with a timestamp, and that may contain other semantic information.
	 *  This additional semantic information may contain, for instance, means of 
	 *  transportation, velocity, among others.
	 * 
	 * @param session Session to identify the session currently active on the tracking application.
	 * @param track Track traced by the user.
	 * 
	 * @return True if the user's location was successfully added, false otherwise.
	 */
	public boolean put(Session session, TraceTrack track);
	
	/**
	 * Enables users to query aspects such as previously taken routes. 
	 * 
	 * @param query Specification of which information the user is requesting.
	 * 
	 * @return The requested information
	 * 
	 * @see TRACEQuery
	 * @see TRACEResultSet
	 */
	public TRACEResultSet query(TRACEQuery query);
}
