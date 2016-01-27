package org.trace.store.middleware.drivers.exceptions;

public class InvalidPasswordException extends UserRegistryException {

	private static final long serialVersionUID = -5398791651316072692L;
	
	private String message;
	
	public InvalidPasswordException(){
		this.message = "Invalid password. A password must contain numbers and letters, and at least one special character and one upper-case letter.";
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
