package org.trace.store.services.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@XmlRootElement
public class BeaconLocation {

	private long timestamp;
	private int beaconId;
	@XmlElement(defaultValue="")private String attributes;
	
	public BeaconLocation(){}
	
	public BeaconLocation(int beaconId, long timestamp){
		this.beaconId = beaconId;
		this.timestamp = timestamp;
		
		this.attributes = "";
	}
	
	public BeaconLocation(int beaconId, long timestamp, String attributes){
		this.beaconId = beaconId;
		this.timestamp = timestamp;
		
		this.attributes = attributes;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getBeaconId() {
		return beaconId;
	}

	public void setBeaconId(int beaconId) {
		this.beaconId = beaconId;
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
		return (JsonObject)parser.parse(getAttributes());
	}
}
