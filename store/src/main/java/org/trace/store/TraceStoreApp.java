package org.trace.store;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.trace.store.filters.AuthenticationFilter;
import org.trace.store.filters.CORSResponseFilter;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.TRACEStore;

public class TraceStoreApp extends ResourceConfig{

	public TraceStoreApp(){
		
		//Force eager initialization of the middleware
		TRACEStore.getTRACEStore();
		TRACESecurityManager.getManager();
		packages("org.trace.store.services");
		
		//Incomming Filters
		 // Register resources and providers using package-scanning.
 
        // Register my custom provider - not needed if it's in my.package.
        register(AuthenticationFilter.class); 
        //register(AuthorizationFilter.class);  
        
        
        // Register an instance of LoggingFilter.
        //register(new LoggingFilter(LOGGER, true));
 
        //Outgoing Filters
        register(CORSResponseFilter.class);
        
        // Enable Tracing support.
        property(ServerProperties.TRACING, "ALL");
	}
	
}
