package org.trace.store.filters;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.TRACESecurityManager;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter{

	private final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class); 
	
	private TRACESecurityManager manager = TRACESecurityManager.getManager();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		final String session;
		
		
		// Get the HTTP Authorization header from the request
		String authorizationHeader = 
				requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Check if the HTTP Authorization header is present and formatted correctly 
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new NotAuthorizedException("Authorization header must be provided");
		}

		// Extract the token from the HTTP Authorization header
		String token = authorizationHeader.substring("Bearer".length()).trim();

		try {

			// Validate the token
			session = validateToken(token);
			
			if(session == null || session.isEmpty())
				throw new Exception();
			
			requestContext.setSecurityContext(new SecurityContext() {
				
				@Override
				public boolean isUserInRole(String arg0) {
					return true;
				}
				
				@Override
				public boolean isSecure() {
					return false;
				}
				
				@Override
				public Principal getUserPrincipal() {
					return new Principal() {
						
						@Override
						public String getName() {
							return session;
						}
					};
				}
				
				@Override
				public String getAuthenticationScheme() {
					// TODO Auto-generated method stub
					return null;
				}
			});

		} catch (Exception e) {
			requestContext.abortWith(
					Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}


	private String validateToken(String token) throws Exception {

		try {
			String session = manager.validateAndExtractSession(token);
			
			if(session!=null && !session.isEmpty())
				return session;
			else
				return null;
			
		}catch(Exception e){
			LOG.error(e.getMessage());
			return null;
		}
	}
}