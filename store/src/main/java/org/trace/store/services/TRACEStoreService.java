package org.trace.store.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
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
import org.trace.DBAPI.data.TraceVertex;
import org.trace.store.filters.Role;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.TRACEStore;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.TRACETrackingDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.EmailAlreadyRegisteredException;
import org.trace.store.middleware.drivers.exceptions.InvalidEmailException;
import org.trace.store.middleware.drivers.exceptions.InvalidPasswordException;
import org.trace.store.middleware.drivers.exceptions.InvalidUsernameException;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.exceptions.UsernameAlreadyRegisteredException;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.drivers.utils.FormFieldValidator;
import org.trace.store.middleware.drivers.utils.SecurityUtils;
import org.trace.store.services.api.BeaconLocation;
import org.trace.store.services.api.Location;
import org.trace.store.services.api.PrivacyPolicies;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TraceActivities;
import org.trace.store.services.api.TraceStates;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.UserRegistryRequest;
import org.trace.store.services.api.data.TrackSummary;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * In order for higher-level information to be acquired, the data acquired by
 * the tracking applications must be aggregated and interpreted in a centralized
 * component – the TRACEstore. This API specifies the set of operations
 * supported by TRACEstore for the uploading and querying of information.
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
	public String test() {

		LOG.info("Welcome to the " + LOG_TAG);

		return "Welcome to the " + LOG_TAG;
	}

	@Path("/sample")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TraceTrack samoleTrack() {

		Location l1 = new Location(1, 1, 1, "a");
		Location l2 = new Location(2, 2, 2, "b");
		Location l3 = new Location(3, 3, 3, "c");
		Location[] locations = { l1, l2, l3 };
		TraceTrack t = new TraceTrack(locations);

		return t;
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 * User-based Requests *
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Allows TRACE users to register themselves into TRACE’s system.
	 * 
	 * @param request
	 *            This request contains all the fields necessary to register a
	 *            new user.
	 * @return
	 * 
	 * @see UserRegistryRequest
	 */
	@POST
	@Path("/register")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces(MediaType.APPLICATION_JSON)
	public String registerUser(UserRegistryRequest request) {

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
				return generateFailedResponse(8, "Invalid name");

			if (!phone.isEmpty() && !FormFieldValidator.isValidPhoneNumber(phone))
				return generateFailedResponse(9, "Invalid phone number");

			if (!address.isEmpty() && !FormFieldValidator.isValidAddress(address))
				return generateFailedResponse(10, "Invalid address");

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
			return generateFailedResponse(6, "Non matching passwords.");
		} catch (SQLException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(7, e.getMessage());
		}

		try {
			activationToken = uDriver.registerUser(request.getUsername(), request.getEmail(), request.getPassword(),
					request.getConfirm(), request.getName(), request.getAddress(), request.getPhone(), Role.user);

			return generateSuccessResponse(activationToken);
		} catch (UnableToRegisterUserException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(7, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return generateFailedResponse(7, e.getMessage());
		}
	}

	/**
	 * Allows users to set security and privacy policies about their data.
	 * 
	 * @param policies
	 *            The privacy policies.
	 * @return Response notifying if the policies submission was successful or
	 *         not.
	 */
	@POST
	@Secured(Role.user)
	@Path("/privacy")
	public Response setPrivacyPolicies(PrivacyPolicies policies) {
		throw new UnsupportedOperationException();
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 * Insert-based Requests *
	 ************************************************************************
	 ************************************************************************
	 */

	private Gson gson = new Gson();

	private String generateSuccessResponse() {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		return gson.toJson(response);
	}

	private String generateSuccessResponse(String payload) {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		response.addProperty("token", payload); // TODO: isto deveria ser
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

	// /**
	// * Enables a tracking application to report its location, at a specific
	// moment in time.
	// * @param sessionId The user's session identifier, which operates as a
	// pseudonym.
	// * @param location The user's location
	// *
	// * @return
	// *
	// * @see GeoLocation
	// */
	// @POST
	// @Secured
	// @Path("/put/geo/{session}")
	// @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	// public String put(@PathParam("session") String session, GeoLocation
	// location, @Context SecurityContext context){
	//
	// boolean success;
	// GraphDB conn = GraphDB.getConnection();
	// success = conn.getTrackingAPI().put(
	// session,
	// new Date(location.getTimestamp()),
	// location.getLatitude(),
	// location.getLongitude());
	//
	// if(success)
	// return generateSuccessResponse();
	// else{
	// LOG.error("Provided location was not accepted. TODO: provide verbose
	// error");
	// return generateFailedResponse("Location insertion failed.");
	// }
	//
	// }

	private Map<String, Object> extractLocationAttributes(Location location) {

		HashMap<String, Object> map = new HashMap<>();
		try {

			JsonObject attributes = (JsonObject) location.getLocationAsJsonObject().get("attributes");

			for (Entry<String, JsonElement> attribute : attributes.entrySet())
				map.put(attribute.getKey(), attribute.getValue());
		} catch (ClassCastException e) {
			LOG.error("Unable to extract the attributes because, " + e.getMessage());
			return null;
		}

		return map;
	}

	/**
	 * Enables a tracking application to report a traced tracked, as a whole.
	 * 
	 * @param sessionId
	 *            The user's session identifier, which operates as a pseudonym.
	 * @param location
	 *            The user's location
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
	public String put(@PathParam("session") String session, TraceTrack track, @Context SecurityContext context) {

		// Step 1 - Validate the session token
		try {
			if (!sDriver.trackingSessionExists(session))
				return generateFailedResponse(1, "Unknown session '" + session + "'.");
			else if (sDriver.isTrackingSessionClosed(session))
				return generateFailedResponse(2, "Session had already been closed.");
		} catch (SessionNotFoundException e) {
			return generateFailedResponse(3, "Failed to upload track because :" + e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("Failed to upload track with session '" + session + "' because : " + e.getMessage());
			return generateFailedResponse(4, "Failed to upload track because :" + e.getMessage());
		}

		try {
			sDriver.closeTrackingSession(session);
		} catch (UnableToPerformOperation e) {
			LOG.error("Failed to close the session because " + e);
			return generateFailedResponse(5, "Failed to close the tracking session because " + e.getMessage());
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				double success;
				Location location;
				TraceVertex v;
				List<TraceVertex> route = new ArrayList<>();
				GraphDB conn = GraphDB.getConnection();

				for (int i = 0; i < track.getTrackSize(); i++) {
					location = track.getLocation(i);

					if (location != null) {

						v = new TraceVertex(location.getLatitude(), location.getLongitude(),
								extractLocationAttributes(location));
						v.setDate(new Date(location.getTimestamp()));
						route.add(v);

					}
				}
				success = conn.getTrackingAPI().submitRoute(session, route);

				if (success == -1) {
					LOG.error("Failed to insert route");
					try {
						sDriver.reopenTrackingSession(session);
						LOG.info("Session {" + session + "} was reopened.");
					} catch (UnableToPerformOperation e) {
						LOG.error(e);
					}
				} else {
					try {
						sDriver.updateSessionDistance(session, success);
						LOG.info("Session {" + session + "} had its total distance updated.");
					} catch (UnableToPerformOperation e) {
						LOG.error("Failed to submit route " + e);
						// return generateFailedResponse(5, "Failed to close the
						// tracking session because "+e.getMessage());
					}
				}
			}
		});

		thread.start();

		// TODO: Correct this so that we can get a better response, i.e., know
		// if the insertion really got done.
		// Right now it always says it has been done correctly.
		// if(failedInserts == 0)
		return generateSuccessResponse();
		// else
		// return generateFailedResponse("Failed to insert "+failedInserts+" out
		// of "+track.getTrackSize()+" locations");
	}

	/**
	 * Enables a tracking application to report its location, at a specific
	 * moment in time.
	 * 
	 * @param sessionId
	 *            The user's session identifier, which operates as a pseudonym.
	 * @param location
	 *            The user's location
	 * 
	 * @return
	 * 
	 * @see BeaconLocation
	 */
	@Deprecated
	@POST
	@Secured
	@Path("/put/beacon")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response put(BeaconLocation location, @Context SecurityContext context) {
		throw new UnsupportedOperationException();
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 *** Get-based Requests ***
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Enables users to query aspects such as previously taken routes
	 * 
	 * @param query
	 *            A generic query that will be dully parsed and processed.
	 * 
	 * @return
	 */
	@POST
	@Secured(Role.user)
	@Path("/query")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response query(TRACEQuery query) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Fetches the coordinates sequence that makes up the route associated with
	 * the provided session identifyer
	 */
	@GET
	@Secured
	@Path("/route/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRouteBySession(@PathParam("sessionId") String sessionId) {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getRouteBySession(sessionId));
	}

	/**
	 * Fetches the list of tracking sessions that are associated with the
	 * specified user.
	 * 
	 * @param username
	 *            The user's username.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessions/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserSessions(@Context SecurityContext context) {
		Gson gson = new Gson();
		String username = context.getUserPrincipal().getName();
		return gson.toJson(mDriver.getUserSessions(username));

	}

	/**
	 * Fetches the list of tracking sessions and corresponding dates that are
	 * associated with the specified user.
	 * 
	 * @param username
	 *            The user's username.
	 * 
	 * @return List of sessions and dates as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessionsAndDates/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserSessionsAndDates(@Context SecurityContext context) {
		Gson gson = new Gson();
		String username = context.getUserPrincipal().getName();
		return gson.toJson(mDriver.getUserSessionsAndDates(username));
	}

	/**
	 * Fetches the list of all tracking sessions.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllSessions() {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getAllSessions());
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 *** REUNIÃO 23/02/2016 ***
	 ************************************************************************
	 ************************************************************************
	 */

	/**
	 * Fetches the coordinates sequence that makes up the route associated with
	 * the provided session identifyer
	 */
	@GET
	@Path("/test/route/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String unsecuredGetRouteBySession(@PathParam("sessionId") String sessionId) {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getRouteBySession(sessionId));
	}

	/**
	 * Fetches the list of tracking sessions that are associated with the
	 * specified user.
	 * 
	 * @param username
	 *            The user's username.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Path("/test/sessions/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String unsecureGetUserSessions(@PathParam("username") String username) {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getUserSessions(username));

	}

	/**
	 * Fetches the list of tracking sessions and corresponding dates that are
	 * associated with the specified user.
	 * 
	 * @param username
	 *            The user's username.
	 * 
	 * @return List of sessions and dates as a Json array.
	 */
	@GET
	@Path("/test/sessionsAndDates/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String unsecuredGetUserSessionsAndDates(@PathParam("username") String username) {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getUserSessionsAndDates(username));
	}

	/**
	 * Fetches the list of all tracking sessions.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Path("/test/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	public String unsecureGetAllSessions() {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getAllSessions());
	}

	
//	return generateFailedResponse(1, "Unknown session '" + session + "'.");

	@POST
	@Secured
	@Path("/put/states")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String putStates(TraceStates states, @Context SecurityContext context) {
		try {
//			LOG.debug("TRACEStoreService.java - putStates");
//			LOG.debug("TRACEStoreService.java - states:" + states.isEmpty());
//			LOG.debug("TRACEStoreService.java - states - names:" + states.getNames().length);
//			LOG.debug("TRACEStoreService.java - states - timeStamps:" + states.getTimeStamps().length);

			String username = context.getUserPrincipal().getName();
//			String username = "kostah50@gmail.com";
			return generateSuccessResponse( gson.toJson(mDriver.putStates(username, states)));
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(1, e.getMessage());
		}
	}
	
	@POST
	@Secured
	@Path("/put/activities")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String putActivities(TraceActivities activities, @Context SecurityContext context) {
		try {
			LOG.debug("TRACEStoreService.java - putActivities");
			String username = context.getUserPrincipal().getName();
			return gson.toJson(mDriver.putActivities(username, activities));
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return generateFailedResponse(1, e.toString());
		}
	}
	
	/*
	 * VERSION 2.0 - New Ijsberg functions
	 */
	private final int MAX_TRIES = 30;
	
	private String getNewUniqueSession() throws UnableToPerformOperation{
		
		String session = null;
		
		int tries = 0;
		
		do{
			session = SecurityUtils.generateSecureActivationToken(32);
			tries++;

			if (tries > MAX_TRIES) 
				throw new UnableToPerformOperation("Can no longer generate unique session code");

		}while(sDriver.trackingSessionExists(session));		
		
		
		return session;
	}
	
	@POST
	@Secured
	@Path("/put/track")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String submitTrackSummary(TrackSummary trackSummary, @Context SecurityContext context){
		
		//TODO: this must be refactored
		//Step 1 - Create a new session
		String session;
		String username = context.getUserPrincipal().getName();

		
		if(!trackSummary.getSession().isEmpty())
			return generateFailedResponse("TODO: maybe already registered");
		
		try {
			session = getNewUniqueSession();
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(e.getMessage());
		}

		
		// Step 2 - Register the session details
		try {
			int userId = uDriver.getUserID(username);
			trackSummary.setSession(session);
			sDriver.registerTrackSummary(userId, trackSummary);
			return generateSuccessResponse(trackSummary.toString());
		} catch (UnableToPerformOperation | UnknownUserException e) {
			return generateFailedResponse(e.getMessage());
		}
	}
	
	@GET
	@Secured
	@Path("/get/track")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrackSummary(@QueryParam("session") String session){
		
		try{
			TrackSummary summary = sDriver.getTrackSummary(session);
			return generateSuccessResponse(summary.toString());
		}catch(UnableToPerformOperation e){
			return generateFailedResponse(e.getMessage());
		}
		
	}
	
	@GET
	@Secured
	@Path("/get/track/trace")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRouteTrace(@QueryParam("session") String session){
		
		try {
			
			List<Location> trace = sDriver.getTrackTrace(session);
			
			JsonArray payload = new JsonArray();
			for(Location location : trace)
				payload.add(location.getLocationAsJsonObject());
			
			return generateSuccessResponse(payload.toString());
			
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return generateFailedResponse(e.getMessage());
		}
		
	}
	
	
	
	@POST
	@Secured
	@Path("/put/track/trace")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String postTraceBatch(@QueryParam("session")String session, Location[] trace, @Context SecurityContext context){
		
		try {
			sDriver.addTrackTraceBatch(session, trace);
			
			if(sDriver.isCompleteRoute(session))
				new Thread(new TraceToGraphRunnable(session)).start();
			
			return generateSuccessResponse();
			
		} catch (UnableToPerformOperation e) {
			return generateFailedResponse(e.getMessage());
		}
		
	}
	
	@GET
	@Secured
	@Path("/get/track/digest")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserRoutes(@Context SecurityContext context){
		
		String username = context.getUserPrincipal().getName();
		
		try {
			
			int userID = uDriver.getUserID(username);
			List<TrackSummary> sessions = sDriver.getUsersTrackSummaries(userID);
			
			JsonArray jSessions = new JsonArray();
			for(TrackSummary summary : sessions)
				jSessions.add(summary.getSession());
			
			return generateSuccessResponse(jSessions.toString());
			
		} catch (UnknownUserException | UnableToPerformOperation e) {
			return generateFailedResponse(e.getMessage());
		}	
	}
	
	
	@GET
	@Path("/sample/track/summary")
	@Produces(MediaType.APPLICATION_JSON)
	public TrackSummary sampleTrackSummary(){
		TrackSummary summary = new TrackSummary();

		summary.setSession("fakeSession");
		summary.setStartedAt(System.currentTimeMillis());
		summary.setEndedAt(System.currentTimeMillis()+5000);
		summary.setElapsedDistance(500);
		summary.setElapsedTime(4);
		summary.setPoints(0);
		summary.setAvgSpeed(3.5f);
		summary.setTopSpeed(4f);
		summary.setModality(3);
		
		return summary;
	}
	
	/* Helpers
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	
	private class TraceToGraphRunnable implements Runnable {
		
		private final String session;
		
		public TraceToGraphRunnable(String session) {
			this.session = session;
		}
		
		@Override
		public void run() {

			double success;
			TraceVertex v;
			List<TraceVertex> route = new ArrayList<>();
			GraphDB conn = GraphDB.getConnection();

			List<Location> routeTrace;
			try {
				routeTrace = sDriver.getTrackTrace(session);
				
				for(Location waypoint : routeTrace){
					if (waypoint != null) {

						v = new TraceVertex(
									waypoint.getLatitude(),
									waypoint.getLongitude(),
									extractLocationAttributes(waypoint));
						
						v.setDate(new Date(waypoint.getTimestamp()));
						
						route.add(v);
					}
				}
				
				success = conn.getTrackingAPI().submitRoute(session, route);
			} catch (UnableToPerformOperation e1) {
				LOG.error(e1.getMessage());
				success = -1;
			}
			
			

			if (success == -1) {
				LOG.error("Failed to insert route");
			} else {
				try {
					sDriver.closeTrackingSession(session);
					sDriver.updateSessionDistance(session, success);
					LOG.info("Session {" + session + "} had its total distance updated.");
				} catch (UnableToPerformOperation e) {
					LOG.error("Failed to submit route " + e);
				}
			}
		}
	}
	
	/* TESTING - Please remove before release
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	@GET
	@Path("/test/get/track/trace")
	@Produces(MediaType.APPLICATION_JSON)
	public String testGetRouteTrace(@QueryParam("session") String session){
		
		try {
			
			List<Location> trace = sDriver.getTrackTrace(session);
			
			JsonArray payload = new JsonArray();
			for(Location location : trace)
				payload.add(location.getLocationAsJsonObject());
			
			return generateSuccessResponse(payload.toString());
			
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return generateFailedResponse(e.getMessage());
		}
		
	}
}
