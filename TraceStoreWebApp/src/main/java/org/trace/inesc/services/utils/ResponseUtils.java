package org.trace.inesc.services.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ResponseUtils {
	
	private static final Gson gson = new Gson();

	public static String generateError(int code, String message){
		JsonObject error = new JsonObject();
		error.addProperty("success", false);
		error.addProperty("code", code);
		error.addProperty("error", message);
		return gson.toJson(error);

	}
	
	public static String generateFailedResponse(int code, String message){
		JsonObject response = new JsonObject();
		response.addProperty("code", code);
		response.addProperty("success", false);
		response.addProperty("error", message);
		return gson.toJson(response);
	}
	
	public static String generateFailedResponse(String msg) {
		JsonObject response = new JsonObject();
		response.addProperty("success", false);
		response.addProperty("error", msg);
		return gson.toJson(response);
	}

	public static String generateSuccess(){
		JsonObject success = new JsonObject();
		success.addProperty("success", true);
		return gson.toJson(success);
	}
	
	public static String generateSuccessResponse() {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		return gson.toJson(response);
	}

	public static String generateSuccessResponse(String payload) {
		JsonObject response = new JsonObject();
		response.addProperty("success", true);
		response.addProperty("token", payload); // TODO: isto deveria ser
												// enviado por email.
		return gson.toJson(response);
	}
}
