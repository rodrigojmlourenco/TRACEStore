package org.trace.store.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.drivers.utils.SecurityUtils;
import org.trace.store.services.security.Secured;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class); 
	
	
	private final int MAX_TRIES = 30;
	
	private TRACESecurityManager manager = TRACESecurityManager.getManager();
	
	private UserDriver userDriver		= UserDriverImpl.getDriver();
	private SessionDriver sessionDriver = SessionDriverImpl.getDriver();
	
	private JSONObject generateError(int code, String message){
		JSONObject error = new JSONObject();
		
		try {
			error.append("success", false);
			error.append("code", code);
			error.append("error", message);
			return error;
		} catch (JSONException e) {			
			e.printStackTrace();
			return null;
		}
	}
	/**
	 *   
	 * @param username The user's unique username.
	 * @param password The user's corresponding password.
	 * 
	 * @return Reponse object, whose body contains the session identifier.
	 */
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject login(@QueryParam("username") String username, @QueryParam("password") String password){
		
		//Step 1 - Check if the user's account is activated
		if(!manager.isActiveUser(username))
			return generateError(1, username+" has not activated his account yet");
		
		//Step 2 - Validate the provided password against the one stored in the database
		if(!manager.validateUser(username, password))
			return generateError(2, "Invalid password or username");
		
		LOG.debug("<"+username+"> presented the correct password.");

		//Step 3 - Issue a new token and provide it to the user
		String session;
		int tries = 0;
		try {
			do{
				session = SecurityUtils.generateSecureActivationToken(32);
				tries++;
				
				if (tries > MAX_TRIES) {
					return generateError(5, "Can no longer generate unique session code");
				}
				
			}while(sessionDriver.trackingSessionExists(session));
		}catch (UnableToPerformOperation e) {
			return generateError(3, e.getMessage());
		}
		
		
		try {
			sessionDriver.openTrackingSession(userDriver.getUserID(username), session);
			GraphDB graphDB = GraphDB.getConnection();
			graphDB.getTrackingAPI().login(username, session);
		} catch (UnableToPerformOperation e) {
			return generateError(4, e.getMessage());
		}
		
		String authToken = manager.issueToken(username, session);
		
		Log.debug("Session { "+authToken+" } attributted to user "+username+", the token contains the session.");
		
		
		JSONObject token = new JSONObject();
		try {
			token.append("success", true);
			token.append("token", authToken);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	/**
	 * Terminates a user's session.
	 * 
	 * @return
	 */
	@POST
	@Secured
	@Path("/logout")
	public Response logout(){
		throw new UnsupportedOperationException();
	}
	
	@POST
	@Path("/activate")
	public Response activate(@QueryParam("token") String token){
		throw new UnsupportedOperationException();
	}
}
