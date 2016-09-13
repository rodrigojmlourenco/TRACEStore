package org.trace.store.services.api.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TraceReward {

	private int identifier, shopId;
	private String condition, reward, type, shopName;
	
	public TraceReward(){}
	
	public TraceReward(int identifier, String condition, String reward){
		this.identifier = identifier;
		this.condition = condition;
		this.reward = reward;
	}
	
	public TraceReward(int identifier, String condition, String reward, String type){
		this.identifier = identifier;
		this.condition = condition;
		this.reward = reward;
		this.type = type;
	}
	
	public TraceReward(int identifier, String condition, String reward, String type, int shopId, String shopName){
		this.identifier = identifier;
		this.condition = condition;
		this.reward = reward;
		this.type = type;
		this.shopId = shopId;
		this.shopName = shopName;
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
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public JsonObject toJson(){
		JsonParser parser = new JsonParser();
		JsonObject json = new JsonObject();
		json.addProperty("identifier", getIdentifier());
		json.add("conditions", parser.parse(getCondition()));
		json.addProperty("reward", getReward());
		json.addProperty("type", getType());
		json.addProperty("shopId", getShopId());
		json.addProperty("shopName", getShopName());
		return json;
	}

	public double getMinimumDistance(){ //TODO: temporary
		JsonObject c = this.toJson().getAsJsonObject("conditions");
		return c.has("distance") ? c.get("distance").getAsDouble() : 0;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}	
}
