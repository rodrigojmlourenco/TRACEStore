package org.trace.store.middleware;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trace.store.middleware.drivers.SessionDriver;
import org.trace.store.middleware.drivers.UserDriver;
import org.trace.store.middleware.drivers.exceptions.InvalidIdentifierException;
import org.trace.store.middleware.drivers.exceptions.UnableToPerformOperation;
import org.trace.store.middleware.drivers.impl.SessionDriverImpl;
import org.trace.store.middleware.drivers.impl.UserDriverImpl;
import org.trace.store.middleware.exceptions.SecretKeyNotFoundException;
import org.trace.store.services.api.data.Session;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TRACESecurityManager{

	public final static int TOKEN_MAX_TRIES = 10000;
	public final static int TOKEN_TTL		= 3600000; //1hour
	public final static String TOKEN_ISSUER = "org.trace";
	
	private Logger LOG = LoggerFactory.getLogger("TRACESecurityManager"); 
	
	private static TRACESecurityManager MANAGER = new TRACESecurityManager();
	
	//Drivers
	private final UserDriver uDriver = UserDriverImpl.getDriver();
	private final SessionDriver sDriver = SessionDriverImpl.getDriver();
	
	
	//Support Data Structures
	private ConcurrentHashMap<String, Date>	expirationDates;
	private ConcurrentHashMap<String, String> authenticationTokens;
	
	//<user,jti>
	private ConcurrentHashMap<String, String> userTokens;
	
	private final String SECRET;
	private final String SECRET_FILE = System.getenv("TRACE_DIR")+"/.secret/key";
	
	private String loadSecretFromFile() throws IOException{
		File key = new File(SECRET_FILE);
		return new String(Files.readAllBytes(key.toPath()));
			
	}
	
	private TRACESecurityManager(){
		userTokens			= new ConcurrentHashMap<>();
		authenticationTokens= new ConcurrentHashMap<>();
		expirationDates		= new ConcurrentHashMap<>();
		
		try {
			SECRET =loadSecretFromFile();
		} catch (IOException e) {
			throw new SecretKeyNotFoundException();
		}
		
		scheduleCleanerTask();

	}

	public static TRACESecurityManager getManager(){return MANAGER ;}

	public String login(String identifier, String password){
		
		return "";
	}
	
	public void logout(String authToken){
		
	}
	
	public void validateAuthToken(String authToken){
		
	}
	
	public Session generateSessionPseudonym(){
		return null;
	}

	
	public boolean isActiveUser(String identifier){
		
		int userID;
		try {
			userID = uDriver.getUserID(identifier);
		} catch (UnableToPerformOperation e) {
			LOG.error("Failed to verify if "+identifier+" is active because, "+e.getMessage());
			return false;
		}
		
		return uDriver.isPendingActivation(userID);
	}
	
	public boolean validateUser(String username, String password){
		
		try {
			return uDriver.isValidPassword(username, password);
		} catch (InvalidIdentifierException | UnableToPerformOperation e) {
			e.printStackTrace();
			LOG.error("Failed to validate "+username+" because, "+e.getMessage());
		}
		
		return false;
	}
	
	/*
	 ****************************************************
	 ****************************************************
	 * JWT Management and Generation					*
	 ****************************************************
	 ****************************************************
	 */
	
	private String generateSecureJTI(){
		byte[] jti = new byte[64];
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(jti);
		return new String(jti);
	}
	
	private void cleanExpiredTokens(){
		
		Date now = new Date(System.currentTimeMillis());
		
		for(String id : authenticationTokens.keySet()){
			try {
				if(validateAndExtractDate(authenticationTokens.get(id)).after(now))
					authenticationTokens.remove(id);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Validates the token and returns the subject's identity.
	 * 
	 * @param token The JWT token
	 * 
	 * @return The token's subject
	 * @throws Exception If the token is invalid or expired.
	 */
	public String validateAndExtractSubject(String token) throws Exception{
		Claims claims = Jwts.parser()         
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
				.parseClaimsJws(token).getBody();

		return claims.getSubject();
	}
	
	private Date validateAndExtractDate(String token) throws Exception{
		Claims claims = Jwts.parser()         
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
				.parseClaimsJws(token).getBody();

		return claims.getExpiration();
	}
	
	private String validateAndExtractJTI(String token) throws Exception{
		Claims claims = Jwts.parser()         
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
				.parseClaimsJws(token).getBody();

		return claims.getId();
	}
	
	
	private String createJWT(String id, String issuer, String subject, Date expiration, String payload){
		//The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		Date now = new Date(System.currentTimeMillis());
		
		//We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder()
				.setId(id)
				.setIssuedAt(now)
				.setSubject(subject)
				.setIssuer(issuer)
				.setExpiration(expiration)
				.setPayload(payload)
				.signWith(signatureAlgorithm, signingKey);

		//Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	
	public String issueToken(String username, String session){
		
		int tries = 0;
		Date expiration;
		String jwt, jti;
		
		//Step 1 - Generate random JTI
		// According to the RFC there should not be repeated JTIs
		do{
			jti = generateSecureJTI();
			
			if(tries >= TOKEN_MAX_TRIES){
				cleanExpiredTokens();
				tries = 0;
			}else
				tries++;
			
		}while(authenticationTokens.containsKey(jti));

		
		//Step 2 - Generate the token
		expiration = new Date(System.currentTimeMillis()+TOKEN_TTL);
		
		jwt = createJWT(jti, TOKEN_ISSUER, username, expiration, session);
		authenticationTokens.put(jti, jwt);
		expirationDates.put(jti, expiration);
		userTokens.put(username, jti);
		
		
		//Step 3 - Store the session in Graph and SQL

		return jwt;
	}
	
	//Asynchronous-Work
	private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);
	
	private void scheduleCleanerTask(){
		scheduledService.scheduleAtFixedRate(new ClearnerTask(), 1*60*60, 1*60*60, TimeUnit.SECONDS);
	}
	
	private class ClearnerTask implements Runnable{

		@Override
		public void run() {
			
			Date now = new Date(System.currentTimeMillis());
			
			for(String jti : expirationDates.keySet()){
				if(now.after(expirationDates.get(jti))){
					expirationDates.remove(jti);
					authenticationTokens.remove(jti);
				}
			}
			
			for(String user : userTokens.keySet()){
				if(!authenticationTokens.containsKey(userTokens.get(user)))
					userTokens.remove(user);
			}
		}
	}
	
	//Testing
	public static void main(String[] args){
		
	}
}
