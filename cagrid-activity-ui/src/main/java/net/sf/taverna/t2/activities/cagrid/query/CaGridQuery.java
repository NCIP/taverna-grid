package net.sf.taverna.t2.activities.cagrid.query;

import java.io.IOException;
import java.util.List;

import javax.wsdl.Operation;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;


import org.xml.sax.SAXException;

public class CaGridQuery extends ActivityQuery {
	
	// a CaGridQuery object corresponds to a wsdl
	private static Logger logger = Logger.getLogger(CaGridQuery.class);
	private final  ServiceQuery[] sq;
	
	public CaGridQuery(String url) {
		this(url,null);
	}
	public CaGridQuery(String url,ServiceQuery[] f_sq ) {
		super(url);
		sq = f_sq;
	}

	@Override
	public void doQuery() {
		//use url and sq
		try {
			String indexURL = getProperty();			
			List<CaGridService> services=CaGridServiceQueryUtility.load(indexURL, sq);
			
			//TODO addItem
			if(services!=null){
				for (CaGridService cs:services){
					List<String> operations = cs.getOperations();
					System.out.println("Adding service: "+ cs.getServiceName());
					for (String op : operations) {
						System.out.println("	Adding operation: "+ op );
						// an Activity item corresponds to an operation
						//services contains service metadata -- no wsdl parser is needed?
						//we can add a parser to parse it if we need more details on those services
						WSDLActivityItem item = new WSDLActivityItem();
						
						item.setOperation(op);
						//make use of "use" and "style" to facilitate metadata-based sorting
						
						item.setUse(op);
						//CaGrid Services are all DOCUMENT style
						item.setStyle("document");
						item.setUrl(cs.getServiceName()+"?wsdl");
						if(!cs.getResearchCenterName().equals("")){
							item.setResearchCenter(cs.getResearchCenterName());	
						}
						
						
						
						//an operation is an "item" to be added
						add(item);
						
					}
					
				}
				
			}
			
			
			System.out.println("Service Query Complete.");
			
		} catch (Exception e) {
			logger.warn("Unable to load index",e);
		}
		

	}

}
