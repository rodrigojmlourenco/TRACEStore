package org.trace.store;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.trace.store.services.security.AuthenticationFilter;
import org.trace.store.services.security.AuthorizationFilter;

public class TraceStoreApp extends ResourceConfig{

	public TraceStoreApp(){
		 // Register resources and providers using package-scanning.
        packages("org.trace.store.services");
 
        // Register my custom provider - not needed if it's in my.package.
        register(AuthenticationFilter.class);
        register(AuthorizationFilter.class);
        
        // Register an instance of LoggingFilter.
        //register(new LoggingFilter(LOGGER, true));
 
        // Enable Tracing support.
        property(ServerProperties.TRACING, "ALL");
	}
	
}
