package org.trace.store.middleware.backend;

import java.io.File;

import org.opentripplanner.graph_builder.GraphBuilder;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.standalone.CommandLineParameters;
import org.trace.store.middleware.backend.exceptions.UnableToGenerateGraphException;

public class GraphDB {

	private static GraphDB CONN = new GraphDB();

	private GraphDB(){

	}

	public static GraphDB getConnection(){
		return CONN;
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
		}catch(Exception e){
			throw new UnableToGenerateGraphException(e.getMessage());
		}

		//Step 2 - Given the OTP graph add all vertices to the graph DB
		for(Vertex v : graph.getVertices()){
			//TODO: add to the db
			System.out.println(v);
		}

		//Step 3 - Given the OTP graph add all street edges to the graph DB
		for(StreetEdge e : graph.getStreetEdges()){
			//TODO: add to the db
			System.out.println(e);
		}
	}
	
	/**
	 * Checks if the graph database is still empty, i.e., if the
	 * database has still not been populated.
	 * 
	 * @return True if the database is empty, false otherwise.
	 */
	public boolean isEmptyGraph(){
		//TODO: not implemented yet
		return false;
	}

}
