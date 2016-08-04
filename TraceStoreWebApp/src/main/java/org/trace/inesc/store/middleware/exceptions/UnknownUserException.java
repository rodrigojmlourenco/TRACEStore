package org.trace.inesc.store.middleware.exceptions;

public class UnknownUserException extends Exception {

	
	private static final long serialVersionUID = -2473393839457878185L;

	public UnknownUserException(String user){
		super("There is no "+user+" user registered.");
	}
	
}
