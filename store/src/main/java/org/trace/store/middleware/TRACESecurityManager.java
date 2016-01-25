package org.trace.store.middleware;

public class TRACESecurityManager {

	private static TRACESecurityManager MANAGER = new TRACESecurityManager();

	private TRACESecurityManager(){}

	public static TRACESecurityManager getManager(){return MANAGER ;}

}
