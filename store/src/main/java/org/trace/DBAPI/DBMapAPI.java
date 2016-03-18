package org.trace.DBAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;

//This Class is responsible for setting up new locations, streets, roads and so on.
public class DBMapAPI extends DBAPI{

	public DBMapAPI(Client client){
		super(client);
	}

	public boolean addLocation(String id, double latitude, double longitude){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("vertexID",id);
		params.put("latitude",latitude);
		params.put("longitude",longitude);
		params.put("latitudeGridID", TraceLocationMethods.getGridID(latitude));
		params.put("longitudeGridID", TraceLocationMethods.getGridID(longitude));

		//query
		results = query("graph.addVertex(label,'location',"
				+ "'vertexID', vertexID,"
				+ "'location', Geoshape.point(latitude,longitude),"
				+ "'latitudeGridID', latitudeGridID,"
				+ "'longitudeGridID', longitudeGridID)", params);
		
		return results != null;
	}
	
	public boolean addBeacon(String id, String traceBeaconID){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("vertexID",id);
		params.put("traceBeaconID",traceBeaconID);

		//query
		results = query("graph.addVertex(label,'beacon','vertexID', vertexID, 'traceBeaconID', traceBeaconID)", params);

		return results != null;
	}
	
	public boolean addRoad(String name, String p1, String p2){
		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("idA",p1);
		params.put("idB",p2);
		params.put("streetName",name);

		//query
		results = query("a = g.V().hasLabel('location').has('vertexID',idA).next(); "
				+ "b = g.V().hasLabel('location').has('vertexID',idB).next(); "
				+ "a.addEdge('road', b, 'name', streetName); "
				+ "b.addEdge('road', a, 'name', streetName)"
				+ "", params);
		
		return results != null;
	}
	
	public boolean addTwoWayRoad(String name, String p1, String p2, Map<String, Object> properties){
		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("idA",p1);
		params.put("idB",p2);
		params.put("streetName",name);
		
		String propertiesString = "";
		
		for(String key : properties.keySet()){
			propertiesString += ",'" + key + "','" + properties.get(key) + "'";
		}
		

		//query
		results = query("a = g.V().hasLabel('location').has('vertexID',idA).next(); "
				+ "b = g.V().hasLabel('location').has('vertexID',idB).next(); "
				+ "a.addEdge('road', b, 'name', streetName" + propertiesString + "); "
				+ "b.addEdge('road', a, 'name', streetName" + propertiesString + "); "
				+ "", params);
		
		return results != null;
	}
	
	public boolean addRoad(String name, String p1, String p2, Map<String, Object> properties){
		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("idA",p1);
		params.put("idB",p2);
		params.put("streetName",name);
		
		String propertiesString = "";
		
		for(String key : properties.keySet()){
			propertiesString += ",'" + key + "','" + properties.get(key) + "'";
		}
		
		//query
		results = query("a = g.V().hasLabel('location').has('vertexID',idA).next(); "
				+ "b = g.V().hasLabel('location').has('vertexID',idB).next(); "
				+ "a.addEdge('road', b, 'name', streetName" + propertiesString + "); "
				+ "", params);
		
		return results != null;
	}

	public List<Result> locationLookUpRadius(double latitude, double longitude, double radius){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("latitude",latitude);
		params.put("longitude",longitude);
		params.put("radius",radius);

		//query
		results = query("g.V().has('location', geoWithin(Geoshape.circle(latitude, longitude, radius))).valueMap()", params);

		return results;
	}

	public List<Result> locationLookUpName(String name){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<>();
		params.put("name",name);

		//query
		results = query("g.V().has('name', name).valueMap()", params);

		return results;
	}
	
	public boolean clearDBVertices(){
		
		//return values list
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<>();
		results = query("g.V().drop();",params);

		return results != null;
	}
	
	public boolean clearDBEdges(){
		
		//return values list
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<>();
		results = query("g.E().drop();",params);

		return results != null;
	}
	
	public boolean clearDB(){	
		
		return clearDBEdges() && clearDBVertices();
	}
	
	//adds all vertices on the list
	//returns true if all vertices were added, false otherwise
	public boolean addVertices(List<TraceVertex> vertices){
		
		boolean success = true;
		
		for(TraceVertex v : vertices){
			if(v.getType() == "location"){
				success = addLocation(v.getName(), v.getLatitude(), v.getLongitude()) && success;
			}else{
				success = addBeacon(v.getName(), v.getBeaconID()) && success;
			}
		}
		return success;
	}
	
	public boolean addEdges(List<TraceEdge> edges){
		
		boolean success = true;
		
		for(TraceEdge e : edges){
			success = success && addRoad(e.getName(),e.getAVertex(),e.getBVertex(),e.getProperties());
		}
		return success;
	}
	
	public boolean isEmpty(){
		
		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<>();
		results = query("V = g.V().count().next(); E = g.E().count().next(); V + E;",params);

		//if it was successful 
		if(results != null){
			//get number of vertices and edges, should be zero if the DB is empty
			int verticesAndEdges = Integer.parseInt(results.get(0).getString());
			return verticesAndEdges == 0;
		}
		return false;
	}
}