package org.trace.store.middleware.drivers.exceptions;

public class SessionNotFoundException extends Exception {

	private static final long serialVersionUID = -1542311134300232657L;
	
	private String message;
	
	public SessionNotFoundException(String session){
		message = session + " not found.";
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
