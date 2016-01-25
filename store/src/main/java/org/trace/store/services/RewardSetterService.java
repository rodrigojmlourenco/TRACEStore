package org.trace.store.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.UserRegistryRequest;

/**
 * Local  businesses,  for  instance  shop  owners,  may  leverage  TRACE
 * to  promote  themselves  by providing  rewards  to  users  that  come  to
 * their  businesses.  For  this  purpose,  TRACEstore contemplates a DBReward
 * API that enables interested business owners to register themselves in TRACE.
 * Once  they  have  been  registered,  they  may  then  specify  the  rewards
 * and  corresponding conditions through the this API. 
 *
 */
@Path("/reward")
public class RewardSetterService {

	
	/*
	 ************************************************************************
	 ************************************************************************
	 * User-based Requests													*
	 ************************************************************************
	 ************************************************************************
	 */
	
	/**
	 * Allows interested third parties to register themselves into TRACE’s system.  
	 *  
	 * @param request This request contains all the fields necessary to register a new third-party.
	 * @return 
	 * 
	 * @see UserRegistryRequest
	 */
	@POST
	@Path("/register")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response registerRewarder(UserRegistryRequest request){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Enables an interested third party application to authenticate themselves.  
	 *   
	 * @param username The user's unique username.
	 * @param password The user's corresponding password.
	 * 
	 * @return
	 */
	@POST
	@Path("/login")
	public Response login(@QueryParam("username") String username, @QueryParam("password") String password){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Terminates a rewarders' session.
	 * 
	 * @return
	 */
	@POST
	@Path("/logout")
	public Response logout(){
		throw new UnsupportedOperationException();
	}
	
	
	/*
	 ************************************************************************
	 ************************************************************************
	 * Insert-based Requests												*
	 ************************************************************************
	 ************************************************************************
	 */
	/**
	 * Enables third parties to associate themselves with a geographical location. 
	 * @param latitude
	 * @param longitude
	 * 
	 * @return
	 */
	@POST
	@Path("/set/location")
	public Response setBaseLocation(
			@QueryParam("lat")double latitude, @QueryParam("lon")double longitude){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Enables third parties to associate themselves with a beacon.
	 *  
	 * @param beaconId BeaconId that will be associated with the third party. 
	 * 
	 * @return
	 */
	@POST
	@Path("/set/location")
	public Response setBaseLocation(@QueryParam("beaconId") long beaconId){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Enables third parties to set a reward based on a set of policies.
	 * 
	 * @param policy Contains a description and a set of policies, which specify when a certain user is eligible to win a reward.
	 * 
	 * @return
	 * 
	 * @see RewardingPolicy
	 */
	@POST
	@Path("/set/reward")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response setBaseLocation(RewardingPolicy policy){
		throw new UnsupportedOperationException();
	}
}
