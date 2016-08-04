package org.trace.inesc.store.storage.graph;

import org.apache.tinkerpop.gremlin.driver.Client;

public class DBPlannerAPI extends DBAPI{
	public DBPlannerAPI(Client client){
		super(client);
	}
}
