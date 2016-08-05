package org.trace.inesc.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.trace.inesc.filters.Role;
import org.trace.inesc.filters.Secured;
import org.trace.inesc.services.utils.ResponseUtils;
import org.trace.inesc.store.middleware.TRACESecurityManager;
import org.trace.inesc.store.middleware.TRACESecurityManager.TokenType;
import org.trace.inesc.store.middleware.drivers.UserDriver;
import org.trace.inesc.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.inesc.store.middleware.exceptions.ExpiredTokenException;
import org.trace.inesc.store.middleware.exceptions.InvalidAuthTokenException;
import org.trace.inesc.store.middleware.exceptions.UnableToPerformOperation;
import org.trace.inesc.store.middleware.exceptions.UnableToRegisterUserException;
import org.trace.inesc.store.middleware.exceptions.UnknownUserException;
import org.trace.inesc.store.middleware.exceptions.UserRegistryException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = Logger.getLogger(AuthenticationEndpoint.class); 


	private Gson gson = new Gson();
	private UserDriver userDriver		= UserDriverImpl.getDriver();
	private TRACESecurityManager manager = TRACESecurityManager.getManager();
		

	private String performNativeLogin(String username, String password){

		try {
			
			if(!manager.isActiveUser(username)){
				LOG.error("User '"+username+"' attempted to loggin without an active account");
				return ResponseUtils.generateError(2, username+" has not activated his account yet");
			}
		} catch (UnknownUserException e1) {
			LOG.error("Unknown user '"+username+"' attempted to loggin.");
			return ResponseUtils.generateError(1, e1.getMessage());
		}

		//Step 2 - Validate the provided password against the one stored in the database
		if(!manager.validateUser(username, password)){
			LOG.error("User '"+username+"' attempted to loggin with invalid credentials");
			return ResponseUtils.generateError(3, "Invalid password or username");
		}

		//Step 3 - Issue a new JWT token and provide it to the user
		String authToken;
		try{
			authToken = manager.issueToken(username);
		}catch(Exception e){
			LOG.error("User '"+username+"' attempted to loggin, however he was unable because: "+e.getMessage());
			return ResponseUtils.generateError(6, e.getMessage());
		}

		JsonObject token = new JsonObject();
		token.addProperty("success", true);
		token.addProperty("token", authToken);
		
		//Register the token for future references
		manager.registerToken(authToken, TokenType.trace);

		return gson.toJson(token);
	}

	private String performFederatedLogin(String idToken){

		Payload payload;
		
		//Step 1 - Validate the token
		try {

			payload = manager.validateGoogleAuthToken(idToken);
			manager.registerToken(idToken, TokenType.google);
			
		} catch (InvalidAuthTokenException e) {
			return ResponseUtils.generateError(4, e.getMessage());
		}

		// Step 2 - Check if user exists, and if not register him
		try {
			
			// Step 2a - The user exists -> return the token
			userDriver.getUserID(payload.getSubject());
			
		} catch (UnknownUserException e){
			
			//Step 2b - The user doesnt exist -> create new user and return the token
			String username = payload.getSubject();
			String email = payload.getEmail();
			String name = String.valueOf(payload.get("name"));
			
			try {
			
				String activationToken = userDriver.registerFederatedUser(username, email, name);
				userDriver.activateAccount(activationToken);
			
			} catch (UserRegistryException | UnableToRegisterUserException | UnableToPerformOperation e1) {
				return ResponseUtils.generateError(5, e.getMessage());
			} catch (ExpiredTokenException e1) {
				return ResponseUtils.generateError(6, e.getMessage());
			}
			
		} catch (UnableToPerformOperation  e) {
			return ResponseUtils.generateError(7, e.getMessage());
		}
		
		JsonObject jToken = new JsonObject();
		jToken.addProperty("success", true);
		jToken.addProperty("token", idToken);
		
		return gson.toJson(jToken);
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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@FormParam("username") String username, @FormParam("password") String password, @FormParam("token") String idToken){

		String response ;
		
		if(idToken != null && !idToken.isEmpty()){
			LOG.info("Federated login...");
			response = performFederatedLogin(idToken);
		}else{
			LOG.info("Native login...");
			response = performNativeLogin(username, password);
		}

		return response;

	}



	/**
	 * Terminates a user's session.
	 * 
	 * @return
	 */
	@POST
	@Secured
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String logout(@FormParam("token") String token, @Context SecurityContext securityContext){
		
		manager.unregisterToken(token);
		
		LOG.info(securityContext.getUserPrincipal().getName()+" has logged out.");
		
		return ResponseUtils.generateSuccess();
	}
	
	@GET
	@Secured
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	public String checkToken(){
		//TODO: esta seria uma boa oportunidade para renover o token talvez...
		return ResponseUtils.generateSuccess();
	}

	@POST
	@Path("/activate")
	@Produces({MediaType.APPLICATION_JSON})
	public String activate(@QueryParam("token") String token){

		LOG.debug("Activating the account with activation token "+token);

		try {
			if(userDriver.activateAccount(token)){
				LOG.info("User account activated.");
				return ResponseUtils.generateSuccess();
			}else{
				LOG.error("User was not successfully activated");
				return ResponseUtils.generateError(3, "User was not successfully activated");
			}

		} catch (ExpiredTokenException e) {
			LOG.error("User attempted to activate his account, however failed to do so because: "+e.getMessage());
			return ResponseUtils.generateError(1, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("User attempted to activate his account, however failed to do so because: "+e.getMessage());
			return ResponseUtils.generateError(2, e.getMessage());
		}
	}

	
	@GET
	@Secured
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUserRoles(@Context SecurityContext context){
		
		
		Gson gson = new Gson();
		JsonArray roles = new JsonArray();
		String user = context.getUserPrincipal().getName();
		
		try {
			List<Role> rolesAsList = userDriver.getUserRoles(user);
			for(Role r : rolesAsList)
				roles.add(r.toString());
			
			return gson.toJson(roles);
			
		} catch (UnableToPerformOperation | UnknownUserException e) {
			return ResponseUtils.generateError(1, e.getMessage());
		}
	}
}