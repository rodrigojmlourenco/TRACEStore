package org.trace.store.middleware.drivers.utils;

import org.trace.store.filters.Role;
import org.trace.store.middleware.drivers.exceptions.UnknownSecurityRoleException;

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
