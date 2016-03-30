package org.trace.store.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.trace.store.filters.Role;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.drivers.RewarderDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.impl.RewarderDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.UserRegistryRequest;

import com.google.api.client.googleapis.notifications.json.gson.GsonNotificationCallback;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

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
	
	private RewarderDriver rDriver = RewarderDriverImpl.getDriver();

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
	@Secured(Role.rewarder)
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
	@Path("/set/beacon")
	@Secured(Role.rewarder)
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
	@Secured(Role.rewarder)
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response setBaseLocation(RewardingPolicy policy){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Fetches all the users that have traveled X or more Kms
	 */
	@GET
	@Path("/usersWithDistance/{distance}")
	@Produces(MediaType.APPLICATION_JSON)
	public String usersWithDistance(@PathParam("distance") double distance){
		Gson gson = new Gson();
		JsonArray jArray = new JsonArray();
		try {
			List<String> users = rDriver.getUsersWithDistance(distance);
			
			for(String s : users){
				jArray.add(s);
			}
			
			return gson.toJson(jArray);
		} catch (UnableToPerformOperation e) {
			return e.getMessage();
		}
	}
}
