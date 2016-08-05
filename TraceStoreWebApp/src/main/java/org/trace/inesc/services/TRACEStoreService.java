package org.trace.inesc.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.trace.inesc.filters.Role;
import org.trace.inesc.filters.Secured;
import org.trace.inesc.services.utils.RequestUtils;
import org.trace.inesc.services.utils.ResponseUtils;
import org.trace.inesc.store.middleware.TRACEStore;
import org.trace.inesc.store.middleware.backend.GraphDB;
import org.trace.inesc.store.middleware.drivers.SessionDriver;
import org.trace.inesc.store.middleware.drivers.TRACETrackingDriver;
import org.trace.inesc.store.middleware.drivers.UserDriver;
import org.trace.inesc.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.inesc.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.inesc.store.middleware.drivers.utils.FormFieldValidator;
import org.trace.inesc.store.middleware.drivers.utils.SecurityUtils;
import org.trace.inesc.store.middleware.exceptions.EmailAlreadyRegisteredException;
import org.trace.inesc.store.middleware.exceptions.InvalidEmailException;
import org.trace.inesc.store.middleware.exceptions.InvalidPasswordException;
import org.trace.inesc.store.middleware.exceptions.InvalidUsernameException;
import org.trace.inesc.store.middleware.exceptions.NonMatchingPasswordsException;
import org.trace.inesc.store.middleware.exceptions.UnableToPerformOperation;
import org.trace.inesc.store.middleware.exceptions.UnableToRegisterUserException;
import org.trace.inesc.store.middleware.exceptions.UnknownUserException;
import org.trace.inesc.store.middleware.exceptions.UsernameAlreadyRegisteredException;
import org.trace.inesc.store.services.data.Location;
import org.trace.inesc.store.services.data.TraceActivities;
import org.trace.inesc.store.services.data.TraceStates;
import org.trace.inesc.store.services.data.TraceTrack;
import org.trace.inesc.store.services.data.TrackSummary;
import org.trace.inesc.store.services.data.UserRegistryRequest;
import org.trace.inesc.store.storage.data.TraceVertex;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * In order for higher-level information to be acquired, the data acquired by
 * the tracking applications must be aggregated and interpreted in a centralized
 * component – the TRACEstore. This API specifies the set of operations
 * supported by TRACEstore for the uploading and querying of information.
 * 
 * TODO: uniformizar os nomes usados summary/digest, track/route, etc
 * TODO: migrate getNewUniqueSession somewhere else, maybe to the AuthenticationManager
 * TODO: criar método para unregister/delete user
 * TODO: criar método para fazer update user details
 * TODO: garantir que em metodos que trabalham com tracks, as tracks pertencem ao utilizador
 */
@Path("/tracker")
public class TRACEStoreService {

	private final Logger LOG = Logger.getLogger(TRACEStoreService.class);

	private final int MAX_TRIES = 30;
	
	private Gson gson = new Gson();
	private UserDriver uDriver = UserDriverImpl.getDriver();
	private SessionDriver sDriver = SessionDriverImpl.getDriver();
	private TRACETrackingDriver mDriver = TRACEStore.getTRACEStore();

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
				return ResponseUtils.generateFailedResponse(8, "Invalid name");

			if (!phone.isEmpty() && !FormFieldValidator.isValidPhoneNumber(phone))
				return ResponseUtils.generateFailedResponse(9, "Invalid phone number");

			if (!address.isEmpty() && !FormFieldValidator.isValidAddress(address))
				return ResponseUtils.generateFailedResponse(10, "Invalid address");

		} catch (InvalidEmailException e) {
			return ResponseUtils.generateFailedResponse(2, "Invalid email address");
		} catch (InvalidUsernameException e) {
			return ResponseUtils.generateFailedResponse(1, "Invalid username");
		} catch (InvalidPasswordException e) {
			return ResponseUtils.generateFailedResponse(3, "Invalid password");
		} catch (UsernameAlreadyRegisteredException e) {
			return ResponseUtils.generateFailedResponse(4, "Username already registered");
		} catch (EmailAlreadyRegisteredException e) {
			return ResponseUtils.generateFailedResponse(5, "Email address already registered");
		} catch (NonMatchingPasswordsException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return ResponseUtils.generateFailedResponse(6, "Non matching passwords.");
		} catch (SQLException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return ResponseUtils.generateFailedResponse(7, e.getMessage());
		}

		try {
			activationToken = uDriver.registerUser(request.getUsername(), request.getEmail(), request.getPassword(),
					request.getConfirm(), request.getName(), request.getAddress(), request.getPhone(), Role.user);

			return ResponseUtils.generateSuccessResponse(activationToken);
		} catch (UnableToRegisterUserException e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return ResponseUtils.generateFailedResponse(7, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("User '" + request.getUsername() + "' not registered, because " + e.getMessage());
			return ResponseUtils.generateFailedResponse(7, e.getMessage());
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
	 * Creates a new track summary, which is uniquely identified by a generated session pseudonym.
	 * <br>
	 * The traced route is not part of the track summary, and must be separately submitted.
	 * @param trackSummary The track summary
	 * @param context The user's security context
	 * 
	 * @return An updated version of the submitted track summary, with corresponding session pseudonym.
	 * 
	 * @see TrackSummary
	 */
	@POST
	@Secured
	@Path("/put/track")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String submitTrackSummary(TrackSummary trackSummary, @Context SecurityContext context){
		
		//Step 1 - Create a new session
		String session;
		String username = context.getUserPrincipal().getName();

		
		if(!trackSummary.getSession().isEmpty())
			return ResponseUtils.generateFailedResponse("TODO: maybe already registered");
		
		try {
			session = getNewUniqueSession();
		} catch (UnableToPerformOperation e) {
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}

		
		// Step 2 - Register the session details
		try {
			int userId = uDriver.getUserID(username);
			trackSummary.setSession(session);
			sDriver.registerTrackSummary(userId, trackSummary);
			return ResponseUtils.generateSuccessResponse(trackSummary.toString());
		} catch (UnableToPerformOperation | UnknownUserException e) {
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}
	}
	
	/**
	 * Registers the whole or partial traced route identified by the provided session, which uniquely identified a
	 * previously registered track summary.
	 * <br>
	 * Furthermore, once the track becomes complete, i.e. the number of expected locations matches the number of
	 * stored ones, the track is stored in the graph database.
	 *  
	 * @param session The track summary unique identifier.
	 * @param trace The whole or partial route trace as a sequence of locations.
	 * @param context The user's security context.
	 * 
	 * @return Success if the trace was successfully store, false otherwise.
	 */
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
			
			return ResponseUtils.generateSuccessResponse();
			
		} catch (UnableToPerformOperation e) {
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}
		
	}

	/*
	 ************************************************************************
	 ************************************************************************
	 *** Get-based Requests ***
	 ************************************************************************
	 ************************************************************************
	 */

	
	/**
	 * Fetches the coordinates sequence that makes up the route associated with
	 * the provided session identifyer
	 * @param sessionId
	 * @return
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
	 * @param username The user's username.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessions")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserSessions(@Context SecurityContext context) {
		Gson gson = new Gson();
		String username = context.getUserPrincipal().getName();
		return gson.toJson(mDriver.getUserSessions(username));

	}

	/**
	 * <b>DEPRECATED: </b> please start using /sessions<br><br>
	 * Fetches the list of tracking sessions and corresponding dates that are
	 * associated with the specified user.
	 * 
	 * @param username The user's username.
	 * 
	 * @return List of sessions and dates as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessionsAndDates/")
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	public String getUserSessionsAndDates(@Context SecurityContext context) {
		Gson gson = new Gson();
		String username = context.getUserPrincipal().getName();
		return gson.toJson(mDriver.getUserSessions(username));
	}

	/**
	 * Fetches the list of all tracking sessions.
	 * 
	 * @return List of sessions as a Json array.
	 */
	@GET
	@Secured
	@Path("/sessions/all")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllSessions(@Context SecurityContext context) {
		//TODO: assure from the security context that this user is an admin
		Gson gson = new Gson();
		return gson.toJson(mDriver.getAllSessions());
	}
	

	/**
	 * Fetches the track summary of the track identified by the provided session.
	 * 
	 * @param session Session pseudonym that uniquely identifies the track.
	 * 
	 * @return The track summary as payload, if the operation was successful.
	 */
	@GET
	@Secured
	@Path("/get/track")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrackSummary(@QueryParam("session") String session){
		
		try{
			TrackSummary summary = sDriver.getTrackSummary(session);
			return ResponseUtils.generateSuccessResponse(summary.toString());
		}catch(UnableToPerformOperation e){
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}
		
	}
	
	/**
	 * Fetches the traced route of the track idenfied by the provided session.
	 * 
	 * @param session Session pseudonym that uniquely identified the track.
	 * 
	 * @return The track's trace route as payload, if the operation was successful.  
	 */
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
			
			return ResponseUtils.generateSuccessResponse(payload.toString());
			
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}
	}
	
	/**
	 * Fetches all the tracks associated with the user, which is identifiable from 
	 * the provided security context.
	 * 
	 * @param context The user's security context.
	 * 
	 * @return List of the user's track summaries as payload, if the operation is successful.
	 * 
	 * TODO: arranjar um nome melhor para este método
	 */
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
			
			return ResponseUtils.generateSuccessResponse(jSessions.toString());
			
		} catch (UnknownUserException | UnableToPerformOperation e) {
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}	
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
									RequestUtils.extractLocationAttributes(waypoint));
						
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
			
			return ResponseUtils.generateSuccessResponse(payload.toString());
			
		} catch (UnableToPerformOperation e) {
			e.printStackTrace();
			return ResponseUtils.generateFailedResponse(e.getMessage());
		}
		
	}
	
	/**
	 * Fetches the coordinates sequence that makes up the route associated with
	 * the provided session identifier
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
	 * @param username The user's username.
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
	 * @param username The user's username.
	 * 
	 * @return List of sessions and dates as a Json array.
	 */
	@GET
	@Path("/test/sessionsAndDates/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public String unsecuredGetUserSessionsAndDates(@PathParam("username") String username) {
		Gson gson = new Gson();
		return gson.toJson(mDriver.getUserSessions(username));
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
	
	
	/* TODO: assess if the following are necessary
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	@POST
	@Secured
	@Path("/put/states")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String putStates(TraceStates states, @Context SecurityContext context) {
		try {

			String username = context.getUserPrincipal().getName();
			return ResponseUtils.generateSuccessResponse( gson.toJson(mDriver.putStates(username, states)));
		} catch (UnableToPerformOperation e) {
			return ResponseUtils.generateFailedResponse(1, e.getMessage());
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
			return ResponseUtils.generateFailedResponse(1, e.toString());
		}
	}
}
