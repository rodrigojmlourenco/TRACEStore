package org.trace.DBAPI.data;

public class TraceBeaconID {

	private String _beaconID;
	
	public TraceBeaconID(){
		_beaconID = "-1ID";
	}
	public TraceBeaconID(String id){
		_beaconID = id;
	}
	public String getBeaconID() {
		return _beaconID;
	}
}

