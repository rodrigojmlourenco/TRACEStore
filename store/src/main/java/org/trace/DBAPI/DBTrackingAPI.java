package org.trace.DBAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.trace.DBAPI.data.TraceVertex;
import org.trace.store.services.TRACEStoreService;

import io.netty.util.internal.SystemPropertyUtil;

public class DBTrackingAPI extends DBAPI{
	
	private final Logger LOG = Logger.getLogger(TRACEStoreService.class); 

	public DBTrackingAPI(Client client){
		super(client);
	}

	public boolean register(String username, String name, String address){
		boolean success = true;
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("username",username);
		params.put("name",name);
		params.put("address",address);

		//query
		results = query("graph.addVertex(label,'user','username', username, 'name', name, 'address', address)", params);

		return results != null;
	}

	private boolean login(String sessionID){
		
		if(sessionID == null){
			LOG.error("DBTrackingAPI.java - login: sessionID is null!");
			return false;
		}

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID",sessionID);
		params.put("date",new Date());

		//begin a session
		results = query("g.V().has('sessionID', sessionID).hasNext();"
				+ "", params);

		if(!results.get(0).getBoolean()){
			//begin a session
			results = query("S = graph.addVertex(label,'session','sessionID', sessionID, 'date', date);"
					+ "", params);
		}

		return results != null;
	}

	public boolean put(String sessionID, Date date, double latitude, double longitude){
		//return values list
		List<Result> results = null;

		//Make sure the user has the appointed session registered in the DB.
		login(sessionID);

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("longitude",longitude);
		params.put("latitude",latitude);
		params.put("sessionID",sessionID);
		params.put("date", date);
		//		params.put("latitudeGridID", TraceLocationMethods.getGridID(latitude));
		//		params.put("longitudeGridID", TraceLocationMethods.getGridID(longitude));

		//		List<String> adjacentLatitudeGridID = TraceLocationMethods.getAdjacentGridIDs(latitude);
		//		List<String> adjacentLongitudeGridID = TraceLocationMethods.getAdjacentGridIDs(longitude);

		//		String latitudeOR = "has('latitudeGridID','" + adjacentLatitudeGridID.get(0) + "'),"
		//				+ "has('latitudeGridID','" + adjacentLatitudeGridID.get(1) + "'),"
		//				+ "has('latitudeGridID','" + adjacentLatitudeGridID.get(2) + "')";

		//		String longitudeOR = "has('longitudeGridID','" + adjacentLongitudeGridID.get(0) + "'),"
		//				+ "has('longitudeGridID','" + adjacentLongitudeGridID.get(1) + "'),"
		//				+ "has('longitudeGridID','" + adjacentLongitudeGridID.get(2) + "')";

		//		String orString = latitudeOR + "," + longitudeOR;

		//		params.put("vertexID",tSession.getVertexID());

		//get id of the "finish" edge
		results = query("Lreturn = new ArrayList<>(); " //return array
				+ "E = g.V().has('sessionID',sessionID).inE().hasLabel('session').has('type','finish').id();" //ID of the "finish" edge
				+ "if(E.hasNext()){" //if there's a finish edge this means that this is not the 1st point being added to the session
				+		"Lreturn.add(true);" //Not the 1st point of session: index(0) - true (has finish edge)
				+ 		"Lreturn.add(E.next());" //index(1) - id
				+ "}else{"//otherwise if there's no "finish" edge this means we are the first point of the session
				+ 		"Lreturn.add(false);" //1st point of session: index(0) - false (does not have finish edge)
				+ 		"Lreturn.add('nothing')"//index(1) - nothing
				+ "};"

				//				+ "preL = g.V().or(" + orString + ").dedup();" 	
				//				+ "L = g.V().or(" + orString + ").has('location', geoWithin(Geoshape.circle(latitude, longitude, " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(latitude,longitude).getPoint())}.order().by(incr).select('a'); " //get location we want to put
				+ "L = g.V().has('location', geoWithin(Geoshape.circle(latitude, longitude, " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(latitude,longitude).getPoint())}.order().by(incr).select('a'); " //get location we want to put

				+ "if(L.hasNext()){" //is there any location on map with these coords?
				+ 		"Lreturn.add(true);" //yes: index(2) - true
				+ 		"Lreturn.add(L.next().value('vertexID'));" //location id: index(3) - vertexID
				+ "}else{"
				+ 		"Lreturn.add(false);" //no: index(2) - false
				+ "};"
				+ "Lreturn;"
				+ "",params);

		//		System.out.println("Size: " + results.size());

		if(results == null || results.size() == 0){
			return false;
		}

		//If point doesn't exist, add a new one, label: "unmapped_location"
		if(!results.get(2).getBoolean()){
			//params
			params.put("vertexID","" + latitude + "_" + longitude);
			params.put("latitude",latitude);
			params.put("longitude",longitude);
			//			params.put("latitudeGridID", TraceLocationMethods.getGridID(latitude));
			//			params.put("longitudeGridID", TraceLocationMethods.getGridID(longitude));

			//query
			List<Result> addPoint = null;
			addPoint = super.query("graph.addVertex(label,'unmapped_location',"
					+ "'vertexID', vertexID,"
					+ "'location', Geoshape.point(latitude,longitude));"
					//					+ "'latitudeGridID', latitudeGridID,"
					//					+ "'longitudeGridID', longitudeGridID)"
					+ "", params);

			//failed to add new vertex
			if(addPoint == null){
				return false;
			}
			//			params.remove("latitudeGridID");
			//			params.remove("longitudeGridID");
			params.remove("latitude");
			params.remove("longitude");
		}else{
			params.put("vertexID",results.get(3).getString());
		}

		if(!results.get(0).getBoolean()){ //first point of the session
			//connect session vertex to position vertex:
			//adds Edge(session - start) [Session --> Location]
			//and adds Edge(session - finish) [Location --> Session]

			results = query("S = g.V().has('sessionID',sessionID).next(); "
					+ "L = g.V().has('vertexID',vertexID).next();"
					+ "S.addEdge('session', L, 'type', 'start', 'sessionID', sessionID, 'date', date); "
					+ "L.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date) "
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}else{ //not first point of the session
			params.put("finishEdgeID",results.get(1).getString());

			results = query("S = g.V().has('sessionID',sessionID).next(); " //Session vertex
					+ "E = g.E(finishEdgeID).next(); " //"finish" edge
					+ "newL = g.V().has('vertexID',vertexID).next(); " //new Location
					+ "oldL = g.E(E).outV().next(); " //old last location
					+ "E.remove(); " //remove finish edge
					+ "oldL.addEdge('session', newL, 'type', 'trajectory', 'sessionID', sessionID, 'date', date); " //add edge from last location to new location
					+ "newL.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); " //add finish edge from new location to session
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}
		return true;
	}

	public boolean put(String sessionID, Date date, double latitude, double longitude, Map<String,Object> attributes){
		//return values list
		List<Result> results = null;

		//Make sure the user has the appointed session registered in the DB.
		login(sessionID);

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("latitude",latitude);
		params.put("longitude",longitude);
		params.put("sessionID",sessionID);
		params.put("date", date);
		//		params.put("latitudeGridID", TraceLocationMethods.getGridID(latitude));
		//		params.put("longitudeGridID", TraceLocationMethods.getGridID(longitude));

		//		List<String> adjacentLatitudeGridID = TraceLocationMethods.getAdjacentGridIDs(latitude);
		//		List<String> adjacentLongitudeGridID = TraceLocationMethods.getAdjacentGridIDs(longitude);

		//		String latitudeOR = "has('latitudeGridID','" + adjacentLatitudeGridID.get(0) + "'),"
		//				+ "has('latitudeGridID','" + adjacentLatitudeGridID.get(1) + "'),"
		//				+ "has('latitudeGridID','" + adjacentLatitudeGridID.get(2) + "')";

		//		String longitudeOR = "has('longitudeGridID','" + adjacentLongitudeGridID.get(0) + "'),"
		//				+ "has('longitudeGridID','" + adjacentLongitudeGridID.get(1) + "'),"
		//				+ "has('longitudeGridID','" + adjacentLongitudeGridID.get(2) + "')";

		//		String orString = latitudeOR + "," + longitudeOR;


		//get id of the "finish" edge
		results = query("Lreturn = new ArrayList<>(); " //return array
				+ "E = g.V().has('sessionID',sessionID).inE().hasLabel('session').has('type','finish').id();" //ID of the "finish" edge
				+ "if(E.hasNext()){" //if there's a finish edge this means that this is not the 1st point being added to the session
				+		"Lreturn.add(true);" //Not the 1st point of session: index(0) - true (has finish edge)
				+ 		"Lreturn.add(E.next());" //index(1) - id
				+ "}else{"//otherwise if there's no "finish" edge this means we are the first point of the session
				+ 		"Lreturn.add(false);" //1st point of session: index(0) - false (does not have finish edge)
				+ 		"Lreturn.add('nothing')"//index(1) - nothing
				+ "};"

				//						+ "L = g.V().or(" + orString + ").has('location', geoWithin(Geoshape.circle(latitude, longitude, " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(latitude,longitude).getPoint())}.order().by(incr).select('a'); " //get location we want to put
				+ "L = g.V().has('location', geoWithin(Geoshape.circle(latitude, longitude, " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(latitude,longitude).getPoint())}.order().by(incr).select('a'); " //get location we want to put

				+ "if(L.hasNext()){" //is there any location on map with these coords?
				+ 		"Lreturn.add(true);" //yes: index(2) - true
				+ 		"Lreturn.add(L.next().value('vertexID'));" //location id: index(3) - vertexID
				+ "}else{"
				+ 		"Lreturn.add(false);" //no: index(2) - false
				+ "};"
				+ "Lreturn;"
				+ "",params);

		//				System.out.println("Size: " + results.size());
		//		g.V().has('location', geoWithin(Geoshape.circle(10,10,2000))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(10,10).getPoint())}.order().by(incr).select('a').valueMap()

		if(results == null || results.size() == 0){
			return false;
		}

		//If point doesn't exist, add a new one, label: "unmapped_location"
		if(!results.get(2).getBoolean()){
			//params
			params.put("vertexID","" + longitude + "_" + latitude);
			//			params.put("latitudeGridID", TraceLocationMethods.getGridID(latitude));
			//			params.put("longitudeGridID", TraceLocationMethods.getGridID(longitude));

			//query
			List<Result> addPoint = null;
			addPoint = super.query("graph.addVertex(label,'unmapped_location',"
					+ "'vertexID', vertexID,"
					+ "'location', Geoshape.point(latitude,longitude));"
					//					+ "'latitudeGridID',latitudeGridID,"
					//					+ "'longitudeGridID',longitudeGridID)"
					+ "", params);

			//failed to add new vertex
			if(addPoint == null){
				return false;
			}
			//			params.remove("latitudeGridID");
			//			params.remove("longitudeGridID");
			params.remove("latitude");
			params.remove("longitude");
		}else{
			params.put("vertexID",results.get(3).getString());
		}


		//parse attributes
		String attributesString = "";
		Set<String> keys = attributes.keySet();
		if(attributes != null){
			for(String key : keys){
				params.put(key,attributes.get(key).toString());
				attributesString += ", '" +  key + "', " + key;
			}
		}
		attributesString += ");";

		//		System.out.println("newL.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date" + attributesString);


		if(!results.get(0).getBoolean()){ //first point of the session
			//connect session vertex to position vertex:
			//adds Edge(session - start) [Session --> Location]
			//and adds Edge(session - finish) [Location --> Session]

			results = query("S = g.V().has('sessionID',sessionID).next(); "
					+ "L = g.V().has('vertexID',vertexID).next();"
					+ "S.addEdge('session', L, 'type', 'start', 'sessionID', sessionID, 'date', date" + attributesString
					+ "L.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); "
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}else{ //not first point of the session
			params.put("finishEdgeID",results.get(1).getString());

			results = query("S = g.V().has('sessionID',sessionID).next(); " //Session vertex
					+ "E = g.E(finishEdgeID).next(); " //"finish" edge
					+ "newL = g.V().has('vertexID',vertexID).next(); " //new Location
					+ "oldL = g.E(E).outV().next(); " //old last location
					+ "E.remove(); " //remove finish edge
					+ "oldL.addEdge('session', newL, 'type', 'trajectory', 'sessionID', sessionID, 'date', date" + attributesString //add edge from last location to new location
					+ "newL.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); " //add finish edge from new location to session
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}
		return true;
	}


	//TODO implement this method
	public boolean put(String sessionID, Date date, String traceBeaconID){
		//return values list
		List<Result> results = null;

		//Make sure the user has the appointed session registered in the DB.
		login(sessionID);

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("traceBeaconID",traceBeaconID);
		params.put("sessionID",sessionID);
		params.put("date", date);
		//				params.put("vertexID",tSession.getVertexID());

		//get id of the "finish" edge
		results = query("Lreturn = new ArrayList<>(); " //return array
				+ "E = g.V().has('sessionID',sessionID).inE().hasLabel('session').has('type','finish').id();" //ID of the "finish" edge
				+ "if(E.hasNext()){" //if there's a finish edge this means that this is not the 1st point being added to the session
				+		"Lreturn.add(true);" //Not the 1st point of session: index(0) - true (has finish edge)
				+ 		"Lreturn.add(E.next());" //index(1) - id
				+ "}else{"//otherwise if there's no "finish" edge this means we are the first point of the session
				+ 		"Lreturn.add(false);" //1st point of session: index(0) - false (does not have finish edge)
				+ 		"Lreturn.add('nothing')"//index(1) - nothing
				+ "};"
				+ "L = g.V().has('traceBeaconID',traceBeaconID); " //get beacon we want to put
				+ "if(L.hasNext()){" //is there any beacon with such an ID?
				+ 		"Lreturn.add(true);" //yes: index(2) - true
				+ 		"Lreturn.add(L.next().value('vertexID'));" //beacon id: index(3) - vertexID
				+ "}else{"
				+ 		"Lreturn.add(false);" //no: index(2) - false
				+ "};"
				+ "Lreturn;"
				+ "",params);

		//				System.out.println("Size: " + results.size());

		if(results == null || results.size() == 0){
			return false;
		}

		//If point doesn't exist, add a new one, label: "unregistered_beacon"
		if(!results.get(2).getBoolean()){
			//params
			params.put("vertexID","" + sessionID + "_" + traceBeaconID);

			//query
			List<Result> addPoint = null;
			addPoint = super.query("graph.addVertex(label,'unregistered_beacon','vertexID', vertexID, 'traceBeaconID', traceBeaconID)", params);

			//failed to add new vertex
			if(addPoint == null){
				return false;
			}
		}else{
			params.put("vertexID",results.get(3).getString());
		}

		if(!results.get(0).getBoolean()){ //first point of the session
			//connect session vertex to position vertex:
			//adds Edge(session - start) [Session --> Location]
			//and adds Edge(session - finish) [Location --> Session]

			results = query("S = g.V().has('sessionID',sessionID).next(); "
					+ "L = g.V().has('vertexID',vertexID).next();"
					+ "S.addEdge('session', L, 'type', 'start', 'sessionID', sessionID, 'date', date); "
					+ "L.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date) "
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}else{ //not first point of the session
			params.put("finishEdgeID",results.get(1).getString());

			results = query("S = g.V().has('sessionID',sessionID).next(); " //Session vertex
					+ "E = g.E(finishEdgeID).next(); " //"finish" edge
					+ "newL = g.V().has('vertexID',vertexID).next(); " //new Location
					+ "oldL = g.E(E).outV().next(); " //old last location
					+ "E.remove(); " //remove finish edge
					+ "oldL.addEdge('session', newL, 'type', 'trajectory', 'sessionID', sessionID, 'date', date); " //add edge from last location to new location
					+ "newL.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); " //add finish edge from new location to session
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}
		return true;
	}

	//TODO implement this method
	public boolean put(String sessionID, Date date, String traceBeaconID, Map<String,Object> attributes){
		//return values list
		List<Result> results = null;

		//Make sure the user has the appointed session registered in the DB.
		login(sessionID);

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("traceBeaconID",traceBeaconID);
		params.put("sessionID",sessionID);
		params.put("date", date);
		//				params.put("vertexID",tSession.getVertexID());

		//get id of the "finish" edge
		results = query("Lreturn = new ArrayList<>(); " //return array
				+ "E = g.V().has('sessionID',sessionID).inE().hasLabel('session').has('type','finish').id();" //ID of the "finish" edge
				+ "if(E.hasNext()){" //if there's a finish edge this means that this is not the 1st point being added to the session
				+		"Lreturn.add(true);" //Not the 1st point of session: index(0) - true (has finish edge)
				+ 		"Lreturn.add(E.next());" //index(1) - id
				+ "}else{"//otherwise if there's no "finish" edge this means we are the first point of the session
				+ 		"Lreturn.add(false);" //1st point of session: index(0) - false (does not have finish edge)
				+ 		"Lreturn.add('nothing')"//index(1) - nothing
				+ "};"
				+ "L = g.V().has('traceBeaconID',traceBeaconID); " //get beacon we want to put
				+ "if(L.hasNext()){" //is there any beacon with such an ID?
				+ 		"Lreturn.add(true);" //yes: index(2) - true
				+ 		"Lreturn.add(L.next().value('vertexID'));" //beacon id: index(3) - vertexID
				+ "}else{"
				+ 		"Lreturn.add(false);" //no: index(2) - false
				+ "};"
				+ "Lreturn;"
				+ "",params);

		//				System.out.println("Size: " + results.size());

		if(results == null || results.size() == 0){
			return false;
		}

		//If point doesn't exist, add a new one, label: "unregistered_beacon"
		if(!results.get(2).getBoolean()){
			//params
			params.put("vertexID","" + sessionID + "_" + traceBeaconID);

			//query
			List<Result> addPoint = null;
			addPoint = super.query("graph.addVertex(label,'unregistered_beacon','vertexID', vertexID, 'traceBeaconID', traceBeaconID)", params);

			//failed to add new vertex
			if(addPoint == null){
				return false;
			}
		}else{
			params.put("vertexID",results.get(3).getString());
		}

		//parse attributes
		String attributesString = "";
		Set<String> keys = attributes.keySet();
		if(attributes != null){
			for(String key : keys){
				params.put(key,attributes.get(key));
				attributesString += ", '" +  key + "', " + key;
			}
		}
		attributesString += ");";

		if(!results.get(0).getBoolean()){ //first point of the session
			//connect session vertex to position vertex:
			//adds Edge(session - start) [Session --> Location]
			//and adds Edge(session - finish) [Location --> Session]

			results = query("S = g.V().has('sessionID',sessionID).next(); "
					+ "L = g.V().has('vertexID',vertexID).next();"
					+ "S.addEdge('session', L, 'type', 'start', 'sessionID', sessionID, 'date', date" + attributesString
					+ "L.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); "
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}else{ //not first point of the session
			params.put("finishEdgeID",results.get(1).getString());

			results = query("S = g.V().has('sessionID',sessionID).next(); " //Session vertex
					+ "E = g.E(finishEdgeID).next(); " //"finish" edge
					+ "newL = g.V().has('vertexID',vertexID).next(); " //new Location
					+ "oldL = g.E(E).outV().next(); " //old last location
					+ "E.remove(); " //remove finish edge
					+ "oldL.addEdge('session', newL, 'type', 'trajectory', 'sessionID', sessionID, 'date', date); " //add edge from last location to new location
					+ "newL.addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date); " //add finish edge from new location to session
					+ "",params);
			//if something went wrong
			if(results == null){
				return false;
			}
		}
		return true;
	}

	public boolean put(String sessionID, TraceVertex vertex){

		//location
		if(vertex.getType().equals("location")){
			if(vertex.hasAttributes()){
				return put(sessionID,vertex.getDate(), vertex.getLatitude(), vertex.getLongitude(), vertex.getAttributes());
			}else{
				return put(sessionID,vertex.getDate(), vertex.getLatitude(), vertex.getLongitude());
			}
		}else{ //beacon
			if(vertex.hasAttributes()){
				return put(sessionID, vertex.getDate(), vertex.getBeaconID(), vertex.getAttributes());
			}else{
				return put(sessionID, vertex.getDate(), vertex.getBeaconID());
			}
		}
	}


	public String getSessionDetails(String sessionID){
		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);

		//query Session->A (type:start)
		results = query("S = g.V().has('sessionID',sessionID).next();"
				+ "'Date: ' + S.value('date') + ' SessionID: ' + S.value('sessionID')"
				+ "",params);

		if(results != null){
			return results.get(0).getString();
		}
		return "";
	}


	public List<TraceVertex> getRouteBySession(String sessionID){

		List<TraceVertex> route = new ArrayList<>();

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);


		//		ArrayList to save the trajectory
		results = query("trajectory = new ArrayList<>();"
				+ "A = g.E().hasLabel('session').has('sessionID',sessionID)."
				+ "filter{it.get().value('type') != 'start'}.order().by('date',incr).order().by('type',decr).outV();"
				+ "for(com.thinkaurelius.titan.graphdb.vertices.CacheVertex B : A) {"
				+ 		"trajectory.add(B.value('vertexID')); "
				+ 		"BLabel = B.label();"
				+ 		"if(BLabel == 'location' || BLabel == 'unmapped_location' || BLabel == 'unmapped'){"
				+ 			"trajectory.add('location'); "
				+ 			"trajectory.add(B.value('location').getPoint().getLatitude()); "
				+ 			"trajectory.add(B.value('location').getPoint().getLongitude()); "
				+ 		"}else{"
				+ 			"trajectory.add('beacon'); "
				+ 			"trajectory.add(B.value('traceBeaconID'));"
				+ 		"};"
				+ "};"
				+ "trajectory",params);


		int count = 0;
		while(count<results.size()){
			String name = results.get(count).getString();
			String type = results.get(count+1).getString();
			TraceVertex v;

			if(type.equals("location")){
				v = new TraceVertex(name, results.get(count+2).getFloat(), results.get(count+3).getFloat());
				count+=4;
			}else{
				v = new TraceVertex(name, results.get(count+2).getString());
				count+=3;
			}
			route.add(v);
		}

		return route;
	}

	public List<String> getUserSessions(String username){

		return getUserSessions(username,0);

	}

	public List<String> getUserSessions(String username, int index){

		List<String> userSessions = new ArrayList<>();

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("username", username);
		params.put("index0", index);
		params.put("index1", index+50);

		//ArrayList to save the trajectory 
		results = query("A = g.V().has('username',username).id().next();"
				+ "B = g.V(A).outE('login').inV().order().by('date',decr).range(index0,index1).values('sessionID');"
				+ "",params);

		for(Result r : results){
			userSessions.add(r.getString());
		}

		return userSessions;
	}

	public List<String> getUserSessionsAndDates(String username){
		return getUserSessionsAndDates(username,0);
	}

	public List<String> getUserSessionsAndDates(String username, int index){

		List<String> userSessions = new ArrayList<>();

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("username", username);
		params.put("index0", index);
		params.put("index1", index+50);

		//ArrayList to save the trajectory 
		results = query("A = g.V().has('username',username).id().next();"
				+ "B = g.V(A).outE('login').inV().order().by('date',decr).range(index0,index1).values('sessionID','date');"
				+ "",params);

		for(Result r : results){
			userSessions.add(r.getString());
		}

		return userSessions;
	}

	public List<String> getAllSessions(){

		List<String> userSessions = new ArrayList<>();

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();

		//ArrayList to save the trajectory
		results = query("g.V().hasLabel('session').values('sessionID').toList();"
				+ "",params);

		for(Result r : results){
			userSessions.add(r.getString());
		}

		return userSessions;
	}

	//TODO: Insertion must be done in chronological order, is this assumed or should it be checked here?
	//TODO: Note that ArrayList() keeps the order of insertion, that at least is assured.
	public boolean submitRoute(String sessionID, List<TraceVertex> route){
		boolean success = true;

		//TODO: Do some kind of route parsing so that there are no two subsequent points being added with the same coords.
		//routeParsing()

		//Verify there's actually a sessionID and that we can associate this route with it
		if(login(sessionID)){
			LOG.error("DBTrackingAPI.java - submitRoute: The login failed");
			return false;
		}

		//First: identify the total amount of Km's that have been traveled (travelledDistance).
		double totalDistance = TraceLocationMethods.routeTotalDistance(route); 

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);
		params.put("totalDistance", totalDistance);
		params.put("latitude", route.get(0).getLatitude());
		params.put("longitude", route.get(0).getLongitude());
		params.put("vertexID", ""+route.get(0).getLatitude()+"_"+route.get(0).getLongitude());
		params.put("date", route.get(0).getDate());
		params.put("gpsTolerance", GPS_TOLERANCE);

		//String that will be submitted for the query
		String queryString = "";

		//Get the session Vertex
		queryString += "S = g.V().hasLabel('session').has('sessionID', sessionID).next();";

		//Set A as the sub collection of vertices that we will need for this route (removed this: filter{it.get().label() != 'session'})
		queryString += "A = g.V().has('location', geoWithin(Geoshape.circle(latitude, longitude, totalDistance))).outE();";
		
		//Set subGraph as the graph that only contains those vertices
		queryString += "subGraph = A.subgraph('subGraph').cap('subGraph').next();";
		
		//Set the traversal graph
		queryString += "sg = subGraph.traversal(standard());";
		
		//Set A
		queryString += "A = sg.V();";
		
		//Set P0 as the route point we are considering now.
		queryString += "P0 = Geoshape.point(latitude,longitude);";
		
		//Filter based on the gpsToleranceolerance and order the vertices based on proximity to the vertex we are considering right now, i.e., closest as the first of the collection.
		queryString += "B0 = A.clone().has('location', geoWithin(Geoshape.circle(latitude, longitude, gpsTolerance))).as('a').map{it.get().value('location').getPoint().distance(P0.getPoint())}.order().by(incr).select('a');";
		
		//Check if the list is not empty. Check if the 1st entry is within the acceptable gps tolerance. 
		//In case it's acceptable consider this point
		//In case the point is not acceptable, add a new point to the DB and also to the initial "A" collection.
		queryString += "if(B0.hasNext()){C0 = B0.next().id();}else{"
				+ "Aux0 = g.V().has('vertexID',vertexID);"
				+ "if(Aux0.hasNext()){C0 = Aux0.next().id();}else{C0 = graph.addVertex(label,'unmapped_location','vertexID', vertexID,'location', P0).id();};"
				+ "};";

		//Connect the session vertex to the first point of the route.
		queryString += "S.addEdge('session', g.V(C0).next(), 'type', 'start', 'sessionID', sessionID, 'date', date);";

		//Now "kinda" repeat for every point of the route
		for(int i = 1; i < route.size(); i++){
//			//First get the latitude, longitude, possible vertexID and date for each point of the route
			params.put("lat"+i, route.get(i).getLatitude());
			params.put("lon"+i, route.get(i).getLongitude());
			params.put("vertexID"+i, "" + route.get(i).getLatitude() + "_" + route.get(i).getLongitude());
			params.put("date"+i, route.get(i).getDate());
//
//			//Set Pi as the route point we are considering now.
			queryString += "P"+i+" = Geoshape.point(lat"+i+",lon"+i+");";
//
//			//Clone A and order the vertices based on the proximity to Pi
			queryString += "B"+i+ " = A.clone().has('location', geoWithin(Geoshape.circle(lat"+i+", lon"+i+", gpsTolerance))).as('a').map{it.get().value('location').getPoint().distance(P"+i+".getPoint())}.order().by(incr).select('a');";
//
//			//Check if the list is not empty. Check if the 1st entry is within the acceptable gps tolerance. 
//			//In case it's acceptable consider this point
//			//In case the point is not acceptable, add a new point to the DB and also to the initial "A" collection.
			queryString += "if(B"+i+".hasNext()){C"+i+" = B"+i+".next().id();}else{"
					+ "Aux"+i+" = g.V().has('vertexID',vertexID"+i+");"
					+ "if(Aux"+i+".hasNext()){C"+i+" = Aux"+i+".next().id();}else{C"+i+" = graph.addVertex(label,'unmapped_location','vertexID', vertexID"+i+",'location', P"+i+").id();};"
					+ "};";
//
//			//Connect the session vertex to the first point of the route.
			queryString += "g.V(C"+(i-1)+").next().addEdge('session', g.V(C"+i+").next(), 'type', 'trajectory', 'sessionID', sessionID, 'date', date"+i+");";
		}
//
//		//Complete the cycle and close the route with the "finish" edge
		queryString += "g.V(C"+(route.size()-1)+").next().addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date"+(route.size()-1)+");";

		LOG.info("DBTrackingAPI.java - submitRoute: queryString has a length of:" + queryString.length());
		
		results = query(queryString,params);

		return results!=null;
	}

	//TODO: try to find a way to delete both edges and vertices in a single query
	public boolean removeSession(String sessionID){
		boolean success = true;

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);

		//ArrayList to save the trajectory
		results = query(""
				//				+ "g.V().has('sessionID', sessionID).drop();"
				+ "g.E().has('sessionID', sessionID).drop();"
				+ "",params);

		results = query(""
				+ "g.V().has('sessionID', sessionID).drop();"
				//				+ "g.E().has('sessionID', sessionID).drop();"
				+ "",params);

		return success;
	}
}
