package org.trace.inesc.store.services.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@XmlRootElement
public class PrivacyPolicies {

	private String policies;
	
	public PrivacyPolicies(){}
	
	public PrivacyPolicies(String policies){
		this.policies = policies;
	}

	public String getPolicies() {
		return policies;
	}

	public void setPolicies(String policies) {
		this.policies = policies;
	}
	
	public JsonObject getPoliciesAsJson(){
		JsonParser parser = new JsonParser();
		return (JsonObject)parser.parse(getPolicies());
	}
}
