package org.trace.store.middleware.drivers.exceptions;

public class UnableToUnregisterUserException extends Exception {

	private String message;
	
	public UnableToUnregisterUserException(String identifier, String cause){
		this.message = "Unable to unregister user "+identifier+", because: "+cause;
	}
}
