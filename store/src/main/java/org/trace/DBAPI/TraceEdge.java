package org.trace.DBAPI;

import java.util.HashMap;
import java.util.Map;

public class TraceEdge {
	
	private String _aVertex;
	private String _bVertex;
	private String _name;
	private Map<String, Object> _properties;

	public TraceEdge(String a, String b){
		_aVertex = a;
		_bVertex = b;
		_name = "unSet";
		_properties = new HashMap<>();
	}
	
	public TraceEdge(String name, String a, String b){
		_aVertex = a;
		_bVertex = b;
		_name = name;
		_properties = new HashMap<>();
	}
	
	public TraceEdge(String name, String a, String b, Map<String, Object> properties){
		_aVertex = a;
		_bVertex = b;
		_name = name;
		_properties = properties;
	}
	
	public TraceEdge(String a, String b, Map<String, Object> properties){
		_aVertex = a;
		_bVertex = b;
		_name = "unSet";
		_properties = properties;
	}

	public String getAVertex() {
		return _aVertex;
	}

	public void setAVertex(String aVertex) {
		_aVertex = aVertex;
	}

	public String getBVertex() {
		return _bVertex;
	}

	public void setBVertex(String bVertex) {
		_bVertex = bVertex;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public Map<String, Object> getProperties() {
		return _properties;
	}

	public void setProperties(Map<String, Object> properties) {
		_properties = properties;
	}
	
	
}
