package org.trace.store.services.api.data;

import com.google.gson.JsonObject;

public class SimpleReward {

	private int identifier;
	private String reward;
	
	public SimpleReward(){}
	
	public SimpleReward(int identifier, String reward){
		this.identifier = identifier;
		this.reward = reward;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("identifier", getIdentifier());
		json.addProperty("reward", getReward());
		return json;
	}
	
}
