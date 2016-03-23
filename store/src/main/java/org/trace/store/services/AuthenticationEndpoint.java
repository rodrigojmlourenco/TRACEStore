package org.trace.store.services;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;
import org.trace.store.filters.Secured;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.ExpiredTokenException;
import org.trace.store.middleware.drivers.exceptions.SessionNotFoundException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.drivers.utils.SecurityUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = Logger.getLogger(AuthenticationEndpoint.class); 


	private final int MAX_TRIES = 30;

	private TRACESecurityManager manager = TRACESecurityManager.getManager();

	private UserDriver userDriver		= UserDriverImpl.getDriver();
	private SessionDriver sessionDriver = SessionDriverImpl.getDriver();

	private Gson gson = new Gson();

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
	@Produces(MediaType.APPLICATION_FORM_URLENCODED)
	public String login(@FormParam("username") String username, @FormParam("password") String password){

		//Step 1 - Check if the user's account is activated
		try {
			if(!manager.isActiveUser(username)){
				LOG.error("User '"+username+"' attempted to loggin without an active account");
				return generateError(1, username+" has not activated his account yet");
			}
		} catch (UnknownUserException e1) {
			LOG.error("Unknown user '"+username+"' attempted to loggin.");
			return generateError(2, e1.getMessage());
		}

		//Step 2 - Validate the provided password against the one stored in the database
		if(!manager.validateUser(username, password)){
			LOG.error("User '"+username+"' attempted to loggin with invalid credentials");
			return generateError(3, "Invalid password or username");
		}

		//Step 3 - Issue a new JWT token and provide it to the user
		String authToken;
		try{
			authToken = manager.issueToken(username);
		}catch(Exception e){
			LOG.error("User '"+username+"' attempted to loggin, however he was unable because: "+e.getMessage());
			return generateError(6, e.getMessage());
		}

		JsonObject token = new JsonObject();
		token.addProperty("success", true);
		token.addProperty("token", authToken);

		return gson.toJson(token);
	}

	/**
	 * 
	 * @param idToken
	 * @return
	 */
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@FormParam("auth") String idToken){

		try{
			JsonFactory jsonFactory = new GsonFactory();
			NetHttpTransport transport = new NetHttpTransport();
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(transport, jsonFactory);


			GoogleIdToken.Payload payload = null;

			try {
				GoogleIdToken token = GoogleIdToken.parse(jsonFactory, idToken);

				if(verifier.verify(token)){
					return payload.toPrettyString();
				}else
					return "Verification failed...";

			} catch (IOException e) {
				LOG.error(e);
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				LOG.error(e);
				e.printStackTrace();
			}
		}catch(Exception exp){
			LOG.error(exp);
			exp.printStackTrace();
		}

		return "hello world";
	}

	/**
	 * Terminates a user's session.
	 * 
	 * @return
	 */
	@POST
	@Secured
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public String logout(@Context SecurityContext securityContext){
		//TODO: invalidate session
		LOG.debug("Logging out "+securityContext.getUserPrincipal().getName());
		return generateError(1, "Method not implemented yet!");
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
			}else{
				LOG.error("User was not successfully activated");
				return generateError(3, "User was not successfully activated");
			}

		} catch (ExpiredTokenException e) {
			LOG.error("User attempted to activate his account, however failed to do so because: "+e.getMessage());
			return generateError(1, e.getMessage());
		} catch (UnableToPerformOperation e) {
			LOG.error("User attempted to activate his account, however failed to do so because: "+e.getMessage());
			return generateError(2, e.getMessage());
		}
	}

	@POST
	@Secured
	@Path("/session/open")
	@Produces(MediaType.APPLICATION_JSON)
	public String openTrackingSession(@Context SecurityContext context){

		String session;
		String username = context.getUserPrincipal().getName();

		int tries = 0;
		try {
			do{
				session = SecurityUtils.generateSecureActivationToken(32);
				tries++;

				if (tries > MAX_TRIES) {
					return generateError(1, "Can no longer generate unique session code");
				}

			}while(sessionDriver.trackingSessionExists(session));
		}catch (UnableToPerformOperation e) {
			return generateError(3, e.getMessage());
		}


		try {
			sessionDriver.openTrackingSession(userDriver.getUserID(username), session);

			GraphDB graphDB = GraphDB.getConnection();

			JsonObject response = new JsonObject();
			response.addProperty("success", true);
			response.addProperty("session", session);

			return gson.toJson(response);

		} catch (UnableToPerformOperation e) {
			return generateError(2, e.getMessage());
		} catch (Exception e) {
			return generateError(3, e.getMessage());
		}
	}

	@POST
	@Secured
	@Path("/close")
	@Produces(MediaType.APPLICATION_JSON)
	public String closeTrackingSession(@FormParam("session") String session){

		try {

			if(sessionDriver.isTrackingSessionClosed(session))
				return generateError(1, "Session had already been closed.");
			else
				sessionDriver.closeTrackingSession(session);

			return generateSuccess();

		} catch (UnableToPerformOperation e) {
			return generateError(2, e.getMessage());
		} catch (SessionNotFoundException e) {
			return generateError(3, e.getMessage());
		}
	}
}