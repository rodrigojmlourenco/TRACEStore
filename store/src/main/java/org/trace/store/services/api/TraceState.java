package org.trace.store.services.api;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;

/**
 *  A track can be perceived as an ordered sequence of locations
 *  that are associated with a timestamp, and that may contain 
 *  other semantic information. This additional semantic information 
 *  may contain, for instance, means of transportation, velocity, among others.
 */
@XmlRootElement
public class TraceState {
	
	private String name;
	private Date timeStamp;
	
	public TraceState(){}
	
	public TraceState(String name, Date timeStamp){
		this.name = name;
		this.timeStamp = timeStamp;
	}

	public String getName() {
		return name;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}
}
