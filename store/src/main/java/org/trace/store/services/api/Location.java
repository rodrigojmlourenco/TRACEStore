package org.trace.store.services.api;

import com.google.gson.JsonObject;

public class Location {
	
	private long timestamp;
	
	private double latitude, longitude;
	
	public Location(){}
	
	public Location(double latitude, double longitude, long timestamp){
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = timestamp;
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
	
	public JsonObject getLocationAsJsonObject(){
		JsonObject location = new JsonObject();
		location.addProperty("latitude", latitude);
		location.addProperty("longitude", longitude);
		location.addProperty("timestamp", timestamp);
		
		return location;
	}
	
	@Override
	public String toString() {
		return getLocationJSON();
	}
}
