package org.trace.store.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.trace.store.services.api.BeaconLocation;
import org.trace.store.services.api.GeoLocation;
import org.trace.store.services.api.PrivacyPolicies;
import org.trace.store.services.api.TRACEQuery;
import org.trace.store.services.api.TraceTrack;
import org.trace.store.services.api.UserRegistryRequest;

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
		throw new UnsupportedOperationException();
	}
	
	/**
	 *  Enables a tracking application to initiate a session. The received sessionID will 
	 *  then be used to identify all the information tracked by that specific application,
	 *  and during that session.
	 *   
	 * @param username The user's unique username.
	 * @param password The user's corresponding password.
	 * 
	 * @return Reponse object, whose body contains the session identifier.
	 */
	@POST
	@Path("/login")
	public Response login(@QueryParam("username") String username, @QueryParam("password") String password){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Terminates a user's session.
	 * 
	 * @return
	 */
	@POST
	@Path("/logout")
	public Response logout(){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Allows users to set security and privacy policies about their data. 
	 *   
	 * @param policies The privacy policies.
	 * @return Response notifying if the policies submission was successful or not.
	 */
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
	@Path("/put/geo/{sessionId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response put(@PathParam("sessionId") String sessionId, GeoLocation location){
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
	@GET
	@Path("/query")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response query(TRACEQuery query){
		throw new UnsupportedOperationException();
	}	
}
