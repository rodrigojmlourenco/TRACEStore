package org.trace.store.middleware.drivers.exceptions;

public class InvalidUsernameException extends UserRegistryException {

	private static final long serialVersionUID = 600479234591545467L;
	
	private String message;
	
	public InvalidUsernameException(String username){
		this.message = username+" is not a valid username.";
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
