package org.trace.inesc.store.middleware.backend;

import java.io.File;

import org.apache.log4j.Logger;
import org.trace.inesc.store.middleware.exceptions.UnableToGenerateGraphException;
import org.trace.inesc.store.storage.graph.DBMapAPI;
import org.trace.inesc.store.storage.graph.DBRewardAPI;
import org.trace.inesc.store.storage.graph.DBTrackingAPI;
import org.trace.inesc.store.storage.graph.TraceDB;

public class GraphDB {

	private static final Logger LOG = Logger.getLogger(GraphDB.class); 
	
	private static GraphDB CONN = new GraphDB();
	private final DBMapAPI map;
	private final TraceDB graphDB;
	private final DBTrackingAPI tracking;
	private final DBRewardAPI reward;

	
	private GraphDB(){

		graphDB = new TraceDB();

		//TODO: the path should not be hard-coded
		if(!graphDB.initializePath("/var/traceDB/conf/remote.yaml")){
			throw new RuntimeException("Unable to initialize the Trace database");
		}

		
		map = new DBMapAPI(graphDB.getClient());
		tracking = new DBTrackingAPI(graphDB.getClient());
		reward = new DBRewardAPI(graphDB.getClient());
		LOG.info("Connection with the graph database successfull.");
	}
	public static GraphDB getConnection(){
		return CONN;
	}
	
	public DBTrackingAPI getTrackingAPI(){
		return tracking;
	}
	
	public DBRewardAPI getRewardAPI() {
		return reward;
	}
	/**
	 * Given an OSM input file, this method populates the graph database
	 * with the vertices and edges found in said file. This parsing
	 * is partially supported by the OpenTripPlannerLibrary.
	 */
	public void populateFromOSM(File osmInput) throws UnableToGenerateGraphException{

		/*
		//Stage 1 - Generate an OTP graph
		CommandLineParameters params = new CommandLineParameters();
		params.inMemory = true;

		Graph graph;
		try{
			GraphBuilder builder = GraphBuilder.forDirectory(params, osmInput);
			builder.run();
			graph = builder.getGraph();
			LOG.info("Temporary graph successfully generated from files found in "+osmInput.getAbsolutePath());
		}catch(Exception e){
			throw new UnableToGenerateGraphException(e.getMessage());
		}

		
		List<TraceVertex> vertices = new ArrayList<TraceVertex>();
		List<TraceEdge> edges = new ArrayList<TraceEdge>();
		//Step 2 - Given the OTP graph add all vertices to the graph DB
		String id;
		for(Vertex v : graph.getVertices()){
			id = v.getLabel().split(":")[2];
			vertices.add(new TraceVertex(id, v.getY(), v.getX()));
		}

		//Step 3 - Given the OTP graph add all street edges to the graph DB
		for(StreetEdge e : graph.getStreetEdges()){
			edges.add(new TraceEdge(e.getName(), Long.toString(e.getStartOsmNodeId()), Long.toString(e.getEndOsmNodeId())));
		}
		
		map.addVertices(vertices);
		map.addEdges(edges);

		
		LOG.info("All vertices and edges successfully added to the graph database.");
		*/
		LOG.error("Unable to populate graph - problems resolving the dependencies for the war!");
	}

	/**
	 * Checks if the graph database is still empty, i.e., if the
	 * database has still not been populated.
	 * 
	 * @return True if the database is empty, false otherwise.
	 */
	public boolean isEmptyGraph(){
		return map.isEmpty();
	}
	
	public static void main(String[] args){
		
	}
}
