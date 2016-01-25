package org.trace.store.middleware;

public class TRACEStoreManager {

	private static TRACEStoreManager MANAGER = new TRACEStoreManager();
	
	private TRACEStoreManager(){}
	
	public static TRACEStoreManager getManager(){
		return MANAGER;
	}
}
