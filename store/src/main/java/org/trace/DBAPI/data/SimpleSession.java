package org.trace.DBAPI.data;

import com.google.gson.JsonObject;

/**
 * Simple session that is characterized by its session identifier, and the timestamp of 
 * the date it was created.
 */
public class SimpleSession {

	private String sessionId;
	private long timestamp;
	
	public SimpleSession(){}
	
	public SimpleSession(String sessionId, long timestamp){
		this.sessionId = sessionId;
		this.timestamp = timestamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("session", sessionId);
		json.addProperty("date", timestamp);
		return json;
	}
	@Override
	public String toString() {
		return toJson().toString();
	}
	
}
