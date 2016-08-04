package org.trace.inesc.store.middleware.drivers.utils;

import org.trace.inesc.filters.Role;
import org.trace.inesc.store.middleware.exceptions.UnknownSecurityRoleException;

public interface SecurityRoleUtils {

	public static String translateRole(Role role) throws UnknownSecurityRoleException{
		switch (role) {
		case user:
			return "User";
		case rewarder:
			return "Rewarder";
		case planner:
			return "UrbanPlanner";
		case admin:
			return "admin";
		default:
			throw new UnknownSecurityRoleException(role);
		}
	}
}
