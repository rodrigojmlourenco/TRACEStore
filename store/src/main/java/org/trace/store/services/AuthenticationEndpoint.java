package org.trace.store.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.trace.store.security.Secured;

@Path("/auth")
public class AuthenticationEndpoint {

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
		throw new UnsupportedOperationException();
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
