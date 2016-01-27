package org.trace.store.middleware.drivers.exceptions;

public class UnknownUserException extends UnableToPerformOperation {

	
	private static final long serialVersionUID = -2473393839457878185L;

	public UnknownUserException(String user){
		super("There is no "+user+" user registered.");
	}
	
}
