package org.trace.store;

import javax.ws.rs.ApplicationPath;

import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.trace.store.filters.AuthenticationFilter;
import org.trace.store.filters.CORSResponseFilter;
import org.trace.store.middleware.TRACESecurityManager;
import org.trace.store.middleware.TRACEStore;

@ApplicationPath("/")
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
        
        PropertyConfigurator.configure(System.getenv("LOG4J_CONFIG"));
	}
	
}
