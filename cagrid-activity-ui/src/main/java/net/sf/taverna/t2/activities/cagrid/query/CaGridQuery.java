package net.sf.taverna.t2.activities.cagrid.query;

//import java.io.IOException;
import java.util.List;

//import javax.wsdl.Operation;
//import javax.wsdl.WSDLException;
//import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.partition.ActivityQuery;
//import net.sf.taverna.wsdl.parser.UnknownOperationException;
//import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;

//import org.xml.sax.SAXException;

public class CaGridQuery extends ActivityQuery {
	
	private static Logger logger = Logger.getLogger(CaGridQuery.class);
	private final  ServiceQuery[] sq; // query to be passed to Index Service to search for available matching caGrid services
	
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
			String indexURL = getProperty();	// URL of Index Service		
			List<CaGridService> services=CaGridServiceQueryUtility.load(indexURL, sq);
			
			if(services!=null){
				for (CaGridService cs:services){
					List<String> operations = cs.getOperations();
					System.out.println("Adding service: "+ cs.getServiceName());
					for (String op : operations) {
						System.out.println("	Adding operation: "+ op );
						// An ActivityItem corresponds to an operation
						// services contains service metadata -- no wsdl parser is needed?
						// we can add a parser to parse it if we need more details on those services
						CaGridActivityItem item = new CaGridActivityItem();
						
						item.setOperation(op);
						
						//make use of "use" and "style" to facilitate metadata-based sorting
						item.setUse(op);
						//CaGrid services are all DOCUMENT style
						item.setStyle("document");
						
						item.setUrl(cs.getServiceName()+"?wsdl");
						
						if(!cs.getResearchCenterName().equals("")){
							item.setResearchCenter(cs.getResearchCenterName());	
						}
			
						// Invoke getServiceSecurityMetadata() method on the service to
						// determine the operation's security requirements, if any
						item.setSecure(false);
						item.setGSITransport(null);
						item.setGSIAnonymouos(null);
						item.setAuthorisation(null);
						item.setGSISecureConversation(null);
						item.setGSISecureMessage(null);
						item.setGSIMode(null);
						// If secure - now is a good place to ask for a caGrid username/password
						// to obtain proxy key and certificate						
						
						// One CaGridActivityItem to be added to the activity/service
						// palette corresponds to one operation of a WSDL-based web service
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
