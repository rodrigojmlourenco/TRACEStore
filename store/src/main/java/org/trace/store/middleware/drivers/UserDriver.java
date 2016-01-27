package org.trace.store.middleware.drivers;


import org.trace.store.middleware.drivers.exceptions.ExpiredTokenException;
import org.trace.store.middleware.drivers.exceptions.InvalidIdentifierException;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.PasswordReuseException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnableToUnregisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserIdentifierException;
import org.trace.store.middleware.drivers.exceptions.UserRegistryException;
import org.trace.store.services.api.PrivacyPolicies;
import org.trace.store.services.security.Role;

public interface UserDriver {

	/**
	 * Creates a new user. This method must also be responsible for
	 * validating all the provided fields. If successful, the
	 * method will return the user's activation token, which
	 * must then be used to activate its account.
	 * 
	 * @param username The user's username.
	 * @param email The user's email address.
	 * @param pass1 The user's password.
	 * @param pass2 The user's password confirmation.
	 * 
	 * @return The activation token
	 * 
	 * @throws UserRegistryException If one of the fields is invalid.
	 * @throws NonMatchingPasswordsException If the passwords do not match.
	 * @throws UnableToRegisterUserException If the user creation was unsuccessful for some unforseeable event.
	 * @throws UnableToPerformOperation 
	 *  
	 */
	public String registerUser(String username, String email, String pass1, String pass2, String name, String address, String phone, Role role)
		throws UserRegistryException, NonMatchingPasswordsException, UnableToRegisterUserException, UnableToPerformOperation;
	
	
	/**
	 * Unregister a user, however, only if presents a correct password.
	 * 
	 * @param identifier The user's identifier, which may either be his username or email.
	 * @param password THe user's password.
	 * 
	 * @return True if the operation was successful. 
	 * @throws UnableToUnregisterUserException 
	 * @throws NonMatchingPasswordsException 
	 */
	public boolean unregisterUser(String identifier, String password) throws UnableToUnregisterUserException, NonMatchingPasswordsException;
	
	/**
	 * Fetches a given user's UID.
	 * 
	 * @param identifier The user's identification, which may either be his username or email.
	 * 
	 * @return The user's UID
	 * 
	 * @throws UnknownUserException There is no user registered with the provided identifier.
	 * @throws UnableToPerformOperation The operation was unsuccessful due to unforseeable reasons.
	 */
	public int getUserID(String identifier) throws UnknownUserException, UnableToPerformOperation;
	
	
	/**
	 * Checks if a given user account is still pending activation.
	 * 
	 * @param userID The user's UID
	 * 
	 * @return True if the account has not yet been activated, false otherwise.
	 */
	public boolean isPendingActivation(int userID);
	
		
	/**
	 * Activated a specific user account given the provided activation token.
	 * 
	 * @param token The activation token (cleartext)
	 * @return True if the operation was successful, false otherwise.
	 * 
	 * @throws ExpiredTokenException
	 * @throws UnableToPerformOperation 
	 */
	public boolean activateAccount(String token) throws ExpiredTokenException, UnableToPerformOperation;
	
	
	
	public boolean changePassword(String username, String oldPass, String newPass, String confirmPass) 
			throws NonMatchingPasswordsException, PasswordReuseException, UnknownUserIdentifierException;

	
	public String recoverPassword(String email) 
			throws UnknownUserIdentifierException, UnableToPerformOperation;
	
	
	public boolean resetPassword(String username, String token, String newPass, String confirmPass) 
			throws ExpiredTokenException, UnknownUserIdentifierException;
	
	
	public boolean isValidPassword(String identifier, String password) throws InvalidIdentifierException, UnableToPerformOperation;
	
	public boolean isValidRole(String identifier, Role role) throws UnableToPerformOperation;
	
	/**
	 * Allows users to set security and privacy policies about their data.
	 *  
	 * @param identifier The user's unique identifier, either its username or email.
	 * @param policies The privacy policies.
	 * 
	 * @return True if the privacy policies were successfully added, false otherwise.
	 */
	public boolean setPrivacyPolicies(String identifier, PrivacyPolicies policies);
}
