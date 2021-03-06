package org.trace.store.services.api.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DistanceBasedRewardRequest {
	
	private double travelledDistance;
	private String reward;
	private int shopId;
	
	public DistanceBasedRewardRequest(){}
	
	public DistanceBasedRewardRequest(double distanceTravelled, String reward){
		this.travelledDistance = distanceTravelled;
		this.reward = reward;
	}

	public double getTravelledDistance() {
		return travelledDistance;
	}

	public void setTravelledDistance(double travelledDistance) {
		this.travelledDistance = travelledDistance;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
}
