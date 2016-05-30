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
import org.trace.DBAPI.data.RouteBoundingBox;
import org.trace.DBAPI.data.TraceVertex;
import org.trace.store.services.TRACEStoreService;

import com.google.gson.Gson;

import io.netty.util.internal.SystemPropertyUtil;

public class DBTrackingAPI extends DBAPI{

	private final Logger LOG = Logger.getLogger(TRACEStoreService.class); 

	public DBTrackingAPI(Client client){
		super(client);
		setup();
	}

	//	public boolean removeSession(String sessionID){
	//		boolean success = true;
	//
	//		//return values list
	//		List<Result> results = null;
	//
	//		//params
	//		Map<String,Object> params = new HashMap<>();
	//		params.put("sessionID", sessionID);
	//
	//		//ArrayList to save the trajectory
	//		results = query(""
	//				//				+ "g.V().has('sessionID', sessionID).drop();"
	//				+ "g.E().has('sessionID', sessionID).drop();"
	//				+ "",params);
	//
	//		results = query(""
	//				+ "g.V().has('sessionID', sessionID).drop();"
	//				//				+ "g.E().has('sessionID', sessionID).drop();"
	//				+ "",params);
	//
	//		return success;
	//	}

	private boolean setup(){
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();

		//query
		results = query(""
				+ ""
				+ ""
				//Returns a String List of all the sessions currently registered in the TraceDB
				+ "public List<String> tracking_getAllSessions(){"
				+ 	"return g.V().hasLabel('session').values('sessionID').toList();"
				+ "};"
				+ ""
				+ ""
				//Registers a specific session in TraceDB in case it hasn't been done before
				+ "public boolean tracking_login(String sessionID){"
				+ 	"if(g.V().has('sessionID',sessionID).hasNext()){"
				+ 		"return true;"
				+ 	"}else{"
				+ 		"S = graph.addVertex(label,'session','sessionID', sessionID, 'date', new Date());"
				+ 		"return true;"
				+ 	"};"
				+ "};"
				+ ""
				+ ""
				//Removes a specific session from TraceDB
				+ "public boolean tracking_removeSession(String sessionID){"
				+ 	"removedV = g.V().has('sessionID', sessionID).drop();"
				+ 	"removedE = g.E().has('sessionID', sessionID).drop();"
				+ 	"return (!removedV && !removedE);"
				+ "};"
				+ ""
				+ ""
				+ "public String tracking_submitRoute(String list){"
				+ 	"return list;"
//				+ 	"String returnString = '';"
//				+ 	"for(String s : list){"
//				+ 		"returnString += s;"
//				+ 	"};"
//				+ 	"return returnString;"
				+ "};"
				+ "", params);

		return results != null;
	}

	private boolean login(String sessionID){

		if(sessionID == null){
			LOG.error("login: sessionID is null!");
			return false;
		}

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID",sessionID);

		//begin a session
		results = query("tracking_login(sessionID);"
				+ "", params);

		if(results != null){
			return results.get(0).getBoolean();
		}else{
			return false;
		}
	}

	public List<String> getSessionsDetails(List<String> sessions){
		//return list
		List<String> sessionsDetails = new ArrayList<>();

		//Nothing to process
		if(sessions == null || sessions.isEmpty()){
			LOG.error("getSessionsDetails: provided list of sessions is null or empty");
			return sessionsDetails;
		}

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();

		//String with the desired query
		String queryString = "";

		queryString = "A = new ArrayList<>();";

		//For each given session
		int i = 0;
		for(String s : sessions){
			//Add the corresponding ID in the params
			params.put("sessionID" + i, s); 

			//Find the corresponding session vertex
			queryString += "S"+i+" = g.V().has('sessionID',sessionID"+i+");";

			//Add its totalDistance to the returning list
			queryString += "A.add(S"+i+".clone().values('totalDistance').next());";
			//			
			//			//Get the STARTING POINT
			queryString += "Start"+i+"= S"+i+".clone().outE().inV().values('location').next().getPoint();";
			//			
			//			//Add its Lat
			queryString += "A.add(Start"+i+".getLatitude());";
			//			//Add its Lon
			queryString += "A.add(Start"+i+".getLongitude());";
			//
			//			//Get the FINISHING POINT
			queryString += "Finish"+i+"= S"+i+".clone().inE().outV().values('location').next().getPoint();";
			//			
			//			//Add its Lat
			queryString += "A.add(Finish"+i+".getLatitude());";
			//			//Add its Lon
			queryString += "A.add(Finish"+i+".getLongitude());";

			i++;
		}
		queryString += "A;";

		results = query(queryString,params);

		if(results == null || results.isEmpty()){
			LOG.error("getSessionsDetails: Results from the query are either null or empty.");
			return sessionsDetails;
		}

		for(Result r : results){
			sessionsDetails.add(r.getString());
		}

		return sessionsDetails;
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
	
	public double submitRoute(String sessionID, List<TraceVertex> submittedRoute){
		boolean success = true;

		//Route parsing so that there are no two subsequent points being added with the same coords.
		List<TraceVertex> route = TraceLocationMethods.routeParser(submittedRoute);

		LOG.info("submitRoute: submittedRoute:" + submittedRoute.size());
		LOG.info("submitRoute: route:" + route.size());

		//TODO remove the "removeSession()" and uncomment the rest after DEBUG PHASE is completed
		if(!sessionIsEmpty(sessionID)){
			removeSession(sessionID);
			//			LOG.error("submitRoute: session is not empty!");
			//			return -1;
		}

		Double totalDistance = TraceLocationMethods.routeTotalDistance(route);

		boolean firstOne = true;
		int i = 0;
		for(i = 0; i+79 < route.size(); i+=80){
			LOG.info("submitRoute: i:" + i);
			if(firstOne){
				firstOne = false;
				success = submitFirstRoute(sessionID,route.subList(i, i+80), totalDistance) && success;
			}else{
				success = submitMoreRoutes(sessionID, route.subList(i, i+80)) && success;
			}
		}
		LOG.info("submitRoute: i:" + i);
		if(route.size() > i){
			if(firstOne){
				firstOne = false;
				success = submitFirstRoute(sessionID, route.subList(i, route.size()), totalDistance) && success;
			}else{
				success = submitMoreRoutes(sessionID, route.subList(i, route.size())) && success;
			}
			//			success = submitMoreRoutes(sessionID, route.subList(i, route.size())) && success;
		}

		if(success){
			return totalDistance;
		}else{
			return -1;
		}
	}

	public boolean removeSession(String sessionID){
		boolean success = true;

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);

		//ArrayList to save the trajectory
		results = query("tracking_removeSession(sessionID);"
				+ "",params);

		return success;
	}

	private boolean sessionIsEmpty(String sessionID){
		if(sessionID == null){
			LOG.error("sessionIsEmpty: sessionID is null!");
			return false;
		}

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID",sessionID);
		params.put("date",new Date());

		//Save in A the session
		results = query("A = g.V().has('sessionID', sessionID);"
				+ "B = true;"
				//Check if it actually exists
				+ "if(A.clone().hasNext()){"
				//In case that it does, check if it has any route already set to it
				+ 	"B = A.outE().has('type','start').hasNext();"
				+ 	"B = !B;"
				+ "}else{"
				//In case it doesn't exist, create one.
				+ 	"graph.addVertex(label,'session','sessionID', sessionID, 'date', date);"
				+ "};"
				+ "B;"
				+ "", params);

		//		if(results != null){
		//			LOG.info("results != null:true");
		//			LOG.info("results.get(0).getBoolean():" + results.get(0).getBoolean());
		//		}

		return ((results != null) && results.get(0).getBoolean());
	}

	private boolean submitFirstRoute(String sessionID, List<TraceVertex> route, double totalDistance){
		boolean success = true;

		//Verify there's actually a sessionID and that we can associate this route with it
		if(!login(sessionID)){
			LOG.error("submitFirstRoute: The login failed");
			return false;
		}

		if(route == null || route.isEmpty()){
			LOG.error("submitFirstRoute: route is null or empty");
			return false;
		}

		//First: identify the bounding box of the trajectory
		RouteBoundingBox routeBoundingBox = TraceLocationMethods.getRouteBoundingBox(route); 

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);
		params.put("totalDistance", totalDistance);

		params.put("SWLat", routeBoundingBox.getSWLat());
		params.put("SWLon", routeBoundingBox.getSWLon());
		params.put("NELat", routeBoundingBox.getNELat());
		params.put("NELon", routeBoundingBox.getNELon());

		params.put("latitude", route.get(0).getLatitude());
		params.put("longitude", route.get(0).getLongitude());
		params.put("vertexID", ""+route.get(0).getLatitude()+"_"+route.get(0).getLongitude());
		params.put("date0", route.get(0).getDate());
		params.put("gpsTolerance", GPS_TOLERANCE);

		//String that will be submitted for the query
		String queryString = "";

		//Get the session Vertex
		queryString += "S = g.V().hasLabel('session').has('sessionID', sessionID).next();";

		//Set A as the sub collection of vertices that we will need for this route (removed this: filter{it.get().label() != 'session'})
		queryString += "A = g.V().has('location', geoWithin(Geoshape.box(SWLat,SWLon,NELat,NELon))).outE();";

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

		queryString += "S.property('totalDistance',totalDistance);";

		//Connect the session vertex to the first point of the route.
		queryString += "S.addEdge('session', g.V(C0).next(), 'type', 'start', 'sessionID', sessionID, 'date', date0);";

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

		LOG.info("submitFirstRoute: route length:" + route.size());
		LOG.info("submitFirstRoute: queryString has a length of:" + queryString.length());

		results = query(queryString,params);

		return results!=null;
	}

	private boolean submitMoreRoutes(String sessionID, List<TraceVertex> route){
		boolean success = true;

		if(sessionIsEmpty(sessionID)){
			LOG.error("submitMoreRoutes: Session is empty, this method expects at least one route submitted before");
			return false;
		}

		if(route == null || route.isEmpty()){
			LOG.error("submitMoreRoutes: route is null or empty");
			return false;
		}

		//First: identify the bounding box of the trajectory
		RouteBoundingBox routeBoundingBox = TraceLocationMethods.getRouteBoundingBox(route); 

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("sessionID", sessionID);
		params.put("SWLat", routeBoundingBox.getSWLat());
		params.put("SWLon", routeBoundingBox.getSWLon());
		params.put("NELat", routeBoundingBox.getNELat());
		params.put("NELon", routeBoundingBox.getNELon());

		params.put("latitude", route.get(0).getLatitude());
		params.put("longitude", route.get(0).getLongitude());
		params.put("vertexID", ""+route.get(0).getLatitude()+"_"+route.get(0).getLongitude());
		params.put("date0", route.get(0).getDate());
		params.put("gpsTolerance", GPS_TOLERANCE);

		//String that will be submitted for the query
		String queryString = "";

		//Get the session "finish" edge
		queryString += "E0 = g.V().hasLabel('session').has('sessionID', sessionID).inE().has('type','finish');";

		//Get the ID of the last vertex
		queryString += "finishID = E0.clone().next().id();";

		//Get the session Vertex
		queryString += "SessionVertex = g.V().hasLabel('session').has('sessionID', sessionID).next();";

		//Get the Vertex of the last location of the session
		queryString += "S = E0.clone().outV().next();";

		//Set A as the sub collection of vertices that we will need for this route (removed this: filter{it.get().label() != 'session'})
		queryString += "A = g.V().has('location', geoWithin(Geoshape.box(SWLat,SWLon,NELat,NELon))).outE();";

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
		queryString += "S.addEdge('session', g.V(C0).next(), 'type', 'trajectory', 'sessionID', sessionID, 'date', date0);";

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

		//		//Complete the cycle and close the route with the "finish" edge
		queryString += "g.V(C"+(route.size()-1)+").next().addEdge('session', SessionVertex, 'type', 'finish', 'sessionID', sessionID, 'date', date"+(route.size()-1)+");";

		//		queryString += "finishID;";

		//Drop the session "finish" edge
		queryString += "g.E(finishID).drop();";

		results = query(queryString,params);
		//		System.out.println("+++++++"+results.get(0).getString());

		//		params.clear();
		//		if(results != null && !results.isEmpty()){
		//			LOG.info("finishID: " + results.get(0).getString());
		//			params.put("finishID", results.get(0).getString());
		//
		//			queryString = "g.E(finishID).drop();";
		//			results = query(queryString,params);
		//		}

		return results!=null;
	}

//	public String test(List<String> list){
//		List<Result> results = null;
//		
//		Gson gson = new Gson();
//
//		//params
//		Map<String,Object> params = new HashMap<>();
//		String gsonString = gson.toJson(list);
//		System.out.println(gsonString);
//		
//		params.put("list", gson);
//
//		//query
//		results = query(""
//				+ "tracking_submitRoute((String) list);"
//				+ "",params);
//
//		if(results != null){
//			return results.get(0).getString();
//		}
//		return "";
//	}
}
