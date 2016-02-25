package org.trace.store;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.TRACEStore;

public class TraceStoreApp extends ResourceConfig{

	public TraceStoreApp(){
		
		//Force eager initialization of the middleware
		TRACEStore.getTRACEStore();
		TRACESecurityManager.getManager();
		
		 // Register resources and providers using package-scanning.
        packages("org.trace.store.services");
 
        // Register my custom provider - not needed if it's in my.package.
        //register(AuthenticationFilter.class); TODO: descomentar
        //register(AuthorizationFilter.class);  TODO: descomentar
        
        // Register an instance of LoggingFilter.
        //register(new LoggingFilter(LOGGER, true));
 
        // Enable Tracing support.
        property(ServerProperties.TRACING, "ALL");
	}
	
}
