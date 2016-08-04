package org.trace.inesc.store.storage.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.trace.inesc.store.storage.data.TraceEdge;
import org.trace.inesc.store.storage.data.TraceVertex;


//This Class is responsible for setting up new locations, streets, roads and so on.
public class DBMapAPI extends DBAPI{

	public DBMapAPI(Client client){
		super(client);
	}

	public boolean addLocation(String id, double latitude, double longitude){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("vertexID",id);
		params.put("latitude",latitude);
		params.put("longitude",longitude);

		//query
		results = query("g.V().has('vertexID', vertexID).hasNext()", params);

		//If the point already exists, don't add it.
		if(!results.get(0).getBoolean()){
			//query
			results = query("graph.addVertex(label,'location',"
					+ "'vertexID', vertexID,"
					+ "'location', Geoshape.point(latitude,longitude),"
					+ "", params);
		}

		return results != null;
	}

	public boolean addBeacon(String id, String traceBeaconID){

		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("vertexID",id);
		params.put("traceBeaconID",traceBeaconID);

		//query
		results = query("graph.addVertex(label,'beacon','vertexID', vertexID, 'traceBeaconID', traceBeaconID)", params);

		return results != null;
	}

	public boolean addRoad(String name, String p1, String p2){
		//return values list
		List<Result> results = null; 

		//		System.out.println("DEBUG: addRoad(" + name + "," + p1 + "," + p2 + ")");

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("idA",p1);
		params.put("idB",p2);
		params.put("streetName",name);

		//query
		results = query("A = g.V().hasLabel('location').has('vertexID',idA).next();"
				+ "B = g.V().hasLabel('location').has('vertexID',idB).next();"
				+ "C = new ArrayList();"
				+ "C.add(A.value('location').getPoint().getLatitude());"
				+ "C.add(A.value('location').getPoint().getLongitude());"
				+ "C.add(B.value('location').getPoint().getLatitude());"
				+ "C.add(B.value('location').getPoint().getLongitude());"
				+ "C;"
				+ "", params);

		//The result of this query must contain 4 values otherwise something went wrong.(ex. point doesn't exist)
		if(results.size() < 4){
			return false;
		}else{
			double p1Latitude = results.get(0).getDouble();
			double p1Longitude = results.get(1).getDouble();
			double p2Latitude = results.get(2).getDouble();
			double p2Longitude = results.get(3).getDouble();

			double distance = TraceLocationMethods.distance(p1Latitude, p1Longitude, p2Latitude, p2Longitude, "K");


			//if the distance is too big we do not want to 
			if(distance > 0.008 && distance < 0.500){
				double p3Latitude = TraceLocationMethods.midPoint(p1Latitude, p2Latitude);
				double p3Longitude = TraceLocationMethods.midPoint(p1Longitude, p2Longitude);
				String p3 = "" + p3Latitude + "_" + p3Longitude;

				boolean success = true;

				success = addLocation(p3, p3Latitude, p3Longitude) && success;

				success = addRoad(name+"L", p1, p3) && success;
				success = addRoad(name+"R", p3, p2) && success;

				return success;

			}else{
				results = null;
				results = query("a = g.V().hasLabel('location').has('vertexID',idA).next(); "
						+ "b = g.V().hasLabel('location').has('vertexID',idB).next(); "
						+ "a.addEdge('road', b, 'name', streetName); "
						+ "", params);

				return results != null;
			}
		}

		//query
		//		results = query("a = g.V().hasLabel('location').has('vertexID',idA).next(); "
		//				+ "b = g.V().hasLabel('location').has('vertexID',idB).next(); "
		//				+ "a.addEdge('road', b, 'name', streetName); "
		//				+ "b.addEdge('road', a, 'name', streetName)"
		//				+ "", params);

	}

	public boolean addTwoWayRoad(String name, String p1, String p2, Map<String, Object> properties){
		//return values list
		List<Result> results = null; 

		//params
		Map<String,Object> params = new HashMap<String, Object>();
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

		System.out.println("DEBUG: addRoad(" + name + "," + p1 + "," + p2 + ")");

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("idA",p1);
		params.put("idB",p2);

		//query
		results = query("A = g.V().hasLabel('location').has('vertexID',idA).next();"
				+ "B = g.V().hasLabel('location').has('vertexID',idB).next();"
				+ "C = new ArrayList();"
				+ "C.add(A.value('location').getPoint().getLatitude());"
				+ "C.add(A.value('location').getPoint().getLongitude());"
				+ "C.add(B.value('location').getPoint().getLatitude());"
				+ "C.add(B.value('location').getPoint().getLongitude());"
				+ "C;"
				+ "", params);

		//The result of this query must contain 4 values otherwise something went wrong.(ex. point doesn't exist)
		if(results.size() < 4){
			return false;
		}else{

						addRoadAux(name, p1, p2, properties);

			//TODO: Verify if is this is useful, it splits roads bigger than 16m into two. 
			//Tested and working. Too much overhead on put() for now to be used.
						
//			double p1Latitude = results.get(0).getDouble();
//			double p1Longitude = results.get(1).getDouble();
//			double p2Latitude = results.get(2).getDouble();
//			double p2Longitude = results.get(3).getDouble();
//
//			//Split with 0.016 as distance, i.e., 16m between each vertice.
//			List<TraceVertex> roadVertices = TraceLocationMethods.splitRoad(p1, p1Latitude, p1Longitude, p2, p2Latitude, p2Longitude, 0.016);			
//
//			int count = 1;
//			TraceVertex lastPoint = null;
//			for(TraceVertex v : roadVertices){
//				addLocation(v.getName(), v.getLatitude(), v.getLongitude());
//				if(lastPoint != null){
//					addRoadAux(name+"_"+count, lastPoint.getName(), v.getName(), properties);
//					count++;
////					System.out.println("DEBUG - addRoad: " + name+"_"+count + " " + lastPoint.getName() + "-->" + v.getName());
//				}
//				lastPoint = v;
//			}
			
			return true;
		}	
	}

	private boolean addRoadAux(String name, String p1, String p2, Map<String, Object> properties){
		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("idA",p1);
		params.put("idB",p2);
		params.put("streetName",name);

		String propertiesString = "";

		for(String key : properties.keySet()){
			propertiesString += ",'" + key + "','" + properties.get(key) + "'";
		}

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
		Map<String,Object> params = new HashMap<String, Object>();
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
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("name",name);

		//query
		results = query("g.V().has('name', name).valueMap()", params);

		return results;
	}

	public boolean clearDBVertices(){

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<String, Object>();
		results = query("g.V().drop();",params);

		return results != null;
	}

	public boolean clearDBEdges(){

		//return values list
		List<Result> results = null;

		//params
		Map<String,Object> params = new HashMap<String, Object>();
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
		Map<String,Object> params = new HashMap<String, Object>();
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