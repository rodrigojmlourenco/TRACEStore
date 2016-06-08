package org.trace.DBAPI;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;

public class TraceDB {

	Cluster _cluster;
	Client _client;

	public TraceDB(){
		_cluster = null;
		_client = null;
	}

	public boolean initialize(){
		try {
			_cluster = Cluster.open("./src/main/resources/remote2.yaml");
			_client = _cluster.connect();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(_cluster == null || _client == null){
			return false;
		}

		return true;
	}
	
	public boolean initializePath(String path){
		try {
			_cluster = Cluster.open(path);
			_client = _cluster.connect();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(_cluster == null || _client == null){
			return false;
		}

		return true;
	}
	
	public boolean initialize(String config){
		try {
			
			_cluster = Cluster.open("./src/main/resources/" + config + ".yaml");
			
//			if(config == "local"){
//				_cluster = Cluster.open("./src/main/resources/local.yaml");
//			}else{
//				_cluster = Cluster.open("./src/main/resources/remote.yaml");
//			}
			_client = _cluster.connect();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(_cluster == null || _client == null){
			return false;
		}

		return true;
	}

	public void close(){
		_client.close();
		_cluster.close();
//		
		//Workaround of a bug in Tinkerpop Driver 3.1.0-incubating, going to be fixed soon
		fixThreadLeakInGremlinCluster(_cluster);
		
		_client = null;
		_cluster = null;
	}

	//Fix gremlin cluster thread leak
	private static void fixThreadLeakInGremlinCluster(Cluster cluster) {
		try {
			Method executorAccessor = Cluster.class.getDeclaredMethod("executor");
			executorAccessor.setAccessible(true);
			ScheduledExecutorService executor = (ScheduledExecutorService) executorAccessor.invoke(cluster);
			executor.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Client getClient() {
		return _client;
	}
	
	public Cluster getCluster() {
		return _cluster;
	}
	
}
