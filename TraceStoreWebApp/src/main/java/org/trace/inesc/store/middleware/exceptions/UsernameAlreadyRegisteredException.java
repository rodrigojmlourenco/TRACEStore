package org.trace.inesc.store.middleware.exceptions;

public class UsernameAlreadyRegisteredException extends UserRegistryException {

	private static final long serialVersionUID = 3022211025955660708L;
	
	private String message;
	
	public UsernameAlreadyRegisteredException(String username){
		this.message = "The username "+username+" is already registered.";
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}

}
