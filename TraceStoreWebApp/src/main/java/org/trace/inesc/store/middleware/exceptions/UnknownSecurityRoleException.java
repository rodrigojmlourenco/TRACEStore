package org.trace.inesc.store.middleware.exceptions;

import org.trace.inesc.filters.Role;

public class UnknownSecurityRoleException extends Exception {

	private static final long serialVersionUID = -7686238226394407742L;
	
	private String message;
	
	public UnknownSecurityRoleException(Role role){
		this.message = "Unknow security role "+role;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
}
