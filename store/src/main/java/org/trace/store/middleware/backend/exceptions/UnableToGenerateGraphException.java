package org.trace.store.middleware.backend.exceptions;

public class UnableToGenerateGraphException extends Exception {
	
	private static final long serialVersionUID = 1746891842857543933L;
	
	String message;
	
	public UnableToGenerateGraphException(String cause){
		message = "Unable to generate graph because: "+cause;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
