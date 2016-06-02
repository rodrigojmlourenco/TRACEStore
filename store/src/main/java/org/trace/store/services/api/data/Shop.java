package org.trace.store.services.api.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Shop {

	private int id,ownerId;
	private String name, branding;
	private double latitude, longitude;
	
	public Shop(){}
	
	public Shop(int id, int ownerId, String name, String branding, double latitude, double longitude){
		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.branding = branding;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranding() {
		return branding;
	}

	public void setBranding(String branding) {
		this.branding = branding;
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

	public JsonObject toJson(){
		JsonParser parser = new JsonParser();
		JsonObject json = new JsonObject();
		json.addProperty("id", getId());
		json.addProperty("ownerId", getOwnerId());
		json.addProperty("name", getName());
		json.addProperty("branding", getBranding());
		json.addProperty("latitude", getLatitude());
		json.addProperty("longitude", getLongitude());
		return json;
	}
	
}
