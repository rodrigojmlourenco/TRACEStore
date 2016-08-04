package org.trace.inesc.store.services.data;

import com.google.gson.JsonObject;

public class Reward {
	
	private int rewardId;
	private String condition, reward;
	
	public Reward(){}

	public Reward(int id, String condition, String reward){
		this.rewardId = id;
		this.condition = condition;
		this.reward = reward;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}
	
	public JsonObject toJson(){
		JsonObject jsonReward = new JsonObject();
		jsonReward.addProperty("id", rewardId);
		jsonReward.addProperty("condition", condition);
		jsonReward.addProperty("reward", reward);
		return jsonReward;
	}
	
	@Override
	public String toString() {
		return this.toJson().toString();
	}
}
