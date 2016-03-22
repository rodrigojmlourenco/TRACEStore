package org.trace.store.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.trace.store.filters.Role;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.TRACEStore;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.TRACETrackingDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UserRegistryException;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.services.api.BeaconLocation;
import org.trace.store.services.api.GeoLocation;
import org.trace.store.services.api.Location;
import org.trace.store.services.api.PrivacyPolicies;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.UserRegistryRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * In order for higher-level information to be acquired, the data acquired by 
 * the tracking applications must  be  aggregated  and  interpreted  in  a 
 * centralized  component  –  the  TRACEstore.  This  API specifies  the  set
 * of  operations  supported  by  TRACEstore  for  the  uploading  and  querying
 * of information. 
 */
@Path("/tracker")
public class TRACEStoreService {

	private final String LOG_TAG = "TRACEStoreService"; 

	private final Logger LOG = Logger.getLogger(TRACEStoreService.class); 

	private UserDriver uDriver = UserDriverImpl.getDriver();
	private SessionDriver sDriver = SessionDriverImpl.getDriver();
	private TRACETrackingDriver mDriver = TRACEStore.getTRACEStore();


	@Path("/test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test(){

		LOG.info("Welcome to the "+LOG_TAG);

		return "Welcome to the "+LOG_TAG;
	}


	@Path("/sample")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TraceTrack samoleTrack(){


		Location l1 = new Location(1, 1, 1);
		Location l2 = new Location(2, 2, 2);
		Location l3 = new Location(3, 3, 3);
		Location[] locations =  {l1, l2, l3};
		TraceTrack t = new TraceTrack(locations);

		return t;

	}


	/*
	 ************************************************************************
	 ************************************************************************
	 * User-based Requests													*
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Allows TRACE users to register themselves into TRACE’s system. 
	 *  
	 * @param request This request contains all the fields necessary to register a new user.
	 * @return 
	 * 
	 * @see UserRegistryRequest
	 */
	@POST
	@Path("/register")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response registerUser(UserRegistryRequest request){



		String activationToken;

		try {

			activationToken = 
					uDriver.registerUser(
							request.getUsername(),
							request.getEmail(),
							request.getPassword(),
							request.getConfirm(),
							request.getName(), 
							request.getAddress(),
							request.getPhone(),
							Role.user);

			LOG.info("User '"+request.getUsername()+"' successfully registered.");

			return Response.ok(activationToken).build();

		} catch (UserRegistryException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return Response.ok(e.getMessage()).build();
		} catch (NonMatchingPasswordsException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return Response.ok(e.getMessage()).build();
		} catch (UnableToRegisterUserException e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return Response.ok(e.getMessage()).build();
		} catch (UnableToPerformOperation e) {
			LOG.error("User '"+request.getUsername()+"' not registered, because "+e.getMessage());
			return Response.ok(e.getMessage()).build();
		}
	}

	/**
	 * Allows users to set security and privacy policies about their data. 
	 *   
	 * @param policies The privacy policies.
	 * @return Response notifying if the policies submission was successful or not.
	 */
	@POST
	@Secured(Role.user)
	@Path("/privacy")
	public Response setPrivacyPolicies(PrivacyPolicies policies){
		throw new UnsupportedOperationException();
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 * Insert-based Requests												*
	 ************************************************************************
	 ************************************************************************
	 */

	private Gson gson = new Gson();
	private String generateSuccessResponse(){
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		return gson.toJson(response);
	}

	private String generateFailedResponse(String msg){
		JsonObject response = new JsonObject();
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}

	/**
	 * Enables a tracking application to report its location, at a specific moment in time.
	 * @param sessionId The user's session identifier, which operates as a pseudonym.
	 * @param location The user's location
	 * 
	 * @return
	 * 
	 * @see GeoLocation
	 */
	@POST
	@Secured
	@Path("/put/geo/{session}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String put(@PathParam("session") String session, GeoLocation location, @Context SecurityContext context){

		boolean success;
		GraphDB conn = GraphDB.getConnection();
		success = conn.getTrackingAPI().put(
				session,
				new Date(location.getTimestamp()),
				location.getLatitude(),
				location.getLongitude());

		if(success)
			return generateSuccessResponse();
		else{
			LOG.error("Provided location was not accepted. TODO: provide verbose error");
			return generateFailedResponse("Location insertion failed.");
		}

	}


	private Map<String, Object> extractLocationAttributes(Location location){
		
		HashMap<String, Object> map = new HashMap<>();
		JsonObject attributes = (JsonObject) location.getLocationAsJsonObject().get("attributes");
		
		for(Entry<String, JsonElement> attribute : attributes.entrySet())
			map.put(attribute.getKey(), attribute.getValue());
		
		return map;
	}
	
	/**
	 * Enables a tracking application to report a traced tracked, as a whole.
	 *  
	 * @param sessionId The user's session identifier, which operates as a pseudonym.
	 * @param location The user's location
	 * 
	 * @return
	 * 
	 * @see TraceTrack
	 */
	@POST
	@Secured
	@Path("/put/track/{session}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String put(@PathParam("session") String session, TraceTrack track, @Context SecurityContext context){

		try {
			if(!sDriver.trackingSessionExists(session))
				return generateFailedResponse("Unknown session '"+session+"'.");
		} catch (UnableToPerformOperation e) {
			LOG.error("Failed to upload track with session '"+session+"' because : "+e.getMessage());
			return generateFailedResponse("Failed to upload track because :"+e.getMessage());
		}

		try {
			if(sDriver.isTrackingSessionClosed(session))
				return generateFailedResponse("Session '"+session+"' is already closed.");
		} catch (UnableToPerformOperation | SessionNotFoundException e) {
			LOG.error("Failed to upload track with session '"+session+"' because : "+e.getMessage());
			return generateFailedResponse("Failed to upload track because :"+e.getMessage());
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				boolean success;
				Location location;

				for(int i = 0; i < track.getTrackSize(); i++){
					GraphDB conn = GraphDB.getConnection();
					location = track.getLocation(i);

					if(location != null){
						success = conn.getTrackingAPI().put(
								session,
								new Date(location.getTimestamp()),
								location.getLatitude(),
								location.getLongitude(),
								extractLocationAttributes(location));
						
						if(!success)
							LOG.error("Failed to inser location "+location);

					}
				}
			}
		});

		thread.start();

		//TODO: Correct this so that we can get a better response, i.e., know if the insertion really got done.
		//Right now it always says it has been done correctly.
		//		if(failedInserts == 0)
		return generateSuccessResponse();
		//		else
		//			return generateFailedResponse("Failed to insert "+failedInserts+" out of "+track.getTrackSize()+" locations");
	}

	/**
	 * Enables a tracking application to report its location, at a specific moment in time.
	 * 
	 * @param sessionId The user's session identifier, which operates as a pseudonym.
	 * @param location The user's location
	 * 
	 * @return
	 * 
	 * @see BeaconLocation
	 */
	@POST
	@Secured
	@Path("/put/beacon")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response put(BeaconLocation location, @Context SecurityContext context){
		throw new UnsupportedOperationException();
	}



	/*
	 ************************************************************************
	 ************************************************************************
	 *** Get-based Requests												  ***
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Enables users to query aspects such as previously taken routes 
	 * 
	 * @param query A generic query that will be dully parsed and processed.
	 * 
	 * @return
	 */
	@POST
	@Secured(Role.user)
	@Path("/query")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response query(TRACEQuery query){
		throw new UnsupportedOperationException();
	}	


	/*
	 ************************************************************************
	 ************************************************************************
	 *** REUNIÃO 23/02/2016												  ***
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Fetches the coordinates sequence that makes up the route associated
	 * with the provided session identifyer
	 */
	@GET
	@Path("/route/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRouteBySession(@PathParam("sessionId") String sessionId){
		Gson gson = new Gson();
		return gson.toJson(mDriver.getRouteBySession(sessionId));
	}

	/**
	 * Fetches the list of tracking sessions that are associated with the
	 * specified user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Path("/sessions/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserSessions(@PathParam("username") String username){
		Gson gson = new Gson();
		return gson.toJson(mDriver.getUserSessions(username));

	}

	/**
	 * Fetches the list of tracking sessions and corresponding dates that are associated with the
	 * specified user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return List of sessions and dates as a Json array.
	 */
	@GET
	@Path("/sessionsAndDates/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserSessionsAndDates(@PathParam("username") String username){
		Gson gson = new Gson();
		return gson.toJson(mDriver.getUserSessionsAndDates(username));
	}

	/**
	 * Fetches the list of all tracking sessions.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Path("/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllSessions(){
		Gson gson = new Gson();
		return gson.toJson(mDriver.getAllSessions());
	}
}
