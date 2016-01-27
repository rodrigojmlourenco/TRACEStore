package org.trace.store.middleware.drivers.exceptions;

public class EmailAlreadyRegisteredException extends UserRegistryException {

	private static final long serialVersionUID = 879834044065336576L;
	
	private String message;
	
	public EmailAlreadyRegisteredException(String email){
		this.message = "The email "+email+" is already associated with another account.";
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
