package org.trace.store.services.api.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TraceReward {

	private int identifier;
	private String condition;
	private String reward;
	
	public TraceReward(){}
	
	public TraceReward(int identifier, String condition, String reward){
		this.identifier = identifier;
		this.condition = condition;
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
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public JsonObject toJson(){
		
		JsonObject json = new JsonObject();
		json.addProperty("identifier", getIdentifier());
		json.addProperty("conditions", condition);
		json.addProperty("reward", getReward());
		return json;
	}
	
	public double getMinimumDistance(){
		JsonObject c = this.toJson().getAsJsonObject("conditions");
		return c.has("distance") ? 0 : c.get("distance").getAsDouble();
	}
	
	public static void main(String[] args){
		TraceReward r = new TraceReward(1, "{distance : 10 }" , "Um pastel de nata");
		System.out.println(r.getMinimumDistance());
	}
	
}
