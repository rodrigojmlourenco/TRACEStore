package org.trace.DBAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.trace.DBAPI.data.TraceSession;

public class Trace {

	public static void main(String [] args){

		System.out.println("Trace.java");
		
		//Logger configurations
//		org.apache.log4j.BasicConfigurator.configure();
		
		//Data Base connection class, hides unnecessary complexity
		TraceDB db = new TraceDB();
		
		//Initialize the db connection
		if(!db.initialize("remote")){
			return;
		}
		
		try{
			
			//Get the APIs we need
			DBMapAPI api = new DBMapAPI(db.getClient());
			DBTrackingAPI apiTracking = new DBTrackingAPI(db.getClient());
			DBRewardAPI apiReward = new DBRewardAPI(db.getClient());
			DBPlannerAPI apiPlanner = new DBPlannerAPI(db.getClient());
			
//			//Clear DB of any data
//			if(!api.clearDB()){
//				System.out.println("ERROR CLEARING DB");
//				return;
//			}
			
//			System.out.println("isEmpty: " + api.isEmpty());
			
//			//Populate DB - Locations and Streets
//			api.addLocation("A", 10, 10);
//			api.addLocation("B", 11, 10);
//			api.addLocation("C", 12, 10);
//			api.addLocation("D", 12, 9);
//			api.addBeacon("A_Beacon", new TraceBeaconID("ID1"));
//			api.addBeacon("B_Beacon", new TraceBeaconID("ID2"));
//			api.addRoad("A2B", "A", "B");
//			api.addRoad("B2C", "B", "C");
//			api.addRoad("C2D", "C", "D");
//			
//			System.out.println("isEmpty: " + api.isEmpty());
//			
//			Map<String,Object> properties = new HashMap<>();
//			properties.put("gradiant", 10);
//			properties.put("type", "highway");
//
//			api.addRoad("D2B", "D", "B",properties);
//			
//			List<TraceVertex> vertices = new ArrayList<>();
//			vertices.add(new TraceVertex("abc",9,10));
//			vertices.add(new TraceVertex("bcd",8,10));
//			vertices.add(new TraceVertex("cde",7,10));
//			vertices.add(new TraceVertex("bcd1",7,10));
//			
//			vertices.add(new TraceVertex(new TraceBeaconID("BeaconID100")));
//			api.addVertices(vertices);
//			
//			properties.put("traffic", "tooMuch");
//			List<TraceEdge> edges = new ArrayList<>();
//			edges.add(new TraceEdge("abc2bcd","abc","bcd"));
//			edges.add(new TraceEdge("bcd2cde","bcd","cde",properties));
//			api.addEdges(edges);
////			
////			//Populate DB with users
//			apiTracking.register("john123", "123", "John Snow", "some street");
//			apiTracking.register("john234", "234", "John Stark", "another street");
//
//			//Populate DB - User Trajectories
//			String tSession = apiTracking.login("john123", "123");
//			if(tSession != null){
//				apiTracking.put(tSession,new Date(), 10,10);
//				apiTracking.put(tSession,new Date(), 11,10);
//				apiTracking.put(tSession,new Date(), 12,9);
//				
//			}else{
//				System.out.println("Failure on login!");
//			}
//			Thread.sleep(1000, 0);
//			
//			String tSession2 = apiTracking.login("john123", "123");
//			if(tSession2 != null){
//				apiTracking.put(tSession2,new Date(), 10,10);
//				apiTracking.put(tSession2,new Date(), 11,10);
//				apiTracking.put(tSession2,new Date(), 12,10);
//				apiTracking.put(tSession2,new Date(), 12,9);
//				
//			}else{
//				System.out.println("Failure on login!");
//			}
//
//			Thread.sleep(1000, 0);
//			
//			String tSession3 = apiTracking.login("john123", "123");
//			if(tSession3 != null){
//				apiTracking.put(tSession3,new Date(), 10,10);
//				apiTracking.put(tSession3,new Date(), 11,10);
//				apiTracking.put(tSession3,new Date(), 12,10);
//				apiTracking.put(tSession3,new Date(), 12,9);
//				apiTracking.put(tSession3,new Date(), 11,10);
//				
//			}else{
//				System.out.println("Failure on login!");
//			}
			
			//Get user sessions
//			List<String> userSessions = apiTracking.getUserSessions("john123","123");
//			List<String> userSessions = apiTracking.getUserSessions("rodrigo","passWord123#");
//
//			//display them
//			int count = 1;
//			for(String s : userSessions){
//				System.out.println(count++ + ": " + apiTracking.getSessionDetails(s));
//			}
//			
//			List<String> route = apiTracking.getTrajectory("0.6087072093132015");
//			System.out.println(route);
			
//			boolean wentWell = true;
//			String tSession = apiTracking.login("john123", "123");
//			if(tSession != null){
//				wentWell = apiTracking.put(tSession,new Date(), 100,10) && wentWell;
//				wentWell = apiTracking.put(tSession,new Date(), 11,10) && wentWell;
//				wentWell = apiTracking.put(tSession,new Date(), 12,9) && wentWell;
//				
//			}else{
//				System.out.println("Failure on login!");
//			}
//			
//			System.out.println(wentWell);
			
			//get input
//			Scanner in = new Scanner(System.in);
//			int num = in.nextInt();
			
//			System.out.println(userSessiosn.get(num-1).getDateString());
			
			//get the route taken in given session
//			apiTracking.getTrajectory(userSessiosn.get(2));
			
			//Populate DB with shop owners
//			apiReward.register("carl30", "qwerty", "Carl Scarface");
//			apiReward.associateLocation("carl30", "qwerty", 12, 9);
			
			//Queries about sessions on Carl's shop
//			apiReward.sessionsOnShop("carl30","qwerty");
//			apiReward.countSessionsOnShop("carl30","qwerty");
//			apiReward.usersOnShop("carl30","qwerty");
//			apiReward.sessionVertexOfEdge("0.7663705834913859");
			
//			if(!api.clearDB()){
//				System.out.println("ERROR CLEARING DB");
//				return;
//			}
//			System.out.println("isEmpty: " + api.isEmpty());

			
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++			
//			List<String> userSessions = apiTracking.getUserSessions("miguel","passWord123#");

//			String tSession = apiTracking.login("miguel",new TraceSession().getSessionID());
//			List<String> sessions = apiTracking.getUserSessionsAndDates("miguel");
//			
//			for(String s : sessions){
//				System.out.println(s);
//			}
			
//			api.addLocation("t1", 33.735374450684, -9.1406631469727);
//			api.addLocation("t2", 33.730535766602, -9.1371507644653);
//			api.addRoad("t1Tot2", "t1", "t2", new HashMap<>());
			
//			System.out.println("tSession: " + tSession);
//
//			Map<String,Object> attributes = new HashMap<>();
//			attributes.put("decline", 0.1);
//			attributes.put("mode", "walking");
			

//			Date date1 = new Date();
////			
//			if(tSession != ""){
//				apiTracking.put(tSession,new Date(), 38.735374450684,-9.1406631469727);
//				apiTracking.put(tSession,new Date(), 38.735374450684,-9.1406631469727);
//				
//				apiTracking.put(tSession,new Date(), 38.735172271729,-9.140739440918);
//				apiTracking.put(tSession,new Date(), 38.735172271729,-9.140739440918);
//				
//				apiTracking.put(tSession,new Date(), 38.735282897949,-9.1403131484985);
//				apiTracking.put(tSession,new Date(), 38.735282897949,-9.1403131484985);
//				apiTracking.put(tSession,new Date(), 38.735118865967,-9.1405467987061);
//				
//				apiTracking.put(tSession,new Date(), 38.735118865967,-9.1405467987061);
//				apiTracking.put(tSession,new Date(), 38.735328674316,-9.1404991149902);
//				apiTracking.put(tSession,new Date(), 38.735328674316,-9.1404991149902);
//				
//				apiTracking.put(tSession,new Date(), 38.735210418701,-9.1404447555542);
//				apiTracking.put(tSession,new Date(), 38.735210418701,-9.1404447555542);
//				apiTracking.put(tSession,new Date(), 38.73490524292,-9.1402463912964);
//
//				apiTracking.put(tSession,new Date(), 38.73490524292,-9.1402463912964);
//				apiTracking.put(tSession,new Date(), 38.734748840332,-9.1401968002319);
//				apiTracking.put(tSession,new Date(), 38.734748840332,-9.1401968002319);
//
//				apiTracking.put(tSession,new Date(), 38.734741210938,-9.1400585174561);
//				apiTracking.put(tSession,new Date(), 38.734741210938,-9.1400585174561);
//				apiTracking.put(tSession,new Date(), 38.734756469727,-9.1399440765381);
//				
//				apiTracking.put(tSession,new Date(), 38.734756469727,-9.1399440765381);
//				apiTracking.put(tSession,new Date(), 38.734745025635,-9.139627456665);
//				apiTracking.put(tSession,new Date(), 38.734745025635,-9.139627456665);
//				
//				apiTracking.put(tSession,new Date(), 38.73482131958,-9.1395511627197);
//				apiTracking.put(tSession,new Date(), 38.73482131958,-9.1395511627197);
//				apiTracking.put(tSession,new Date(), 38.73477935791,-9.1393356323242);
//				
//				apiTracking.put(tSession,new Date(), 38.73477935791,-9.1393356323242);
//				apiTracking.put(tSession,new Date(), 38.73477935791,-9.1393356323242);
//				apiTracking.put(tSession,new Date(), 38.73477935791,-9.1393356323242);
//				
//				apiTracking.put(tSession,new Date(), 38.734550476074,-9.1389074325562);
//				apiTracking.put(tSession,new Date(), 38.734550476074,-9.1389074325562);
//				apiTracking.put(tSession,new Date(), 38.734497070312,-9.1388320922852);
//				
//				apiTracking.put(tSession,new Date(), 38.734497070312,-9.1388320922852);
//				apiTracking.put(tSession,new Date(), 38.734294891357,-9.1388339996338);
//				apiTracking.put(tSession,new Date(), 38.734294891357,-9.1388339996338);
//				
//				apiTracking.put(tSession,new Date(), 38.734134674072,-9.1387538909912);
//				apiTracking.put(tSession,new Date(), 38.734134674072,-9.1387538909912);
//				
//				apiTracking.put(tSession,new Date(), 38.733959197998,-9.1385116577148);
//				apiTracking.put(tSession,new Date(), 38.733959197998,-9.1385116577148);
//				
//				apiTracking.put(tSession,new Date(), 38.733745574951,-9.1383647918701);
//				apiTracking.put(tSession,new Date(), 38.733745574951,-9.1383647918701);
//				
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.1382150650024);
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.1382150650024);
//				
//				apiTracking.put(tSession,new Date(), 38.733531951904,-9.1381683349609);
//				apiTracking.put(tSession,new Date(), 38.733531951904,-9.1381683349609);
//				
//				apiTracking.put(tSession,new Date(), 38.733364105225,-9.137749671936);
//				apiTracking.put(tSession,new Date(), 38.733364105225,-9.137749671936);
//				
//				apiTracking.put(tSession,new Date(), 38.733333587646,-9.1375188827515);
//				apiTracking.put(tSession,new Date(), 38.733333587646,-9.1375188827515);
//				apiTracking.put(tSession,new Date(), 38.733516693115,-9.137508392334);
//				apiTracking.put(tSession,new Date(), 38.733516693115,-9.137508392334);
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.137396812439);
//				
//				apiTracking.put(tSession,new Date(), 38.733734130859,-9.1374416351318);
//				apiTracking.put(tSession,new Date(), 38.733734130859,-9.1374416351318);
//				apiTracking.put(tSession,new Date(), 38.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 38.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 38.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 38.733535766602,-9.1371507644653);
//				
//				apiTracking.put(tSession,new Date(), 38.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 38.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 38.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 38.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 38.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 38.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 38.733554840088,-9.1374053955078);
//				
//				apiTracking.put(tSession,new Date(), 38.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 38.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 38.733451843262,-9.1371011734009);
//				apiTracking.put(tSession,new Date(), 38.733451843262,-9.1371011734009);
//				apiTracking.put(tSession,new Date(), 38.733535766602,-9.1371507644653);
//			}
//			
//
//			Date date2 = new Date();
//			Long time = date2.getTime() - date1.getTime();
////			
//			System.out.println("done: " + time + "ms.");
			
//			++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			if(tSession != ""){
//				apiTracking.put(tSession,new Date(), 39.735374450684,-9.1406631469727);
//				apiTracking.put(tSession,new Date(), 39.735374450684,-9.1406631469727);
//
//				apiTracking.put(tSession,new Date(), 39.735172271729,-9.140739440918);
//				apiTracking.put(tSession,new Date(), 39.735172271729,-9.140739440918);
//
//				apiTracking.put(tSession,new Date(), 39.735282897949,-9.1403131484985);
//				apiTracking.put(tSession,new Date(), 39.735282897949,-9.1403131484985);
//				apiTracking.put(tSession,new Date(), 39.735118865967,-9.1405467987061);
//
//				apiTracking.put(tSession,new Date(), 39.735118865967,-9.1405467987061);
//				apiTracking.put(tSession,new Date(), 39.735328674316,-9.1404991149902);
//				apiTracking.put(tSession,new Date(), 39.735328674316,-9.1404991149902);
//
//				apiTracking.put(tSession,new Date(), 39.735210418701,-9.1404447555542);
//				apiTracking.put(tSession,new Date(), 39.735210418701,-9.1404447555542);
//				apiTracking.put(tSession,new Date(), 39.73490524292,-9.1402463912964);
//
//				apiTracking.put(tSession,new Date(), 39.73490524292,-9.1402463912964);
//				apiTracking.put(tSession,new Date(), 39.734748840332,-9.1401968002319);
//				apiTracking.put(tSession,new Date(), 39.734748840332,-9.1401968002319);
//
//				apiTracking.put(tSession,new Date(), 39.734741210938,-9.1400585174561);
//				apiTracking.put(tSession,new Date(), 39.734741210938,-9.1400585174561);
//				apiTracking.put(tSession,new Date(), 39.734756469727,-9.1399440765381);
//
//				apiTracking.put(tSession,new Date(), 39.734756469727,-9.1399440765381);
//				apiTracking.put(tSession,new Date(), 39.734745025635,-9.139627456665);
//				apiTracking.put(tSession,new Date(), 39.734745025635,-9.139627456665);

//				apiTracking.put(tSession,new Date(), 39.73482131958,-9.1395511627197);
//				apiTracking.put(tSession,new Date(), 39.73482131958,-9.1395511627197);
//				apiTracking.put(tSession,new Date(), 39.73477935791,-9.1393356323242);
//
//				apiTracking.put(tSession,new Date(), 39.73477935791,-9.1393356323242);
//				apiTracking.put(tSession,new Date(), 39.73477935791,-9.1393356323242);
//				apiTracking.put(tSession,new Date(), 39.73477935791,-9.1393356323242);
//
//				apiTracking.put(tSession,new Date(), 39.734550476074,-9.1389074325562);
//				apiTracking.put(tSession,new Date(), 39.734550476074,-9.1389074325562);
//				apiTracking.put(tSession,new Date(), 39.734497070312,-9.1388320922852);
//
//				apiTracking.put(tSession,new Date(), 39.734497070312,-9.1388320922852);
//				apiTracking.put(tSession,new Date(), 39.734294891357,-9.1388339996338);
//				apiTracking.put(tSession,new Date(), 39.734294891357,-9.1388339996338);
//
//				apiTracking.put(tSession,new Date(), 39.734134674072,-9.1387538909912);
//				apiTracking.put(tSession,new Date(), 39.734134674072,-9.1387538909912);
//
//				apiTracking.put(tSession,new Date(), 39.733959197998,-9.1385116577148);
//				apiTracking.put(tSession,new Date(), 39.733959197998,-9.1385116577148);
//
//				apiTracking.put(tSession,new Date(), 39.733745574951,-9.1383647918701);
//				apiTracking.put(tSession,new Date(), 39.733745574951,-9.1383647918701);
//
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.1382150650024);
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.1382150650024);
//
//				apiTracking.put(tSession,new Date(), 39.733531951904,-9.1381683349609);
//				apiTracking.put(tSession,new Date(), 39.733531951904,-9.1381683349609);
//
//				apiTracking.put(tSession,new Date(), 39.733364105225,-9.137749671936);
//				apiTracking.put(tSession,new Date(), 39.733364105225,-9.137749671936);
//
//				apiTracking.put(tSession,new Date(), 39.733333587646,-9.1375188827515);
//				apiTracking.put(tSession,new Date(), 39.733333587646,-9.1375188827515);
//				apiTracking.put(tSession,new Date(), 39.733516693115,-9.137508392334);
//				apiTracking.put(tSession,new Date(), 39.733516693115,-9.137508392334);
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.137396812439);
//
//				apiTracking.put(tSession,new Date(), 39.733734130859,-9.1374416351318);
//				apiTracking.put(tSession,new Date(), 39.733734130859,-9.1374416351318);
//				apiTracking.put(tSession,new Date(), 39.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 39.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 39.733535766602,-9.1371507644653);
//				apiTracking.put(tSession,new Date(), 39.733535766602,-9.1371507644653);
//
//				apiTracking.put(tSession,new Date(), 39.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 39.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 39.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 39.733467102051,-9.137243270874);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 39.7336769104,-9.137396812439);
//				apiTracking.put(tSession,new Date(), 39.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 39.733554840088,-9.1374053955078);
//
//				apiTracking.put(tSession,new Date(), 39.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 39.733554840088,-9.1374053955078);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733627319336,-9.1372213363647);
//				apiTracking.put(tSession,new Date(), 39.733451843262,-9.1371011734009);
//				apiTracking.put(tSession,new Date(), 39.733451843262,-9.1371011734009);
//				apiTracking.put(tSession,new Date(), 39.733535766602,-9.1371507644653);
//			}

//			System.out.println("done");
//			++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			
//			System.out.println("tSession: " + tSession);

			//display them
//			int count = 1;
//			for(String s : userSessions){
//				System.out.println(count++ + ": " + apiTracking.getSessionDetails(s));
//			}

			//attributes
//			Map<String,Object> attributes = new HashMap<>();
//			attributes.put("decline", 0.1);
//			attributes.put("mode", "walking");
////			
//			System.out.println("attributes created");
////			
//			//route creation
//			List<TraceVertex> routeForSubmission = new ArrayList<>();
//			routeForSubmission.add(new TraceVertex(18, 10));
//			Thread.sleep(100);
//			routeForSubmission.add(new TraceVertex(18, 22));
//			Thread.sleep(100);
//			routeForSubmission.add(new TraceVertex("beaconID4"));
//			Thread.sleep(100);
//			routeForSubmission.add(new TraceVertex("beaconID4"));
//			Thread.sleep(100);
//			routeForSubmission.add(new TraceVertex(18,20));
//
//			System.out.println("route created");
//			
			//route printing
//			System.out.println("routeForSubmission:");
//			for(TraceVertex v : routeForSubmission){
//				System.out.println(v.toString() + " Time: " + v.getDate().getTime());
//			}
//			System.out.println("#Points: " + routeForSubmission.size());
			
			//route submission
//			apiTracking.submitRoute(tSession, routeForSubmission);
			
//			System.out.println("route submitted");
			
//			apiTracking.removeSession("0.8623381698428202");
//			System.out.println("session deleted");
			
//			List<TraceVertex> route = apiTracking.getRouteBySession("");
//			List<TraceVertex> route = apiTracking.getRouteBySession(tSession);

//			System.out.println("route lookup complete");
//			
//			List<String> route = apiTracking.getTrajectory("0.9576968035667545");
//			List<String> route = apiTracking.getTrajectory(tSession);
//			List<TraceVertex> route = apiTracking.getRouteBySession("77df624092b948374cb82af706833693e6c1c17b3ad00eddf8f07126780396ec");
//			List<TraceVertex> route = apiTracking.getRouteBySession("18d10825bdb8efd2977b7fe44a1a2a1cdfa17d9658143dce15bb3b1a8fe76ef6");
//			
//			//route printing
//			System.out.println("Route:");
//			for(TraceVertex v : route){
//				System.out.println(v.toString());
//			}
//			System.out.println("#Points: " + route.size());
			
//			List<String> userSessions2 = apiTracking.getUserSessions("rodrigo",50);
//			List<String> userSessions2 = apiTracking.getAllSessions();
			
//			int count=0;
//			for(String s : userSessions2){
//				System.out.println(s);
//				count++;
//			}
//			System.out.println(count);
			
//			List<String> allSessions = apiTracking.getAllSessions();

//			apiTracking.put("abc", new Date(), 10, 10, attributes);

//			System.out.println(route);
			
//			System.out.println(userSessions2);
//			System.out.println(allSessions);
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++			
			
			
//			List<String> myList = new ArrayList<String>();
//	        myList.add("one");
//	        myList.add("two");
//	        myList.add("three");
//	        myList.add("four");
//	        myList.add("five");
//
//	        System.out.println("Inserted in 'order': ");
//	        printList(myList);
//	        System.out.println("\n");
//	        System.out.println("Inserted out of 'order': ");
//
//	        // Clear the list
//	        myList.clear();
//
//	        myList.add("four");
//	        myList.add("five");
//	        myList.add("one");
//	        myList.add("two");
//	        myList.add("three");
//
//	        printList(myList);
			
//			38.0001
			
//			List<String> list = TraceLocationMethods.getAdjacentGridIDs(38.733627319336);
//			
//			System.out.println("GPS: 38.733627319336");
//			System.out.println("gridID: " + TraceLocationMethods.getGridID(38.733627319336));
			
//			for(String s : list){
//				System.out.println(s);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return;
	}
	
//	private static void printList(List<String> myList) {
//		for (String string : myList) {
//			System.out.println(string);
//		}
//	}
}

