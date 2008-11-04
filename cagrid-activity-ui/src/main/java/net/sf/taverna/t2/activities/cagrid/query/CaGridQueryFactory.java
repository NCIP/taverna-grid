package net.sf.taverna.t2.activities.cagrid.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;

public class CaGridQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new CaGridQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		//TODO: change this line?
		return "taverna.defaultwsdl";
	}

	@Override
	public AddQueryActionHandler getAddQueryActionHandler() {
		return new CaGridAddQueryActionHandler();
	}

	@Override
	public boolean hasAddQueryActionHandler() {
		return true;
	}
	
	

}
