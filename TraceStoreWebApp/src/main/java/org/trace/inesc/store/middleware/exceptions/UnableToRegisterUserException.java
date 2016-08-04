package org.trace.inesc.store.middleware.exceptions;

public class UnableToRegisterUserException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public UnableToRegisterUserException(){
		this.message = "Unable to register user";
	}
	
	public UnableToRegisterUserException(String cause){
		this.message = cause; 
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
