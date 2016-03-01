package org.trace.store.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.ExpiredTokenException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.drivers.utils.SecurityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class); 
	
	
	private final int MAX_TRIES = 30;
	
	private TRACESecurityManager manager = TRACESecurityManager.getManager();
	
	private UserDriver userDriver		= UserDriverImpl.getDriver();
	private SessionDriver sessionDriver = SessionDriverImpl.getDriver();
	
	private Gson gson = new Gson();
	
	private String extractSessionFromSecurityContext(SecurityContext context){
		return context.getUserPrincipal().getName();
	}
	
	private String generateError(int code, String message){
		JsonObject error = new JsonObject();
		error.addProperty("success", false);
		error.addProperty("code", code);
		error.addProperty("error", message);
		return gson.toJson(error);
		
	}
	
	private String generateSuccess(){
		JsonObject success = new JsonObject();
		success.addProperty("success", true);
		return gson.toJson(success);
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
	public String login(@QueryParam("username") String username, @QueryParam("password") String password){
		
		//Step 1 - Check if the user's account is activated
		if(!manager.isActiveUser(username))
			return generateError(1, username+" has not activated his account yet");
		
		//Step 2 - Validate the provided password against the one stored in the database
		if(!manager.validateUser(username, password))
			return generateError(2, "Invalid password or username");
		
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
		} catch (Exception e) {
			return generateError(5, e.getMessage());
		}
		
		String authToken;
		try{
			authToken = manager.issueToken(username,session);
		}catch(Exception e){
			return generateError(6, e.getMessage());
		}
		
		JsonObject token = new JsonObject();
		token.addProperty("success", true);
		token.addProperty("token", authToken);
		
		return gson.toJson(token);
	}
	
	/**
	 * Terminates a user's session.
	 * 
	 * @return
	 */
	@POST
	@Secured
	@Path("/logout")
	public Response logout(@Context SecurityContext securityContext){
		LOG.debug("TODO: close the session :"+extractSessionFromSecurityContext(securityContext));
		return Response.ok().build();
	}
	
	@POST
	@Path("/activate")
	@Produces({MediaType.APPLICATION_JSON})
	public String activate(@QueryParam("token") String token){
		
		LOG.debug("Activating the account with activation token "+token);
		
		try {
			if(userDriver.activateAccount(token)){
				LOG.info("User account activated.");
				return generateSuccess();
			}else
				return generateError(3, "User was not successfully activated");
						
		} catch (ExpiredTokenException e) {
			return generateError(1, e.getMessage());
		} catch (UnableToPerformOperation e) {
			return generateError(2, e.getMessage());
		}
	}
}
