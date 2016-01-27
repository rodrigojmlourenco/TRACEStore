package org.trace.store.middleware.drivers.exceptions;

public class InvalidEmailException extends UserRegistryException {

	private static final long serialVersionUID = 8337371689659144683L;
	
	private String message;
	
	public InvalidEmailException(String email){
		this.message = email + " is not a valid email.";
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
	
}
