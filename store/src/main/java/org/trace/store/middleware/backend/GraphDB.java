package org.trace.store.middleware.backend;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opentripplanner.graph_builder.GraphBuilder;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.standalone.CommandLineParameters;

import org.trace.store.middleware.backend.exceptions.UnableToGenerateGraphException;

import trace.DBMapAPI;
import trace.DBTrackingAPI;
import trace.TraceDB;
import trace.TraceEdge;
import trace.TraceVertex;

public class GraphDB {

	private static final Logger LOG = Logger.getLogger(GraphDB.class); 
	
	private static GraphDB CONN = new GraphDB();
	private final DBMapAPI map;
	private final TraceDB graphDB;
	private final DBTrackingAPI tracking;
	
	private GraphDB(){

		graphDB = new TraceDB();

		//TODO: the path should not be hard-coded
		if(!graphDB.initializePath("/var/traceDB/conf/remote.yaml")){
			throw new RuntimeException("Unable to initialize the Trace database");
		}

		
		map = new DBMapAPI(graphDB.getClient());
		tracking = new DBTrackingAPI(graphDB.getClient());
		LOG.info("Connection with the graph database successfull.");
	}
	public static GraphDB getConnection(){
		return CONN;
	}
	
	public DBTrackingAPI getTrackingAPI(){
		return tracking;
	}

	/**
	 * Given an OSM input file, this method populates the graph database
	 * with the vertices and edges found in said file. This parsing
	 * is partially supported by the OpenTripPlannerLibrary.
	 */
	public void populateFromOSM(File osmInput) throws UnableToGenerateGraphException{

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

		
		List<TraceVertex> vertices = new ArrayList<>();
		List<TraceEdge> edges = new ArrayList<>();
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
		
		//map.addVertices(vertices);
		//map.addEdges(edges);

		
		LOG.info("All vertices and edges successfully added to the graph database.");
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
