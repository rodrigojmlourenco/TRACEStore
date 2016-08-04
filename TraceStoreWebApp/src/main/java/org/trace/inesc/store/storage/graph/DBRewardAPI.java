package org.trace.inesc.store.storage.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;

public class DBRewardAPI extends DBAPI{
	
	public DBRewardAPI(Client client){
		super(client);
	}

	//TODO implement this method
	public boolean register(String username, String password, String name){
		boolean success = true;
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("username",username);
		params.put("password",password);
		params.put("name",name);

		//query
		results = query("graph.addVertex(label,'shopOwner','username', username, 'password', password, 'name', name)", params);
		
		return success;
	}
	
	public boolean associateLocation(String username, String password, double xCoord, double yCoord){
		boolean success = true;
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("username",username);
		params.put("password",password);
		params.put("x",xCoord);
		params.put("y",yCoord);

		//query
		results = query("O = g.V().hasLabel('shopOwner').has('username',username).has('password',password).next(); "
				+ "L = g.V().hasLabel('location').has('location',Geoshape.point(x,y)).next(); "
				+ "O.addEdge('shopLocation',L)", params);
		
		return success;
	}
	
	public List<Result> sessionsOnShop(String username, String password){
		boolean success = true;
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("username",username);
		params.put("password",password);

		//query
		results = query("L = g.V().hasLabel('shopOwner').has('username',username).has('password',password).out('shopLocation'); "
				+ "L.inE('session').values('sessionID')", params);
		
//		L.inE('session').values('sessionID')
		
		return results;
	}
	
	public List<Result> countSessionsOnShop(String username, String password){
		boolean success = true;
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("username",username);
		params.put("password",password);

		//query
		results = query("L = g.V().hasLabel('shopOwner').has('username',username).has('password',password).out('shopLocation'); "
				+ "L.inE('session').count()", params);
		
//		L.inE('session').values('sessionID')
		
		return results;
	}
	
	public boolean usersOnShop(String username, String password){
		boolean success = true;
		List<Result> results = null;
		
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("username",username);
		params.put("password",password);

		//query
		results = query("L = g.V().hasLabel('shopOwner').has('username',username).has('password',password).out('shopLocation'); "
				+ "SIDs = L.inE('session'); "
				+ "users = new ArrayList<String>(); "
				+ "A = 1; B = 2; "
//				+ "while(SIDs.hasNext()) {A = g.V().hasLabel('session').has('sessionID',SIDs.next().values('sessionID'))}; "
//				+ "SIDs.next().values('sessionID')"
				+ "SIDs"
				+ "", params);
		
		return success;
	}
	
	public boolean sessionVertexOfEdge(String sessionID){
		//params
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("sessionID",sessionID);
		
		query("session = g.V().hasLabel('location').has('name','D').next(); "
				+ "A = session; "
//				+ "while(A.label() == 'location') {A = A.inVertex('session').has('sessionID', A.values('sessionID').next())}"
//				+ "g.V(A).in('session').has('sessionID')"
				+ "A.class",params);
		
		return true;
	}
	
	public boolean setShop(int shopId, int ownerId, double latitude, double longitude){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("shopId",shopId);
		params.put("ownerId",ownerId);
		params.put("latitude",latitude);
		params.put("longitude",longitude); 

		//Check if position 
		
		query("graph.addVertex(label,'shop','shopId', shopId, 'ownerId', ownerId, 'location', Geoshape.point(latitude,longitude))"
				+ "",params);
		return true;
	}
	
	public boolean updateShop(int shopId, double latitude, double longitude){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("shopId",shopId);
		params.put("latitude",latitude);
		params.put("longitude",longitude); 

		//Check if position 
		
		query("g.V().hasLabel('shop').has('shopId', shopId)"
				+ ".property('location',Geoshape.point(latitude,longitude));"
				+ "",params);
		
		return true;
	}
	
	public List<String> getShopsIds(double latitude, double longitude, double radius){
		List<String> shopsId = new ArrayList<String>();
		List<Result> results = null;
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("latitude",latitude);
		params.put("longitude",longitude); 
		params.put("radius",radius); 

		results = query("g.V().hasLabel('shop')"
				+ ".has('location', geoWithin(Geoshape.circle(latitude, longitude, radius)))"
				+ ".values('shopId');"
				+ "",params);
		
		for(Result result : results){
			shopsId.add(result.getString());
		}
		return shopsId;
	}
	
	public List<String> getShopsOwnerIds(double latitude, double longitude, double radius){
		List<String> ownerIds = new ArrayList<String>();
		List<Result> results = null;
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("latitude",latitude);
		params.put("longitude",longitude); 
		params.put("radius",radius); 

		results = query("g.V().hasLabel('shop')"
				+ ".has('location', geoWithin(Geoshape.circle(latitude, longitude, radius)))"
				+ ".values('ownerId').dedup();"
				+ "",params);
		
		for(Result result : results){
			ownerIds.add(result.getString());
		}
		return ownerIds;
	}
	
	public List<String> getShops(double latitude, double longitude, double radius){
		List<String> ownerIds = new ArrayList<String>();
		List<Result> results = null;
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("latitude",latitude);
		params.put("longitude",longitude); 
		params.put("radius",radius); 

		results = query("g.V().hasLabel('shop')"
				+ ".has('location', geoWithin(Geoshape.circle(latitude, longitude, radius)))"
				+ ";"
				+ "",params);
		
		for(Result result : results){
			ownerIds.add(result.getString());
		}
		return ownerIds;
	}
}
