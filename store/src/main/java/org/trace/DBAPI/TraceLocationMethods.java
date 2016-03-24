package org.trace.DBAPI;

/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  latitude/longitude of those points). It is being used to calculate     :*/
/*::  the distance between two locations using GeoDataSource (TM) prodducts  :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    South latitudes are negative, east longitudes are positive           :*/
/*::                                                                         :*/
/*::  Passed to function:                                                    :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles (default)                         :*/
/*::                  'K' is kilometers                                      :*/
/*::                  'N' is nautical miles                                  :*/
/*::  Worldwide cities and other features databases with latitude longitude  :*/
/*::  are available at http://www.geodatasource.com                          :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@geodatasource.com                  :*/
/*::                                                                         :*/
/*::  Official Web site: http://www.geodatasource.com                        :*/
/*::                                                                         :*/
/*::           GeoDataSource.com (C) All Rights Reserved 2015                :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//38.0001


import java.util.*;

import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.codehaus.groovy.transform.LazyASTTransformation;
import org.trace.DBAPI.data.TraceVertex;

import java.lang.*;
import java.io.*;

class TraceLocationMethods {

	//	protected static final double MAX_DISTANCE = 0.016; 

	//	public static void main (String[] args) throws java.lang.Exception
	//	{
	//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
	//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
	//		System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
	//	}

	public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		
//		System.out.println("lat1:" + lat1 + " lon1:" + lon1 + " lat2:" + lat2 + " long2:" + lon2);
		
		if(lat1 == lat2 && lon1 == lon2){
			return 0;
		}
		
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	public static double midPoint(double p1, double p2){
		return ((p1 + p2) / 2);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}


	public static String getGridID(double coord){
		int intCoord = (int) (coord * 10000);
		double gridID = intCoord / 10000.0;

		return "" + gridID;
	}

	public static List<String> getAdjacentGridIDs(double coord){
		List<String> adjacentGridIDs = new ArrayList<>();

		int intCoord = (int) (coord * 10000);
		int intCoord1 = intCoord - 1;
		int intCoord2 = intCoord + 1;

		adjacentGridIDs.add("" + (intCoord1 / 10000.0));
		adjacentGridIDs.add("" + (intCoord / 10000.0));
		adjacentGridIDs.add("" + (intCoord2 / 10000.0));

		return adjacentGridIDs;
	}

	public static List<TraceVertex> splitRoad(String p1, double lat1, double lon1, String p2, double lat2, double lon2, double distance){
		List<TraceVertex> roadVertices = new ArrayList<>();

		if(distance(lat1, lon1, lat2, lon2, "K") > distance){
			double lat3 = TraceLocationMethods.midPoint(lat1, lat2);
			double lon3 = TraceLocationMethods.midPoint(lon1, lon2);
			String p3 = "" + lat3 + "_" + lon3;

			List<TraceVertex> newRoadVerticesLeft = splitRoadAux(p1, lat1, lon1, p3, lat3, lon3, distance);
			List<TraceVertex> newRoadVerticesRight = splitRoadAux(p3, lat3, lon3, p2, lat2, lon2, distance);

			for(TraceVertex v : newRoadVerticesLeft){
				if(!roadVertices.contains(v)){
					roadVertices.add(v);
				}
			}
			for(TraceVertex v : newRoadVerticesRight){
				if(!roadVertices.contains(v)){
					roadVertices.add(v);
				}
			}
		}else{
			roadVertices.add(new TraceVertex(p2,lat2,lon2));
		}
		return roadVertices;
	}

	private static List<TraceVertex> splitRoadAux(String p1, double lat1, double lon1, String p2, double lat2, double lon2, double distance){
		List<TraceVertex> roadVertices = new ArrayList<>();

		roadVertices.add(new TraceVertex(p1, lat1, lon1));
		
		if(distance(lat1, lon1, lat2, lon2, "K") > distance){
			double lat3 = TraceLocationMethods.midPoint(lat1, lat2);
			double lon3 = TraceLocationMethods.midPoint(lon1, lon2);
			String p3 = "" + lat3 + "_" + lon3;

			List<TraceVertex> newRoadVerticesLeft = splitRoadAux(p1, lat1, lon1, p3, lat3, lon3, distance);
			List<TraceVertex> newRoadVerticesRight = splitRoadAux(p3, lat3, lon3, p2, lat2, lon2, distance);

			for(TraceVertex v : newRoadVerticesLeft){
				roadVertices.add(v);
			}

			for(TraceVertex v : newRoadVerticesRight){
				roadVertices.add(v);
			}
		}else{
			roadVertices.add(new TraceVertex(p2,lat2,lon2));
		}

		return roadVertices;
	}
	
	public static double routeTotalDistance(List<TraceVertex> route){
		double totalDistance = 0;
		
		TraceVertex lastVertice = null;
		for(TraceVertex v : route){
			if(lastVertice != null){
//				double d = distance(lastVertice.getLatitude(), lastVertice.getLongitude(), v.getLatitude(), v.getLongitude(), "K");
//				System.out.println("d:" + d);
				totalDistance+=distance(lastVertice.getLatitude(), lastVertice.getLongitude(), v.getLatitude(), v.getLongitude(), "K");;
//				System.out.println("totalDistance:" + totalDistance);
			}
			lastVertice = v;
		}
		
		return totalDistance;
	}
}