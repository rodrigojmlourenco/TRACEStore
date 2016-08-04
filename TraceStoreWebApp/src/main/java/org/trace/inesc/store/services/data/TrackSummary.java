package org.trace.inesc.store.services.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;

@XmlRootElement
public class TrackSummary {

	/** A string session that uniquely identifies the recorded track */
	@XmlElement(defaultValue="") private String session = "";
	
	/** Timestamp marking when the route started */
	private long startedAt;
	
	/** Timestamp marking when the route ended */
	private long endedAt;
	
	/** Duration of the route in seconds */
	private int elapsedTime;
	
	/** Distance traveled in meters */
	private double elapsedDistance;
	/*          elapsedDistance*/
	
	/** Average measured speed in meters per second */
	private float avgSpeed;
	
	/** Top measured speed in meters per second */
	private float topSpeed;
	
	/** Total amount of traced location points */
	private int points;
	
	/** Main modality registered */
	private int modality;
	
	public TrackSummary(){}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public long getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(long endedAt) {
		this.endedAt = endedAt;
	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(int elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public double getElapsedDistance() {
		return elapsedDistance;
	}

	public void setElapsedDistance(double elapsedDistance) {
		this.elapsedDistance = elapsedDistance;
	}

	public float getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public float getTopSpeed() {
		return topSpeed;
	}

	public void setTopSpeed(float topSpeed) {
		this.topSpeed = topSpeed;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getModality() {
		return modality;
	}

	public void setModality(int modality) {
		this.modality = modality;
	}
	
	public JsonObject toJson(){
		JsonObject jTrackSummary = new JsonObject();
		jTrackSummary.addProperty(Attributes.session, getSession());
		jTrackSummary.addProperty(Attributes.startedAt, getStartedAt());
		jTrackSummary.addProperty(Attributes.endedAt, getEndedAt());
		jTrackSummary.addProperty(Attributes.elapsedTime, getElapsedTime());
		jTrackSummary.addProperty(Attributes.elapsedDistance, getElapsedDistance());
		jTrackSummary.addProperty(Attributes.avgSpeed, getAvgSpeed());
		jTrackSummary.addProperty(Attributes.topSpeed, getTopSpeed());
		jTrackSummary.addProperty(Attributes.points, getPoints());
		jTrackSummary.addProperty(Attributes.modality, getModality());
		//jTrackSummary.addProperty(Attributes.from, getFrom()); TODO
		//jTrackSummary.addProperty(Attributes.to, getTo()); TODO
		
		return jTrackSummary;
	}
	
	@Override
	public String toString() {
		return toJson().toString();
	}
	
	public interface Attributes {
		String session = "session";
		String startedAt = "startedAt";
		String endedAt = "endedAt";
		String elapsedTime = "elapsedTime";
		String elapsedDistance = "elapsedDistance";
		String avgSpeed = "avgSpeed";
		String topSpeed = "topSpeed";
		String points = "points";
		String modality = "modality";
		String from = "from";
		String to = "to";
	}
	
}
