package org.trace.store.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.TRACEStore;
import org.trace.store.middleware.drivers.TRACETrackingDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UserRegistryException;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.services.api.BeaconLocation;
import org.trace.store.services.api.GeoLocation;
import org.trace.store.services.api.PrivacyPolicies;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.UserRegistryRequest;
import org.trace.store.services.security.Role;
import org.trace.store.services.security.Secured;

/**
 * In order for higher-level information to be acquired, the data acquired by 
 * the tracking applications must  be  aggregated  and  interpreted  in  a 
 * centralized  component  –  the  TRACEstore.  This  API specifies  the  set
 * of  operations  supported  by  TRACEstore  for  the  uploading  and  querying
 * of information. 
 */
@Path("/trace")
public class TRACEStoreService {

	private final String LOG_TAG = "TRACEStoreService"; 

	private final Logger LOG = LoggerFactory.getLogger(TRACEStoreService.class); 
	
	private UserDriver uDriver = UserDriverImpl.getDriver();
	private TRACETrackingDriver mDriver = TRACEStore.getTRACEStore();
	private TRACESecurityManager secMan = TRACESecurityManager.getManager();

	@Path("/test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test(){
		return "Welcome to the "+LOG_TAG;
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

			mDriver.registerUser(request.getUsername(),
									request.getPassword(),
									request.getName(),
									request.getAddress());
			

			return Response.ok(activationToken).build();

		} catch (UserRegistryException e) {
			return Response.ok(e.getMessage()).build();
		} catch (NonMatchingPasswordsException e) {
			return Response.ok(e.getMessage()).build();
		} catch (UnableToRegisterUserException e) {
			return Response.ok(e.getMessage()).build();
		} catch (UnableToPerformOperation e) {
			return Response.ok(e.getMessage()).build();
		}
	}

	@POST
	@Secured(Role.user)
	@Path("/session")
	public Response generateSessionId(){
		return Response.ok(secMan.generateSessionPseudonym()).build();
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
	@Secured(Role.user)
	@Path("/put/geo/{sessionId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response put(@PathParam("sessionId") String sessionId, GeoLocation location){
		
		LOG.debug(sessionId);
		Log.debug(location.toString());
		
		throw new UnsupportedOperationException();
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
	@Secured(Role.user)
	@Path("/put/beacon/{sessionId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response put(@PathParam("sessionId") String sessionId, BeaconLocation location){
		throw new UnsupportedOperationException();
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
	@Secured(Role.user)
	@Path("/put/track/{sessionId}")
	public Response put(@PathParam("sessionId") String sessionId, TraceTrack track){
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
}
