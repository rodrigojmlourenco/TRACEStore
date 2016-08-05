package org.trace.inesc.services.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.trace.inesc.services.TRACEStoreService;
import org.trace.inesc.store.services.data.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RequestUtils {
	
	private static final Logger LOG = Logger.getLogger(TRACEStoreService.class);
	
	public static Map<String, Object> extractLocationAttributes(Location location) {

		HashMap<String, Object> map = new HashMap<>();
		try {

			JsonObject attributes = (JsonObject) location.getLocationAsJsonObject().get("attributes");

			for (Entry<String, JsonElement> attribute : attributes.entrySet())
				map.put(attribute.getKey(), attribute.getValue());
		} catch (ClassCastException e) {
			LOG.error("Unable to extract the attributes because, " + e.getMessage());
			return null;
		}

		return map;
	}

}
