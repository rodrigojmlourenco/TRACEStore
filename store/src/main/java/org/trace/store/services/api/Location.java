package org.trace.store.services.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Location {
	
	private long timestamp;
	
	private double latitude, longitude;
	
	private String attributes;
	
	public Location(){}
	
	public Location(double latitude, double longitude, long timestamp){
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = timestamp;
		this.attributes = "";
	}
	
	public Location(double latitude, double longitude, long timestamp, String attributes){
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = timestamp;
		this.attributes = attributes;
	}

	public String getLocationJSON() {
		return getLocationAsJsonObject().toString();
	}

	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public JsonObject getLocationAsJsonObject(){
		
		JsonParser parser = new JsonParser();
		
		JsonObject location = new JsonObject();
		location.addProperty("latitude", latitude);
		location.addProperty("longitude", longitude);
		location.addProperty("timestamp", timestamp);
		location.add("attributes", parser.parse(attributes));
		return location;
	}
	
	@Override
	public String toString() {
		return getLocationJSON().toString();
	}
}
