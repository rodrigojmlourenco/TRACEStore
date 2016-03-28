package org.trace.store.services.api;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@XmlRootElement
public class GeoLocation {

	private double latitude, longitude;
	long timestamp;
	private String attributes;
	
	
	public GeoLocation(){};
	
	public GeoLocation(double latitude, double longitude, long timestamp){
		this.latitude	= latitude;
		this.longitude	= longitude;
		this.timestamp	= timestamp;
		
		this.attributes = "";
	}
	
	public GeoLocation(double latitude, double longitude, long timestamp, String attributes){
		this.latitude	= latitude;
		this.longitude	= longitude;
		this.timestamp	= timestamp;
		
		this.attributes = attributes;
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
	
	public JsonObject getAttributesAsJson(){
		
		if(attributes.isEmpty()) return null;
		
		JsonParser parser = new JsonParser();
		return (JsonObject) parser.parse(getAttributes());
	}
	
	@Override
	public String toString() {
		return "{ latitude: "+latitude+", longitude: "+longitude+", timestamp: "+timestamp+"}";
	}
	
	
}
