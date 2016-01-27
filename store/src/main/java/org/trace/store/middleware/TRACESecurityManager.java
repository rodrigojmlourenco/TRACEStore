package org.trace.store.middleware;

import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.services.api.data.Session;

public class TRACESecurityManager{

	private static TRACESecurityManager MANAGER = new TRACESecurityManager();
	private final UserDriver driver = UserDriverImpl.getDriver();
	
	private TRACESecurityManager(){}

	public static TRACESecurityManager getManager(){return MANAGER ;}

	public String login(String identifier, String password){
		
		return "";
	}
	
	public void logout(String authToken){
		
	}
	
	public void validateAuthToken(String authToken){
		
	}
	
	public Session generateSessionPseudonym(){
		return null;
	}

}
