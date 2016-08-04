package org.trace.inesc.store.services.data;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ShopDetailed {

	private int shopId;
	private String name, branding;
	private double latitude, longitude;
	private String avatarURL, mapURL;

	// Rewards List
	private List<Reward> rewards;

	public ShopDetailed() {
		this.rewards = new ArrayList<Reward>();
	}

	public ShopDetailed(int shopId, String name, String branding, double latitude, double longitude) {
		this.shopId = shopId;
		this.rewards = new ArrayList<Reward>();
		this.name = name;
		this.branding = branding;
		this.latitude = latitude;
		this.longitude = longitude;
		this.avatarURL = "";
		this.mapURL = "";
	}

	public ShopDetailed(int shopId, String name, String branding, double latitude, double longitude, String avatarURL,
			String mapURL) {
		this.shopId = shopId;
		this.rewards = new ArrayList<Reward>();
		this.name = name;
		this.branding = branding;
		this.latitude = latitude;
		this.longitude = longitude;
		this.avatarURL = avatarURL;
		this.mapURL = mapURL;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranding() {
		return branding;
	}

	public void setBranding(String branding) {
		this.branding = branding;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getMapURL() {
		return mapURL;
	}

	public void setMapURL(String mapURL) {
		this.mapURL = mapURL;
	}

	public void addReward(Reward reward) {
		this.rewards.add(reward);
	}

	public JsonObject toJson() {

		JsonArray jsonRewardsList = new JsonArray();
		JsonObject jsonShopDetailed = new JsonObject();

		for (Reward r : this.rewards)
			jsonRewardsList.add(r.toJson());

		jsonShopDetailed.addProperty("id", shopId);
		jsonShopDetailed.addProperty("name", name);
		jsonShopDetailed.addProperty("branding", branding);
		jsonShopDetailed.addProperty("latitude", latitude);
		jsonShopDetailed.addProperty("longitude", longitude);
		jsonShopDetailed.addProperty("avatarURL", avatarURL);
		jsonShopDetailed.addProperty("mapURL", mapURL);
		jsonShopDetailed.add("rewards", jsonRewardsList);

		return jsonShopDetailed;
	}

	@Override
	public String toString() {
		return this.toJson().toString();
	}
}
