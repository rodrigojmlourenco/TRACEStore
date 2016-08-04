package org.trace.inesc.services;
 
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.trace.inesc.filters.Secured;

import com.google.gson.JsonParser;

@Secured
@Path("/hello")
public class HelloWorldService {
 
	@GET
	@Path("/{param}")
	public Response getMsg(@PathParam("param") String msg) {
 
		JsonParser parser = new JsonParser();
		String output = "Jersey say v3 : " + parser.parse(msg).toString();
 
		return Response.status(200).entity(output).build();
 
	}
 
}