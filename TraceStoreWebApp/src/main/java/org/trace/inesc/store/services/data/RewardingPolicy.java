package org.trace.inesc.store.services.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@XmlRootElement
public class RewardingPolicy {

	private String description;
	private String policies;
	
	public RewardingPolicy(){}
	
	public RewardingPolicy(String description, String policy){
		this.policies = policy;
	}

	public String getPolicy() {
		return policies;
	}

	public void setPolicy(String policy) {
		this.policies = policy;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPolicies() {
		return policies;
	}

	public void setPolicies(String policies) {
		this.policies = policies;
	}

	public JsonObject getPoliciesAsJsonObject(){
		JsonParser parser = new JsonParser();
		return (JsonObject)parser.parse(getPolicy());
	}
	
}
