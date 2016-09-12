package org.trace.store.services.api.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegisterShopRequest {
	
	private String name, branding, type;
	private double latitude,longitude;
	
	public RegisterShopRequest(){}
	
	public RegisterShopRequest(String name, String branding, double latitude, double longitude){
		this.name = name;
		this.branding = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public RegisterShopRequest(String name, String branding, double latitude, double longitude, String type){
		this.name = name;
		this.branding = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getBranding() {
		return branding;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBranding(String branding) {
		this.branding = branding;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
