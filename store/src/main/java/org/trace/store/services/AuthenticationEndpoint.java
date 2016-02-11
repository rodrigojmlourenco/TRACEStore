package org.trace.store.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.backend.GraphDB;
import org.trace.store.services.security.Secured;

@Path("/auth")
public class AuthenticationEndpoint {

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationEndpoint.class); 
	
	/**
	 *   
	 * @param username The user's unique username.
	 * @param password The user's corresponding password.
	 * 
	 * @return Reponse object, whose body contains the session identifier.
	 */
	@POST
	@Path("/login")
	public Response login(@QueryParam("username") String username, @QueryParam("password") String password){
		
		GraphDB graphDB = GraphDB.getConnection();
		String session = graphDB.getTrackingAPI().login(username, password);
		
		LOG.debug("{ username: "+username+", password: "+password+"}");
		
		if(!session.isEmpty())
			return Response.ok(session).build();
		else
			return Response.ok("Failed to login").build();
		
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
