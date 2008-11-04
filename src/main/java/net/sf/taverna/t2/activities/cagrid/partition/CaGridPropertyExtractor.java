package net.sf.taverna.t2.activities.cagrid.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.cagrid.query.WSDLActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class CaGridPropertyExtractor implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		//TODO use CaGridActivityItem or WSDLActivityItem
		if (target instanceof WSDLActivityItem) {
			WSDLActivityItem item = (WSDLActivityItem)target;
			map.put("type", item.getType());
			map.put("use", item.getUse());
			map.put("style", item.getStyle());
			map.put("operation", item.getOperation());
			map.put("url",item.getUrl());
			map.put("provider", item.getResearchCenter());
		}
		return map;
	}

}
