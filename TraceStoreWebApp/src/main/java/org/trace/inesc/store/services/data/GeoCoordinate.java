package org.trace.inesc.store.services.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GeoCoordinate {

	private double latitude, longitude;
	
	public GeoCoordinate(){}
	
	public GeoCoordinate(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude= longitude; 
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
	
	@Override
	public String toString() {
		
		JsonObject object = new JsonObject();
		object.addProperty("latitude", latitude);
		object.addProperty("longitude", longitude);
		
		return (new Gson()).toJson(object);
	}
}
