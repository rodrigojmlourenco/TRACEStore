package org.trace.DBAPI.data;

import java.util.List;

//Created for parsing effects only

public class RouteBoundingBox {
	private double _minLat,_maxLat,_minLon,_maxLon;
	
	public RouteBoundingBox(double minLat, double maxLat, double minLon, double maxLon){
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = minLon;
		_maxLon = maxLon;
	}

	public double getMinLat() {
		return _minLat;
	}

	public double getMaxLat() {
		return _maxLat;
	}

	public double getMinLon() {
		return _minLon;
	}

	public double getMaxLon() {
		return _maxLon;
	}
	
	
	public double getSWLat() {
		return _minLat;
	}
	
	public double getSWLon() {
		return _minLon;
	}

	public double getNELat() {
		return _maxLat;
	}

	public double getNELon() {
		return _maxLon;
	}
	
}
