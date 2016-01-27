package org.trace.store.middleware.drivers.exceptions;

public class UnableToPerformOperation extends Exception {

	private static final long serialVersionUID = 2437681817307011175L;
	
	private String message;

	public UnableToPerformOperation(String message){
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}

