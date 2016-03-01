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
import org.trace.store.services.security.Secured;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class); 
	
	
	private TRACESecurityManager manager = TRACESecurityManager.getManager();
	
	
	
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
		
		//TODO: activate this clause
		//Step 1 - Check if the user's account is activated
		/*
		if(!manager.isActiveUser(username))
			return generateError(1, username+" has not activated his account yet");
		*/
		
		LOG.debug("Skipping active verification for user "+username); //TODO: remover
		
		//Step 2 - Validate the provided password against the one stored in the database
		if(!manager.validateUser(username, password))
			return generateError(2, "Invalid password or username");
		
		LOG.debug("<"+username+"> presented the correct password.");

		//Step 3 - Issue a new token and provide it to the user
		String session = manager.issueToken(username);
		
		Log.debug("Session { "+session+" } attributted to user "+username+".");
		
		//TODO: store the token in mariaDB
		
		JSONObject token = new JSONObject();
		try {
			token.append("success", true);
			token.append("token", session);
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
