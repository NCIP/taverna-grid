package net.sf.taverna.t2.activities.cagrid.servicedescriptions;

public class CaGridServiceQuery {
	
	public String queryCriteria;
	public String queryValue;
	
	public CaGridServiceQuery(String queryCriteria, String queryValue){
		this.queryCriteria = queryCriteria;
		this.queryValue = queryValue;
	}
	
}
