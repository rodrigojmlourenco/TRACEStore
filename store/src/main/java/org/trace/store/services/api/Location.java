package org.trace.store.services.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Location {
	private String locationJSON;
	private long timestamp;
	
	public Location(){}
	
	public Location(String location, long timestamp){
		this.locationJSON = location;
		this.timestamp = timestamp;
	}

	public String getLocationJSON() {
		return locationJSON;
	}

	public void setLocationJSON(String locationJSON) {
		this.locationJSON = locationJSON;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public JsonObject getLocationAsJsonObject(){
		JsonParser parser = new JsonParser();
		return (JsonObject) parser.parse(getLocationJSON());
	}
}
