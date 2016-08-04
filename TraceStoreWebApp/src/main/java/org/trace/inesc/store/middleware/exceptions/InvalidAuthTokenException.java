package org.trace.inesc.store.middleware.exceptions;

public class InvalidAuthTokenException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public InvalidAuthTokenException(){
		this.message = "The provided token is not valid";
	}
	
	public InvalidAuthTokenException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}

}
