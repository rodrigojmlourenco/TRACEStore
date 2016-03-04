package org.trace.store.middleware.drivers.impl;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.binary.Base64;
import org.trace.store.filters.Role;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.EmailAlreadyRegisteredException;
import org.trace.store.middleware.drivers.exceptions.ExpiredTokenException;
import org.trace.store.middleware.drivers.exceptions.InvalidEmailException;
import org.trace.store.middleware.drivers.exceptions.InvalidIdentifierException;
import org.trace.store.middleware.drivers.exceptions.InvalidPasswordException;
import org.trace.store.middleware.drivers.exceptions.InvalidUsernameException;
import org.trace.store.middleware.drivers.exceptions.NoSuchTokenException;
import org.trace.store.middleware.drivers.exceptions.NonMatchingPasswordsException;
import org.trace.store.middleware.drivers.exceptions.PasswordReuseException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.exceptions.UnableToRegisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnableToUnregisterUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownSecurityRoleException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserException;
import org.trace.store.middleware.drivers.exceptions.UnknownUserIdentifierException;
import org.trace.store.middleware.drivers.exceptions.UserRegistryException;
import org.trace.store.middleware.drivers.exceptions.UsernameAlreadyRegisteredException;
import org.trace.store.middleware.drivers.utils.FormFieldValidator;
import org.trace.store.middleware.drivers.utils.SecurityRoleUtils;
import org.trace.store.middleware.drivers.utils.SecurityUtils;
import org.trace.store.services.api.PrivacyPolicies;

public class UserDriverImpl implements UserDriver{

	protected final int HASHING_ITERATIONS = 0;
	protected final int SALT_SIZE = 64;
	protected final int TOKEN_SIZE = 25;

	private Connection conn;

	private UserDriverImpl(){
		conn = MariaDBDriver.getMariaConnection();
	}

	private static UserDriverImpl DRIVER = new UserDriverImpl();


	public static UserDriver getDriver(){
		return DRIVER;
	}

	@Override
	public String registerUser(
			String username, String email,
			String pass1, String pass2,
			String name, String address, String phone, Role role) 
					throws UserRegistryException, NonMatchingPasswordsException,
					UnableToRegisterUserException, UnableToPerformOperation {

		int userId = -1;
		boolean success = false;
		Exception error = null; 

		// 1 - Validate the security of the provided fields.
		try {
			validateFields(username, email, pass1, pass2);
		} catch (SQLException e1) {
			throw new UnableToPerformOperation(e1.getMessage());
		}

		// 2 - Secure the password
		byte[] hashSalt;
		byte[] hashedPassword;

		try {
			hashSalt = SecurityUtils.generateSalt(SALT_SIZE);
			hashedPassword=  SecurityUtils.getSecureHash(HASHING_ITERATIONS, pass1, hashSalt);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new UnableToRegisterUserException();
		}

		// 3 - Create new user entry
		try {

			userId = insertUser(
					username,
					email,
					Base64.encodeBase64String(hashedPassword), 
					Base64.encodeBase64String(hashSalt));

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnableToRegisterUserException();
		} finally {
			if(userId == -1) throw new UnableToRegisterUserException();;
		}

		// 4 - Update the user's detailed information
		try {
			success = insertUserDetails(userId, name, address, phone);
		} catch (SQLException e) {
			error = e;
			success = false;
		}

		// 5 - Update the user's role
		if(success){
			try {
				success = updateUserRole(userId, role);
			} catch (SQLException e) {
				error = e;
				success = false;
			}
		}

		// 6 - Setup the activation token
		String token = "";
		if(success){

			try {
				token = SecurityUtils.generateSecureActivationToken(25);
				byte[] hashedToken = SecurityUtils.hashSHA1(token);
				success = insertValidationRequest(userId, Base64.encodeBase64String(hashedToken));
			} catch (NoSuchAlgorithmException e) {
				error = e;
				success = false;
			} catch (SQLException e) {
				error = e;
				success = false;
			}
		}

		if(!success){

			try{
				if(error != null)error.printStackTrace();
				deleteUser(userId);
				throw new UnableToRegisterUserException();
			}catch(SQLException e){
				throw new UnableToRegisterUserException();
			}
		}else
			return token;

	}

	@Override
	public boolean unregisterUser(String identifier, String password)
			throws UnableToUnregisterUserException, NonMatchingPasswordsException, UnknownUserException {

		try{

			int userID = getUserID(identifier);

			if(isValidPassword(identifier, password))
				return deleteUser(userID);
			else
				throw new NonMatchingPasswordsException();

		}catch(SQLException e){
			e.printStackTrace();
			throw new UnableToUnregisterUserException(identifier, e.getMessage());
		} catch (InvalidIdentifierException e) {
			throw new UnableToUnregisterUserException(identifier, e.getMessage());
		} catch (UnableToPerformOperation e) {
			throw new UnableToUnregisterUserException(identifier, e.getMessage());
		}
	}

	@Override
	public int getUserID(String identifier) throws UnableToPerformOperation, UnknownUserException {
		int UID;
		try{
			if(FormFieldValidator.isValidEmail(identifier))
				UID =  getUserIDfromEmail(identifier);
			else if(FormFieldValidator.isValidUsername(identifier))
				UID = getUserIDfromUsername(identifier);
			else
				throw new UnableToPerformOperation("Invalid identifier.");

			if(UID <= -1)
				throw new UnknownUserException(identifier);
			else
				return UID;

		}catch(SQLException e){
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean isPendingActivation(int userID) {
		boolean result;

		try{
			PreparedStatement statement = 
					conn.prepareStatement(
							"SELECT Id FROM activation WHERE Id = ?");

			statement.setLong(1, userID);
			ResultSet set = statement.executeQuery();
			result = set.next();

			statement.close();
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}

		return result;
	}

	
	

	@Override
	public boolean activateAccount(String token) throws ExpiredTokenException, UnableToPerformOperation {


		try {
			byte[] hashedToken = SecurityUtils.hashSHA1(token);
			String tokenHashAsString = Base64.encodeBase64String(hashedToken);

			if(isTokenExpired(tokenHashAsString))
				throw new ExpiredTokenException();

			int activationID = getTokenActivationID(tokenHashAsString);

			PreparedStatement statement =
					conn.prepareStatement("DELETE FROM activation WHERE Id=?");

			statement.setInt(1, activationID);
			int count = statement.executeUpdate();
			statement.close();

			return count > 0;


		} catch (NoSuchAlgorithmException e) {
			throw new UnableToPerformOperation(e.getMessage());
		} catch (SQLException e) {
			throw new UnableToPerformOperation(e.getMessage());
		} catch (NoSuchTokenException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}

	@Override
	public boolean changePassword(String username, String oldPass, String newPass, String confirmPass)
			throws NonMatchingPasswordsException, PasswordReuseException, UnknownUserIdentifierException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public String recoverPassword(String email) throws UnknownUserIdentifierException, UnableToPerformOperation {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public boolean resetPassword(String username, String token, String newPass, String confirmPass)
			throws ExpiredTokenException, UnknownUserIdentifierException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
	}


	private String getUserSalt(long userID) throws SQLException {

		String result;
		PreparedStatement statement = 
				conn.prepareStatement("SELECT salt FROM users WHERE Id = ?");

		statement.setLong(1, userID);
		ResultSet set = statement.executeQuery();
		

		if(set.next())
			result = set.getString(1);
		else
			result = null;
		
		statement.close();
		return result;
	}
	
	@Override
	public boolean isValidPassword(String identifier, String password)
			throws InvalidIdentifierException, UnableToPerformOperation, UnknownUserException {

		int userID;
		byte[] salt;

		try{
			
			userID = getUserID(identifier);
			salt = Base64.decodeBase64(getUserSalt(userID));

			String hashedPass = Base64.encodeBase64String(
					SecurityUtils.getSecureHash(HASHING_ITERATIONS, password, salt));


			PreparedStatement statement =
					conn.prepareStatement(
							"SELECT Id "
									+ "FROM users "
									+ "WHERE Id = ? AND Password = ?");

			statement.setLong(1, userID);
			statement.setString(2, hashedPass);

			ResultSet set = statement.executeQuery();
			statement.close();
			
			if(set.next()) 
				return true;
			else 
				return false;

		}catch(SQLException e){
			throw new UnableToPerformOperation(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new UnableToPerformOperation(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	@Override
	public boolean isValidRole(String identifier, Role role) throws UnableToPerformOperation, UnknownUserException {
		int userId;
		String dbRole;
		
		try {
			dbRole = SecurityRoleUtils.translateRole(role);
		} catch (UnknownSecurityRoleException e) {
			return false;
		}
		
		userId = getUserID(identifier);
		try{
		PreparedStatement statement = conn.prepareStatement(
				"SELECT "+dbRole+" "
				+"FROM user_roles "
				+ "WHERE Id = ?");
		
		statement.setInt(1, userId);
		ResultSet set = statement.executeQuery();
		statement.close();
		
		
		if(set.next())
			return set.getBoolean(1);
		else
			return false;
		}catch(SQLException e){
			throw new UnableToPerformOperation(e.getMessage());
		}
	}
	
	@Override
	public boolean setPrivacyPolicies(String identifier, PrivacyPolicies policies) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	/*
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	
	private boolean isUsernameRegistered(String username) throws SQLException{

		boolean exists;
		PreparedStatement statement = 
				conn.prepareStatement("SELECT * FROM users WHERE Username=?");

		statement.setString(1, username);
		ResultSet set = statement.executeQuery();
		exists = set.next();
		statement.close();

		return exists;
	}


	private boolean isEmailRegistered(String email) throws SQLException{

		boolean exists;
		PreparedStatement statement = 
				conn.prepareStatement("SELECT * FROM users WHERE Email=?");

		statement.setString(1, email);
		ResultSet set = statement.executeQuery();
		exists = set.next();
		statement.close();

		return exists;
	}

	private void validateFields(String username, String email, String pass1, String pass2) 
			throws InvalidEmailException, InvalidUsernameException, InvalidPasswordException, UsernameAlreadyRegisteredException, EmailAlreadyRegisteredException, NonMatchingPasswordsException, SQLException{

		if(!FormFieldValidator.isValidEmail(email))
			throw new InvalidEmailException(email);

		if(!FormFieldValidator.isValidUsername(username))
			throw new InvalidUsernameException(username);

		if(!FormFieldValidator.isValidPassword(pass1)) 
			throw new InvalidPasswordException();

		if(!FormFieldValidator.isValidPassword(pass2))
			throw new InvalidPasswordException();

		if(isUsernameRegistered(username))
			throw new UsernameAlreadyRegisteredException(username);

		if(isEmailRegistered(email))
			throw new EmailAlreadyRegisteredException(email);

		if(!pass1.equals(pass2))
			throw new NonMatchingPasswordsException();
	}

	
	private int insertUser(String username, String email, String passwordHash, String salt)
			throws SQLException{


		PreparedStatement statement = 
				conn.prepareStatement(
						"INSERT INTO users (Username, Email, Password, Salt) "+
								"VALUES (?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);


		statement.setString(1, username);
		statement.setString(2, email);
		statement.setString(3, passwordHash);
		statement.setString(4, salt);

		statement.executeUpdate();
		ResultSet keys = statement.getGeneratedKeys();
		statement.close();

		if(keys.next()) 
			return keys.getInt(1);
		else 
			return -1;

	}

	private boolean insertUserDetails(int userId, String name, String address, String phone) throws SQLException{

		PreparedStatement statement = 
				conn.prepareStatement(
						"INSERT INTO user_details (Id, Name, Address, Phone) "+
						"VALUES (?,?,?,?)");


		statement.setInt(1, userId);
		statement.setString(2, name);
		statement.setString(3, address);
		statement.setString(4, phone);

		int success = statement.executeUpdate();
		statement.close();

		return success == 1;
	}

	private boolean updateUserRole(int userId, Role role) throws SQLException{

		String dbRole;
		try {
			dbRole = SecurityRoleUtils.translateRole(role);
		} catch (UnknownSecurityRoleException e) {
			return false;
		}

		PreparedStatement statement = 
				conn.prepareStatement(
						"INSERT INTO user_roles (Id, "+dbRole+") "+
						"VALUES (?,?)");


		statement.setInt(1, userId);
		statement.setBoolean(2, true);
		int success = statement.executeUpdate();
		statement.close();

		return success == 1;
	}

	private boolean insertValidationRequest(int userId, String token) throws SQLException{
		PreparedStatement statement =
				conn.prepareStatement(
						"INSERT INTO activation (Id, Token, Expiration) "
								+ "VALUES (?,?,?)");

		statement.setLong(1, userId);
		statement.setString(2, token);
		statement.setDate(3, new Date(SecurityUtils.getExpirationDate(1).getTime()) );

		int count = statement.executeUpdate();
		statement.close();

		return count > 0;
	}

	private boolean deleteUser(long userID) throws SQLException{

		PreparedStatement statement = 
				conn.prepareStatement("DELETE FROM users WHERE Id=?");

		statement.setLong(1, userID);
		int count = statement.executeUpdate();
		statement.close();

		return count > 0;

	}

	private int getUserIDfromUsername(String username) throws SQLException{

		int result;
		PreparedStatement statement =
				conn.prepareStatement("SELECT Id FROM users WHERE username = ?");

		statement.setString(1, username);
		ResultSet set = statement.executeQuery();


		if(set.next()){
			result = set.getInt(1);
		}else 
			result = -1;

		statement.close();
		return result;
	}

	private int getUserIDfromEmail(String email) throws SQLException{

		int result;
		PreparedStatement statement =
				conn.prepareStatement("SELECT Id FROM users WHERE email = ?");

		statement.setString(0, email);
		ResultSet set = statement.executeQuery();


		if(set.next()){
			result = set.getInt("Id");
		}else 
			result = -1;

		statement.close();
		return result;
	}
	
	private boolean isTokenExpired(String token) throws NoSuchTokenException, SQLException  {

		boolean result;
		PreparedStatement statement = 
				conn.prepareStatement(
						"SELECT Id, Expiration FROM activation "
								+ "WHERE Token=?");

		statement.setString(1, token);
		ResultSet set = statement.executeQuery();
		statement.close();

		if(set.next()){
			long expires = set.getDate(2).getTime();
			result = (expires - (new java.util.Date()).getTime()) <= 0;
			return result;
		}else
			throw new NoSuchTokenException();
	}

	private int getTokenActivationID(String token) throws SQLException, NoSuchTokenException {

		int result;
		
		PreparedStatement statement = 
				conn.prepareStatement(
						"SELECT Id "
								+ "FROM activation "
								+ "WHERE Token = ?");

		statement.setString(1, token);
		ResultSet set = statement.executeQuery();


		if(set.next())
			result = set.getInt(1);
		else
			throw new NoSuchTokenException();


		statement.close();
		return result;
	}

	/*
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	
	protected void clearAll(){
		try {
			Statement statement = conn.createStatement();
			statement.executeQuery("DELETE FROM users");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args){
		UserDriverImpl d = (UserDriverImpl)UserDriverImpl.getDriver();

		try {
			
			int id;
			d.clearAll();
			String activation = d.registerUser("bonobo", "bonobo@somemail.com", "passWord123#", "passWord123#", "Bonobo da Silva", "Av Alves Redol n9", "+351919999999", Role.user);
			System.out.println(activation);
			d.activateAccount(activation);
			System.out.println(id=d.getUserID("bonobo"));
			
			if(!d.isPendingActivation(id) && d.isValidPassword("bonobo", "passWord123#"))
				System.out.println("YES IT IS CORRECT");
			else
				System.out.println("SOMETHING IS NOT RIGHT!");
			
		} catch (UserRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NonMatchingPasswordsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnableToRegisterUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnableToPerformOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExpiredTokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
