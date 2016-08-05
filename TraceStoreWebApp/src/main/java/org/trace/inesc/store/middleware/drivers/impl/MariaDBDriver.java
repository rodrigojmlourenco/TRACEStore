package org.trace.inesc.store.middleware.drivers.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MariaDBDriver {
	
	//Logging
	private Logger log = Logger.getLogger(MariaDBDriver.class);
	
	private Connection conn;

	private static MariaDBDriver DRIVER = new MariaDBDriver();
	
	private String mUser, mPassword, mDatabase;
	
	private MariaDBDriver(){
		String user="error", password="error", database="error";

		
		String configFile = System.getenv("TRACE_CONFIG");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document config = dBuilder.parse(configFile);

			config.getDocumentElement().normalize();

			NodeList configParams = config.getElementsByTagName("repository").item(0).getChildNodes();

			Node aux;
			for(int i=0; i<configParams.getLength(); i++){
				aux = configParams.item(i);

				if(aux.getNodeType() == Node.ELEMENT_NODE){
					switch (aux.getNodeName()) {
					case "user":
						user = ((Element)aux).getAttribute("val");
						break;
					case "password":
						password = ((Element)aux).getAttribute("val");
					case "database":
						database = ((Element)aux).getAttribute("val");
					default:
						break;
					}
				}
			}

			try {
				Class.forName("org.mariadb.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+database, user, password);
			
			mUser = user;
			mPassword = password;
			mDatabase = database;
			
			scheduleCleanerTask();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private Connection getConnection(){ return conn; }
	
	/**
	 * Checks whether the connection to the JDBC is still open and valid.
	 * 
	 * @return True if the connection is valid, false otherwise.
	 */
	private boolean stillValidConnection(){
		try {
			return conn.isValid(0);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Closes the current connection, if possible, and initiates a new one.
	 */
	private void attemptReconnect(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+mDatabase, mUser, mPassword);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void reconnectForInvalidConnection(){
		
		log.debug("[1] Checking if the connection is valid...");
		
		if(!stillValidConnection()){
			log.debug("[2] The connection is invalid, attempting reconnect...");
			attemptReconnect();
		}
	}
	
	protected static void forceReconnectIfNecessary(){
		DRIVER.reconnectForInvalidConnection();
	}
	
	public static Connection getMariaConnection(){
		return DRIVER.getConnection();
	}
	
	/* Async Work
	 * Async Work
	 * Async Work 
	 **************************************************************************
	 * The connection with the MySQL/MariaDB driver is susceptible to two
	 * timeouts ( wait and interactive ). If no requests are performed during
	 * these periods, the connection is terminated.
	 * 
	 * To assure that this does not happen, a KeepAliveTask is scheduled to
	 * to keep this connection alive.
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************
	 */
	private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);
	
	private void scheduleCleanerTask(){
		scheduledService.scheduleAtFixedRate(
				new KeepAliveTask(),
				KeepAliveTask.TIMEOUT,
				KeepAliveTask.TIMEOUT, TimeUnit.SECONDS);
	}

	/**
	 * Performs a simple request to the existing MariaDBDriver Connection.
	 */
	private class KeepAliveTask implements Runnable {
		
		protected final static int TIMEOUT = 28800 - (2*60*60); // 8h - 2h, where 8h is the default mysql timeout.

		@Override
		public void run() {
			
			log.info("Keeping the connection alive...");
			
			Connection conn = MariaDBDriver.getMariaConnection();
			
			try{
				Statement stmt = conn.createStatement();
				stmt.execute("SELECT 1");
				stmt.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
	}
	
}
