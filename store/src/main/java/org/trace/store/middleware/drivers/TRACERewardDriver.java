package org.trace.store.middleware.drivers;

import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.data.Beacon;

/**
 * Local  businesses,  for  instance  shop  owners,  may  leverage  TRACE  to
 * promote  themselves  by providing  rewards  to  users  that come to their
 * businesses.  For  this  purpose,  TRACEstore contemplates a TRACERewardDriver
 * that enables interested business owners to register themselves in TRACE. Once  
 * they  have  been  registered,  they  may  then  specify the rewards and 
 * corresponding conditions through the TRACERewardDriver. 
 */
public interface TRACERewardDriver {

	/**
	 * Enables third parties to associate themselves with a geographical location. 
	 * 
	 * @param identifier The 3rd party's identifier
	 * @param latitude The 3rd party's location's latitude
	 * @param longitude The 3rd party's location's longitude
	 * 
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean setLocation(String identifier, float latitude, float longitude);
	
	/**
	 * Enables third parties to associate themselves with a beacon. 
	 * 
	 * @param identifier The 3rd party's identifier
	 * @param beacon The 3rd party's beacon.
	 * 
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean setLocation(String identifier, Beacon beacon);
	
	/**
	 * Enables third parties to set a reward based on a set of policies.
	 *   
	 * @param identifier The 3rd party's identifier.
	 * @param description A brief description of the reward
	 * @param policy The policy that specified the conditions that must be fulfield 
	 * 
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean setReward(String identifier, String description, RewardingPolicy policy);
}
