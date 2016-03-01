package org.trace.store.middleware.drivers.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MariaDBDriver {
	
	private Connection conn;

	private static MariaDBDriver DRIVER = new MariaDBDriver();
	
	private MariaDBDriver(){
		String user="error", password="error", database="error";

		String configFile = System.getenv("HOME")+"/trace/config.xml";

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


			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+database, user, password);

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
	
	
	protected static Connection getMariaConnection(){
		return DRIVER.getConnection();
	}
}
