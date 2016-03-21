package org.trace.DBAPI.data;

import java.util.Date;

public class TraceSession {
	
	private String _sessionID;
	private Date _date;
	private String _dateString;
	private String _vertexID;

	
	public TraceSession(){
		_sessionID = Math.random() + "";
		_date = new Date();
		_dateString = null;
	}
	
	public TraceSession(String sessionID, String date, String vertexID){
		_sessionID = sessionID;
		_dateString = date;
		_vertexID = vertexID;
	}

	public String getSessionID() {
		return _sessionID;
	}
	
	public Date getDate() {
		return _date;
	}
	
	public String getDateString(){
		
		if(_dateString == null){
			return _date.toString();
		}
		
		return _dateString;
	}
	
	public String getVertexID() {
		return _vertexID;
	}
	
	public void setVertexID(String vertexID){
		_vertexID = vertexID;
	}
}
