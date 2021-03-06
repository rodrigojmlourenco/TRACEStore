package org.trace.inesc.services;

import java.sql.SQLException;
import java.util.List;

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
import org.trace.inesc.filters.Role;
import org.trace.inesc.filters.Secured;
import org.trace.inesc.store.middleware.backend.GraphDB;
import org.trace.inesc.store.middleware.drivers.RewarderDriver;
import org.trace.inesc.store.middleware.drivers.UserDriver;
import org.trace.inesc.store.middleware.drivers.impl.RewarderDriverImpl;
import org.trace.inesc.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.inesc.store.middleware.drivers.utils.FormFieldValidator;
import org.trace.inesc.store.middleware.exceptions.EmailAlreadyRegisteredException;
import org.trace.inesc.store.middleware.exceptions.InvalidEmailException;
import org.trace.inesc.store.middleware.exceptions.InvalidPasswordException;
import org.trace.inesc.store.middleware.exceptions.InvalidUsernameException;
import org.trace.inesc.store.middleware.exceptions.NonMatchingPasswordsException;
import org.trace.inesc.store.middleware.exceptions.UnableToPerformOperation;
import org.trace.inesc.store.middleware.exceptions.UnableToRegisterUserException;
import org.trace.inesc.store.middleware.exceptions.UnknownUserException;
import org.trace.inesc.store.middleware.exceptions.UsernameAlreadyRegisteredException;
import org.trace.inesc.store.services.data.DistanceBasedRewardRequest;
import org.trace.inesc.store.services.data.RegisterShopRequest;
import org.trace.inesc.store.services.data.RewardingPolicy;
import org.trace.inesc.store.services.data.Shop;
import org.trace.inesc.store.services.data.ShopDetailed;
import org.trace.inesc.store.services.data.TraceReward;
import org.trace.inesc.store.services.data.UserRegistryRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Local businesses, for instance shop owners, may leverage TRACE to promote
 * themselves by providing rewards to users that come to their businesses. For
 * this purpose, TRACEstore contemplates a DBReward API that enables interested
 * business owners to register themselves in TRACE. Once they have been
 * registered, they may then specify the rewards and corresponding conditions
 * through the this API.
 *
 */
@Path("/reward")
public class RewardSetterService {

	private RewarderDriver rDriver = RewarderDriverImpl.getDriver();
	private UserDriver uDriver = UserDriverImpl.getDriver();

	private final String LOG_TAG = "TRACEStoreService";
	private final Logger LOG = Logger.getLogger(TRACEStoreService.class);

	private Gson gson = new Gson();

	private String generateSuccessResponse(String payload) {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		response.addProperty("payload", payload); // TODO: isto deveria ser
													// enviado por email.
		return gson.toJson(response);
	}

	private String generateFailedResponse(String msg) {
		JsonObject response = new JsonObject();
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}

	private String generateFailedResponse(int code, String msg) {
		JsonObject response = new JsonObject();
		response.addProperty("code", code);
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 * User-based Requests *
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Allows interested third parties to register themselves into TRACE’s
	 * system.
	 * 
	 * @param request
	 *            This request contains all the fields necessary to register a
	 *            new third-party.
	 * @return
	 * 
	 * @see UserRegistryRequest
	 */
	@POST
	@Path("/register")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String registerRewarder(UserRegistryRequest request) {
		String activationToken;
		String name, phone, address;

		try {

			((UserDriverImpl) uDriver).validateFields(request.getUsername(), request.getEmail(), request.getPassword(),
					request.getConfirm());

			name = request.getName();
			phone = request.getPhone();
			address = request.getAddress();

			name = name == null ? "" : name;
			phone = phone == null ? "" : phone;
			address = address == null ? "" : address;

			if (!name.isEmpty() && !FormFieldValidator.isValidName(name))
				return generateFailedResponse(6, "Invalid name");

			if (!phone.isEmpty() && !FormFieldValidator.isValidPhoneNumber(phone))
				return generateFailedResponse(7, "Invalid phone number");

			if (!address.isEmpty() && !FormFieldValidator.isValidAddress(address))
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
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(5, "Non matching passwords.");
		} catch (SQLException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(9, e.getMessage());
		}

		try {
			activationToken = uDriver.registerUser(request.getUsername(), request.getEmail(), request.getPassword(),
					request.getConfirm(), request.getName(), request.getAddress(), request.getPhone(), Role.rewarder);

			return generateSuccessResponse(activationToken);
		} catch (UnableToRegisterUserException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(10, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(11, e.getMessage());
		}
	}

	private boolean hasRewarderRole(String identifier) {
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
	public String registerReward(DistanceBasedRewardRequest request, @Context SecurityContext context) {

		String user = context.getUserPrincipal().getName();

		try {
			int userId = uDriver.getUserID(user);
			int shopId = rDriver.getShopId(userId);

			if (!hasRewarderRole(user)) {
				LOG.error(user + " is not a rewarder");
				return generateFailedResponse(1, "The user is not a rewarder");
			}

			if(request.getTravelledDistance() == 0){
				rDriver.registerCycleToShopReward(shopId, request.getReward());
			}else{
				rDriver.registerDistanceBasedReward(shopId, request.getTravelledDistance(), request.getReward());
			}

			return generateSuccessResponse("");

		} catch (UnknownUserException e) {
			return generateFailedResponse(2, e.getMessage());
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(3, e.getMessage());
		}
	}

	@GET
	@Secured
	@Path("/get/rewards")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRegisteredRewards(@Context SecurityContext context) {

		int userId, shopId;
		String user = context.getUserPrincipal().getName();

		LOG.info("@getRegisteredRewards: Rewarder<" + user + ">?");

		try {
			userId = uDriver.getUserID(user);
			shopId = rDriver.getShopId(userId);

			if (!hasRewarderRole(user)) {
				LOG.error(user + " is not a rewarder");
				return generateFailedResponse(1, "The user is not a rewarder");
			}

			List<TraceReward> rewards = rDriver.getAllShopRewards(shopId);

			Gson gson = new Gson();
			JsonArray payload = new JsonArray();
			for (TraceReward r : rewards) {
				JsonObject aux = r.toJson();

				if(r.getType().equals("Cycle To Shop")){
					aux.addProperty("winners", "-");
				}else{
					// TODO: mais tarde fazer isto de forma inteligente
					int userCount = rDriver.getUsersWithDistance(r.getMinimumDistance()).size();
					aux.addProperty("winners", userCount);
				}
				payload.add(aux);
			}

			LOG.info("Fetched " + rewards.size() + " associated with " + user);
			return generateSuccessResponse(gson.toJson(payload));

		} catch (UnknownUserException e) {
			LOG.error(e.getMessage());
			return generateFailedResponse(2, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error(e.getMessage());
			return generateFailedResponse(3, e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			return generateFailedResponse(3, e.getMessage());
		}
	}
	
	@GET
	@Path("/rewards")
	@Produces(MediaType.APPLICATION_JSON)
	public String getShopRewards(@QueryParam("lat") double latitude, @QueryParam("lon") double longitude, @QueryParam("radius") double radius){
		
//		System.out.println("+++++++++++++++++LATITUDE:" + latitude);
		
		GraphDB conn = GraphDB.getConnection();
		List<String> shopIds = conn.getRewardAPI().getShopsIds(latitude, longitude, radius);
		
//		System.out.println("++++++++++++++++SHOPSIDS+++++++++++");
//		for(String shopId : shopIds){
//			System.out.println("shopId: " + shopId);
//		}
		
		JsonArray result = new JsonArray();
		
		try {
			List<ShopDetailed> shopsWithRewards = rDriver.getDetailedShops(shopIds);
			
			for (ShopDetailed shop : shopsWithRewards) {
				result.add(shop.toJson());
			}
			
			return generateSuccessResponse(result.toString());
			
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return generateFailedResponse(1, e.getMessage());
		}catch (Exception e){
			e.printStackTrace();
			return generateFailedResponse(1, e.getMessage());
		}
	}
	
	/*
	 ************************************************************************
	 ************************************************************************
	 * Insert-based Requests *
	 ************************************************************************
	 ************************************************************************
	 */
	/**
	 * Enables third parties to associate themselves with a geographical
	 * location.
	 * 
	 * @param latitude
	 * @param longitude
	 * 
	 * @return
	 */
	@POST
	@Secured(Role.rewarder)
	@Path("/set/location")
	public Response setBaseLocation(@QueryParam("lat") double latitude, @QueryParam("lon") double longitude) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Enables third parties to associate themselves with a beacon.
	 * 
	 * @param beaconId
	 *            BeaconId that will be associated with the third party.
	 * 
	 * @return
	 */
	@POST
	@Path("/set/beacon")
	@Secured(Role.rewarder)
	public Response setBaseLocation(@QueryParam("beaconId") long beaconId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Enables third parties to set a reward based on a set of policies.
	 * 
	 * @param policy
	 *            Contains a description and a set of policies, which specify
	 *            when a certain user is eligible to win a reward.
	 * 
	 * @return
	 * 
	 * @see RewardingPolicy
	 */
	@POST
	@Path("/set/beacon")
	@Secured(Role.rewarder)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response setBaseLocation(RewardingPolicy policy) {
		throw new UnsupportedOperationException();
	}

	@POST
	@Path("/del/reward")
	@Secured(Role.rewarder)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String unregisterReward(@FormParam("reward") int rewardId, @Context SecurityContext context) {

		int ownerId;
		String user = context.getUserPrincipal().getName();

		if (!hasRewarderRole(user)) {
			LOG.error("Unauthorized access, no 'reward' capabilities");
			return generateFailedResponse(1, "Unauthorized access, no 'reward' capabilities");
		}

		try {
			ownerId = uDriver.getUserID(user);

			if (!rDriver.ownsReward(ownerId, rewardId)) {
				LOG.error("This rewards does not belong to the user");
				return generateFailedResponse(2, "This rewards does not belong to the user");
			}

			try {
				rDriver.unregisterReward(rewardId);

			} catch (Exception e) {
				LOG.error(e.getMessage());
				LOG.error(e.getClass().getSimpleName());
				LOG.error(e);
				return generateFailedResponse(5, e.getMessage());
			}

			return generateSuccessResponse("success");

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
	public String usersWithDistance(@PathParam("distance") double distance) {
		Gson gson = new Gson();
		JsonArray jArray = new JsonArray();
		try {
			List<String> users = rDriver.getUsersWithDistance(distance);

			for (String s : users) {
				jArray.add(s);
			}

			return gson.toJson(jArray);
		} catch (UnableToPerformOperation e) {
			return e.getMessage();
		}
	}

	/**
	 * Fetches the Km of the specified user
	 */
	@GET
	@Path("/userDistance/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String userDistance(@PathParam("userId") int userId) {
		Gson gson = new Gson();
		JsonArray jArray = new JsonArray();
		try {
			double distance = rDriver.getUserDistance(userId);

			return gson.toJson(distance);
		} catch (UnableToPerformOperation e) {
			return e.getMessage();
		}
	}

	// @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON})
	/**
	 * Fetches the Km of the specified user
	 */
	@POST
	@Path("/userDistanceByDate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String userDistanceByDate(String userId) {
		Gson gson = new Gson();
		JsonArray jArray = new JsonArray();
		// try {
		// double distance = rDriver.getUserDistance(userId);
		//
		// return gson.toJson(distance);
		String toReturn = "userId: " + userId;

		return gson.toJson(toReturn);
		// } catch (UnableToPerformOperation e) {
		// return e.getMessage();
		// }
	}

	@POST
	@Path("/set/shop")
	@Secured(Role.rewarder)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String registerShop(RegisterShopRequest request, @Context SecurityContext context) {

		// TODO: add if user doesn't have a shop, but update in case he already
		// has one.

		String user = context.getUserPrincipal().getName();

		try {
			int ownerId = uDriver.getUserID(user);
			if (!hasRewarderRole(user)) {
				LOG.error(user + " is not a rewarder");
				return generateFailedResponse(1, "The user is not a rewarder");
			}

			GraphDB conn = GraphDB.getConnection();

			Shop shop = rDriver.getShop(ownerId);

			// register
			if (shop == null) {
				int shopId = rDriver.registerShop(ownerId, request.getName(), request.getBranding(),
						request.getLatitude(), request.getLongitude());
				conn.getRewardAPI().setShop(shopId, ownerId, request.getLatitude(), request.getLongitude());

			} else { // update
				rDriver.updateShop(ownerId, request.getName(), request.getBranding(), request.getLatitude(),
						request.getLongitude());
				conn.getRewardAPI().updateShop(shop.getId(), request.getLatitude(), request.getLongitude());
			}

			return generateSuccessResponse("" + shop.getId());

		} catch (UnknownUserException e) {
			return generateFailedResponse(2, e.getMessage());
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(3, e.getMessage());
		}
	}

	@GET
	@Path("/get/shop")
	@Secured(Role.rewarder)
	@Produces(MediaType.APPLICATION_JSON)
	public String getShop(@Context SecurityContext context) {

		String user = context.getUserPrincipal().getName();

		Shop shop = null;

		Gson gson = new Gson();
		try {
			int ownerId = uDriver.getUserID(user);

			shop = rDriver.getShop(ownerId);

			if (shop != null) {
				return generateSuccessResponse(gson.toJson(shop));
			} else {
				return generateFailedResponse("This user has no shops registered.");
			}
		} catch (UnknownUserException e) {
			return generateFailedResponse(2, e.getMessage());
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(3, e.getMessage());
		}
	}
}
