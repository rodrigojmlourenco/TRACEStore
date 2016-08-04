package org.trace.inesc.store;

import javax.ws.rs.ApplicationPath;

import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.trace.inesc.filters.AuthenticationFilter;
import org.trace.inesc.filters.CORSResponseFilter;

@ApplicationPath("/")
public class TRACEStoreWebApp extends ResourceConfig {
	
	public TRACEStoreWebApp(){
		
		//Force eager initialization of the middleware
		//TRACEStore.getTRACEStore();
		//TRACESecurityManager.getManager();
		packages("org.trace.inesc.services");
		
		//Incomming Filters
		 // Register resources and providers using package-scanning.
 
        // Register my custom provider - not needed if it's in my.package.
        register(AuthenticationFilter.class); 
        //register(AuthorizationFilter.class);  
        
        //Outgoing Filters
        register(CORSResponseFilter.class);
        
        // Enable Tracing support.
        property(ServerProperties.TRACING, "ALL");
        
        PropertyConfigurator.configure(System.getenv("LOG4J_CONFIG"));
	}

}
