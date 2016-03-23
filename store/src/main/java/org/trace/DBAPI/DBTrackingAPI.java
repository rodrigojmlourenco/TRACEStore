package org.trace.DBAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.trace.DBAPI.data.TraceVertex;

import io.netty.util.internal.SystemPropertyUtil;

public class DBTrackingAPI extends DBAPI{

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
		
		System.out.println("route size: " + route.size());
		
		login(sessionID);
		System.out.println("sessionID: " + sessionID);

		//First: identify the total amount of Km's that have been travelled (travelledDistance).
		double totalDistance = TraceLocationMethods.routeTotalDistance(route); 
		System.out.println("totalDistance = " + totalDistance);

		//Second: prepare a query that does a geowithin based on the first point of the trajectory and with a radius of "travelledDistanced". (geoPoints)
		//Submit each point of the route and always use the geoPoints universe instead of the whole DB.

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
		
		System.out.println("lat:" + route.get(0).getLatitude());
		System.out.println("lon:" + route.get(0).getLongitude());

		//ArrayList to save the trajectory 
		results = query("g.V().has('location', geoWithin(Geoshape.circle(latitude, longitude, totalDistance))).values('vertexID');"
				+ "",params);
		
		params.remove("totalDistance");

		List<String> IDs = new ArrayList<>();
		String map = "g.V()";

		System.out.println(results.size());
		
		if(!results.isEmpty()){
			map+=".or( ";
			//ArrayList to save the trajectory 
			for(Result r : results){
				IDs.add(r.getString());
				map += "has('vertexID','" +r.getString()+ "'),";
			}
			map = map.substring(0, map.length()-1);
			map += ")";
		}
		map+=";";
		

		//Query: 1st) Put the mapping of the vertices.
		//		 2nd) Check if vertices exist, if not add.
		//       3rd) 
		
		//TODO: nao fazer clone e lookup, apenas usar uma ordenação e ver a distancia do primeiro
		String queryString = "";
		queryString += "S = g.V().hasLabel('session').has('sessionID', sessionID).next();";
		queryString += "A = " + map;
		queryString += "B0 = A.clone().has('location', geoWithin(Geoshape.circle(latitude, longitude, " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(latitude,longitude).getPoint())}.order().by(incr).select('a');";
		queryString += "if(B0.hasNext()){C0 = B0.next();}else{CAux0 = graph.addVertex(label,'unmapped_location','vertexID', vertexID,'location', Geoshape.point(latitude,longitude));C0 = g.V(CAux0).next();};";
		queryString += "S.addEdge('session', C0, 'type', 'start', 'sessionID', sessionID, 'date', date);";
		
		for(int i = 1; i < route.size(); i++){
			params.put("lat"+i, route.get(i).getLatitude());
			params.put("lon"+i, route.get(i).getLongitude());
			params.put("vertexID"+i, "" + route.get(i).getLatitude() + "_" + route.get(i).getLongitude());
			params.put("date"+i, route.get(i).getDate());

			queryString += "B"+i+ " = A.clone().has('location', geoWithin(Geoshape.circle(lat"+i+", lon"+i+", " + GPS_TOLERANCE + "))).as('a').map{it.get().value('location').getPoint().distance(Geoshape.point(lat"+i+",lon"+i+").getPoint())}.order().by(incr).select('a');";
			//TODO: if B hasNext ou "vertexID" exists... duplicate values...
			queryString += "if(B"+i+".hasNext()){C"+i+" = B"+i+".next();}else{CAux"+i+" = graph.addVertex(label,'unmapped_location','vertexID', vertexID"+i+",'location', Geoshape.point(lat"+i+",lon"+i+"));C"+i+" = g.V(CAux"+i+").next();};";
			queryString += "C"+(i-1)+".addEdge('session', C"+i+", 'type', 'trajectory', 'sessionID', sessionID, 'date', date"+i+");";
		}
//		
		queryString += "C"+(route.size()-1)+".addEdge('session', S, 'type', 'finish', 'sessionID', sessionID, 'date', date"+(route.size()-1)+");";
		
//		System.out.println(queryString);
		
		results = query(queryString,params);

		//Old code
		//		for(TraceVertex v : route){
		//			success = put(sessionID, v) && success;
		//		}

		return success;
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
