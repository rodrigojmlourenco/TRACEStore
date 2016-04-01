package org.trace.store.services;

import java.sql.SQLException;
import java.util.List;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.trace.store.filters.Role;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.drivers.RewarderDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.EmailAlreadyRegisteredException;
import org.trace.store.middleware.drivers.exceptions.InvalidEmailException;
import org.trace.store.middleware.drivers.exceptions.InvalidPasswordException;
import org.trace.store.middleware.drivers.exceptions.InvalidUsernameException;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.exceptions.UsernameAlreadyRegisteredException;
import org.trace.store.middleware.drivers.impl.RewarderDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.drivers.utils.FormFieldValidator;
import org.trace.store.services.api.RewardingPolicy;
import org.trace.store.services.api.UserRegistryRequest;
import org.trace.store.services.api.data.DistanceBasedRewardRequest;
import org.trace.store.services.api.data.TraceReward;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
	private UserDriver uDriver = UserDriverImpl.getDriver();

	private final String LOG_TAG = "TRACEStoreService"; 
	private final Logger LOG = Logger.getLogger(TRACEStoreService.class);
	
	private Gson gson = new Gson();
	
	private String generateSuccessResponse(String payload){
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		response.addProperty("payload", payload); //TODO: isto deveria ser enviado por email.
		return gson.toJson(response);
	}

	private String generateFailedResponse(String msg){
		JsonObject response = new JsonObject();
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}
	private String generateFailedResponse(int code, String msg){

		JsonObject response = new JsonObject();
		response.addProperty("code", code);
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}
	
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String registerRewarder(UserRegistryRequest request){
		String activationToken;
		String name, phone, address;

		try {

			((UserDriverImpl)uDriver).validateFields(request.getUsername(), request.getEmail(), request.getPassword(), request.getConfirm());

			name 	= request.getName();
			phone	= request.getPhone();
			address	= request.getAddress();

			name 	= name == null ? "" : name;
			phone 	= phone == null ? "" : phone;
			address = address == null ? "" :address;

			if(!name.isEmpty() && !FormFieldValidator.isValidName(name))
				return generateFailedResponse(6, "Invalid name");

			if(!phone.isEmpty() && !FormFieldValidator.isValidPhoneNumber(phone))
				return generateFailedResponse(7, "Invalid phone number");

			if(!address.isEmpty() && !FormFieldValidator.isValidAddress(address))
				return generateFailedResponse(8, "Invalid address");


		} catch (InvalidEmailException e) {
			return generateFailedResponse(2, "Invalid email address");
		} catch (InvalidUsernameException e) {
			return generateFailedResponse(1, "Invalid username");
		} catch (InvalidPasswordException e) {
			return generateFailedResponse(3, "Invalid password");
		} catch (UsernameAlreadyRegisteredException e) {
			return generateFailedResponse(4, "Username already registered");
		} catch (EmailAlreadyRegisteredException e) {
			return generateFailedResponse(5, "Email address already registered");
		} catch (NonMatchingPasswordsException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return generateFailedResponse(5, "Non matching passwords.");
		} catch (SQLException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return generateFailedResponse(9, e.getMessage());
		}

		try{
			activationToken =	 
					uDriver.registerUser(
							request.getUsername(),
							request.getEmail(),
							request.getPassword(),
							request.getConfirm(),
							request.getName(), 
							request.getAddress(),
							request.getPhone(),
							Role.rewarder);

			return generateSuccessResponse(activationToken);
		}catch (UnableToRegisterUserException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return generateFailedResponse(10, e.getMessage());
		}	 catch (UnableToPerformOperation e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return generateFailedResponse(11, e.getMessage());
		}
	}
	
	private boolean hasRewarderRole(String identifier){
		List<Role> roles;
		try {
			roles = uDriver.getUserRoles(identifier);
			return roles.contains(Role.rewarder);
		} catch (UnableToPerformOperation | UnknownUserException e) {
			return false;
		}
		
		
			
	}

	@POST
	@Path("/set/reward")
	@Secured(Role.rewarder)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String registerReward(DistanceBasedRewardRequest request, @Context SecurityContext context){
		
		String user = context.getUserPrincipal().getName();
		
		try {
			int ownerId = uDriver.getUserID(user);
			
			if(!hasRewarderRole(user)){
				LOG.error(user+" is not a rewarder");
				return generateFailedResponse(1, "The user is not a rewarder");
			}
			
			rDriver.registerDistanceBasedReward(ownerId, request.getTravelledDistance(), request.getReward());
			
			return generateSuccessResponse("");
			
		} catch (UnknownUserException e){
			return generateFailedResponse(2, e.getMessage());
		}catch (UnableToPerformOperation e) {
			return generateFailedResponse(3, e.getMessage());
		}
	}
	
	@GET
	@Secured
	@Path("/get/rewards")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRegisteredRewards(@Context SecurityContext context){
		
		
		
		int ownerId;
		String user = context.getUserPrincipal().getName();
		
		LOG.info("@getRegisteredRewards: Rewarder<"+user+">?");
		
		try {
			ownerId = uDriver.getUserID(user);
						
			if(!hasRewarderRole(user)){
				LOG.error(user+" is not a rewarder");
				return generateFailedResponse(1, "The user is not a rewarder");
			}
						
			List<TraceReward> rewards = rDriver.getAllOwnerRewards(ownerId);
			
			Gson gson = new Gson();
			JsonArray payload = new JsonArray();
			for(TraceReward r : rewards){
				
				//TODO: mais tarde fazer isto de forma inteligente
				int userCount = rDriver.getUsersWithDistance(r.getMinimumDistance()).size();
				JsonObject aux = r.toJson();
				aux.addProperty("winners", userCount);
				payload.add(aux);
			}

			LOG.info("Fetched "+rewards.size()+" associated with "+user);
			return generateSuccessResponse(gson.toJson(payload));
			
		} catch (UnknownUserException e){
			LOG.error(e.getMessage());
			return generateFailedResponse(2, e.getMessage());
		}catch( UnableToPerformOperation e) {
			LOG.error(e.getMessage());
			return generateFailedResponse(3, e.getMessage());
		}
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
	@Path("/set/beacon")
	@Secured(Role.rewarder)
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response setBaseLocation(RewardingPolicy policy){
		throw new UnsupportedOperationException();
	}
	
	
	@POST
	@Path("/del/reward")
	@Secured(Role.rewarder)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String unregisterReward(@FormParam("reward")int rewardId, @Context SecurityContext context){
		
		
		
		int ownerId;
		String user = context.getUserPrincipal().getName();
		
		LOG.info("@unregisterReward - "+user);
		
		if(!hasRewarderRole(user)){
			LOG.error("Unauthorized access, no 'reward' capabilities");
			return generateFailedResponse(1, "Unauthorized access, no 'reward' capabilities");
		}
			
		
		try {
			ownerId = uDriver.getUserID(user);
			
			if(!rDriver.ownsReward(ownerId, rewardId))
				return generateFailedResponse(2, "This rewards does not belong to the user");
			
			if(rDriver.unregisterReward(rewardId)){
				LOG.info("The user's <"+ownerId+"> reward <"+rewardId+"> was removed.");
				return generateSuccessResponse("");
			}else{
				LOG.error("The reward was not removed as it didn't exist anyhow.");
				return generateFailedResponse(3, "The reward was not removed as it didn't exist anyhow.");
			}
			
			
		} catch (UnknownUserException e) {
			LOG.error(e.getMessage());
			return generateFailedResponse(2, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error(e.getMessage());
			return generateFailedResponse(3, e.getMessage());
		}
		
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
