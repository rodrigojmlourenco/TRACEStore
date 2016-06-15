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
public class TraceStates {
	
	private String[] names;
	private Date[] timeStamps;
	
	public TraceStates(){
		names = null;
		timeStamps = null;
	}
	
	public TraceStates(String[] name, Date[] timeStamp){
		this.names = name;
		this.timeStamps = timeStamp;
	}

	public String[] getNames() {
		return names;
	}
	
	public Date[] getTimeStamps() {
		return timeStamps;
	}

	public void setNames(String[] names) {
		this.names = names;
	}
	
	public void setTimeStamps(Date[] timeStamps) {
		this.timeStamps = timeStamps;
	}
	
	public List<String> getNamesAsList(){
		return Arrays.asList(getNames());
	}
	
	public List<Date> getTimeStampsAsList(){
		return Arrays.asList(getTimeStamps());
	}
	
	public String getName(int index){
		if(index > names.length) return null;
		
		return names[index];
	}
	
	public Date getTimeStamp(int index){
		if (index > timeStamps.length) return null;
		
		return timeStamps[index];
	}
	
	public int getSize(){
		return names.length;
	}
	
	public boolean isEmpty(){
		return (names == null && timeStamps == null);
	}
}
