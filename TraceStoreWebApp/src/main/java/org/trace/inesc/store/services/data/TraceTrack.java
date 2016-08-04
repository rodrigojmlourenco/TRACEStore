package org.trace.inesc.store.services.data;

import java.util.Arrays;
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
public class TraceTrack {
	
	private Location[] track;
	
	public TraceTrack(){}
	
	public TraceTrack(Location[] track){
		this.track = track;
	}

	public Location[] getTrack() {
		return track;
	}

	public void setTrack(Location[] track) {
		this.track = track;
	}
	
	public List<Location> getTrackAsList(){
		return Arrays.asList(getTrack());
	}
	
	public JsonObject getJsonLocation(int index){
		
		if(index > track.length) return null;
		
		return track[index].getLocationAsJsonObject();
	}
	
	public Location getLocation(int index){
		if(index > track.length) return null;
		
		return track[index];
	}
	
	public int getTrackSize(){
		return track.length;
	}
}
