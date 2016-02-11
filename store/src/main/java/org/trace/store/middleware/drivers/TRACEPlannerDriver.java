package org.trace.store.middleware.drivers;

import org.trace.store.services.api.TRACEPlannerQuery;
import org.trace.store.services.api.TRACEPlannerResultSet;

/**
 * TRACE has the potential to acquire extensive and very rich information 
 * regarding the characteristics of a city’s transportation network, and its
 * citizen’s mobility patterns. This information is of great value for urban
 * planner entities, as it enables more responsive and improve urban planning
 * initiatives. For instance,  the  knowledge  of  which  streets  are  preferred
 * by  cyclists  can  be  used  to  guide  the construction of new bicycle paths.
 * For this purpose, TRACEstore also contemplates a TRACEPlannerDriver.
 * The latter was designed to allow urban planner entities to query for 
 * higher-level information, in a flexible manner.
 */
public interface TRACEPlannerDriver {
	
	/**
	 *  Enables urban planners to lookup statistical data based on the provided
	 *  planner queries.
	 *   
	 * @param query Planner queries which are to be answered by TRACEstore.
	 * @return
	 */
	public TRACEPlannerResultSet get(TRACEPlannerQuery query);

}
