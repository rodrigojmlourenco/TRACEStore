package org.trace.inesc.store.services.data;

import java.util.Arrays;
import java.util.Date;
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
public class TraceActivities {
	
	private Date[] timeStamps;
	private int[] still;
	private int[] unknown;
	private int[] tilting;
	private int[] foot;
	private int[] walking;
	private int[] running;
	private int[] cycling;
	private int[] vehicle;

	
	public TraceActivities(){}
	
	public TraceActivities(Date[] timeStamp, 
					int[] still,
					int[] unknown,
					int[] tilting,
					int[] foot,
					int[] walking,
					int[] running,
					int[] cycling,
					int[] vehicle){
		
		
		this.timeStamps = timeStamp;
		this.still = still;
		this.unknown = unknown;
		this.tilting = tilting;
		this.foot = foot;
		this.walking = walking;
		this.running = running;
		this.cycling = cycling;
		this.vehicle = vehicle;
	}

	public Date[] getTimeStamps() {
		return timeStamps;
	}

	public void setTimeStamps(Date[] timeStamps) {
		this.timeStamps = timeStamps;
	}
	
	public Date getTimeStamp(int index){
		if (index > timeStamps.length) return null;
		return timeStamps[index];
	}
	
	public int getStill(int index){
		if (index > still.length) return -1;
		return still[index];
	}
	
	public int getUnknown(int index){
		if (index > unknown.length) return -1;
		return unknown[index];
	}
	
	public int getTilting(int index){
		if (index > tilting.length) return -1;
		return tilting[index];
	}
	
	public int getFoot(int index){
		if (index > foot.length) return -1;
		return foot[index];
	}
	
	public int getWalking(int index){
		if (index > walking.length) return -1;
		return walking[index];
	}
	
	public int getRunning(int index){
		if (index > running.length) return -1;
		return running[index];
	}
	
	public int getCycling(int index){
		if (index > cycling.length) return -1;
		return cycling[index];
	}
	
	public int getVehicle(int index){
		if (index > vehicle.length) return -1;
		return vehicle[index];
	}
	
	public int getSize(){
		return timeStamps.length;
	}
}
