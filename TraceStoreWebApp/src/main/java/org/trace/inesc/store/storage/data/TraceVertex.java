package org.trace.inesc.store.storage.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TraceVertex {

	private String _name;
	private double _longitude; //Longitude
	private double _latitude; //Latitude
	private String _beaconID;
	private String _type;
	private Map<String,Object> _attributes;
	private Date _date;
	
	public TraceVertex(String name, double latitude, double longitude){
		_name = name;
		_latitude = latitude;
		_longitude = longitude;
		_type = "location";
		_attributes = new HashMap<String, Object>();
		_date = new Date();
	}
	
	public TraceVertex(String name, double latitude, double longitude, Map<String,Object> attributes){
		_name = name;
		_latitude = latitude;
		_longitude = longitude;
		_type = "location";
		_attributes = attributes;
		_date = new Date();
	}
	
	public TraceVertex(double latitude, double longitude){
		_name = "unset";
		_latitude = latitude;
		_longitude = longitude;
		_type = "location";
		_attributes = new HashMap<String, Object>();
		_date = new Date();
	}
	
	public TraceVertex(double latitude, double longitude, Map<String,Object> attributes){
		_name = "unset";
		_latitude = latitude;
		_longitude = longitude;
		_type = "location";
		_attributes = attributes;
		_date = new Date();
	}
	
	public TraceVertex(String name, String beaconID){
		_name = name;
		_beaconID = beaconID;
		_type = "beacon";
		_attributes = new HashMap<String, Object>();
		_date = new Date();
	}
	
	public TraceVertex(String name, String beaconID, Map<String,Object> attributes){
		_name = name;
		_beaconID = beaconID;
		_type = "beacon";
		_attributes = attributes;
		_date = new Date();
	}
	
	public TraceVertex(String beaconID){
		_name = "unset";
		_beaconID = beaconID;
		_type = "beacon";
		_attributes = new HashMap<String, Object>();
		_date = new Date();
	}
	
	public TraceVertex(String beaconID, Map<String,Object> attributes){
		_name = "unset";
		_beaconID = beaconID;
		_type = "beacon";
		_attributes = attributes;
		_date = new Date();
	}
	
	public void setAttributes(Map<String,Object> attributes){
		_attributes = attributes;
	}
	
	public Map<String,Object> getAttributes(){
		return _attributes;
	}
	
	public boolean hasAttributes(){
		return _attributes.isEmpty();
	}

	public String getName() {
		return _name;
	}

	public double getLongitude(){
		return _longitude;
	}

	public double getLatitude(){
		return _latitude;
	}

	public String getBeaconID() {
		return _beaconID;
	}

	public void setBeaconID(String beaconID) {
		_beaconID = beaconID;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}
	
	public void setDate(Date date){
		_date = date;
	}
	
	public Date getDate(){
		return _date;
	}
	
	@Override 
	public boolean equals(Object o){
		TraceVertex v = (TraceVertex) o;
		return _name.equals(v.getName());
	}
	
	@Override
	public String toString(){
		String vertexString = "";
		
		if(_type == "location"){
			vertexString = "Location name:" + _name + " - coords: " + _latitude + "," + _longitude;
		}else{
			vertexString = "Beacon name:" + _name + " - beaconID: " + _beaconID;
		}
		return vertexString;
	}
}