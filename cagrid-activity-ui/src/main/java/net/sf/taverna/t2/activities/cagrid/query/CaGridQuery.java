package net.sf.taverna.t2.activities.cagrid.query;

//import java.io.IOException;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import gov.nih.nci.cagrid.metadata.security.CommunicationMechanism;
import gov.nih.nci.cagrid.metadata.security.Operation;
import gov.nih.nci.cagrid.metadata.security.ProtectionLevelType;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadataOperations;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;

//import javax.wsdl.Operation;
//import javax.wsdl.WSDLException;
//import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.partition.ActivityQuery;
//import net.sf.taverna.wsdl.parser.UnknownOperationException;
//import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.types.URI.MalformedURIException;
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
				
				for (CaGridService caGridService:services){
					List<String> operations = caGridService.getOperations();
					System.out.println("Adding service: "+ caGridService.getServiceName());
					
					// Get security metadata for all operations/methods of this service
					// by invoking getServiceSecurityMetadata() method on the service
					ServiceSecurityClient ssc = null;
					try {
						ssc = new ServiceSecurityClient(caGridService.getServiceWSDLLocation());
					} catch (MalformedURIException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ServiceSecurityMetadata securityMetadata = null;
					if (ssc != null) {
						try {
							securityMetadata = ssc.getServiceSecurityMetadata();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// Get all secure OperationS mapped to their name
					Map<String, Operation> secureOperationsMap = new HashMap<String, Operation>();
					ServiceSecurityMetadataOperations ssmo = null; 
					if (securityMetadata != null){
						ssmo = securityMetadata.getOperations(); // all secure operations of the service?
					}
					if (ssmo != null) {
						Operation[] ops = ssmo.getOperation();
						if (ops != null) {
							for (int i = 0; i < ops.length; i++) {
								String lowerMethodName = ops[i].getName().substring(0, 1)
										.toLowerCase()
										+ ops[i].getName().substring(1);
								secureOperationsMap.put(lowerMethodName, ops[i]);
							}
						}
					}
					
					for (String operation : operations) {
						System.out.println("	Adding operation: "+ operation );
						// An ActivityItem corresponds to an operation
						// services contains service metadata -- no wsdl parser is needed?
						// we can add a parser to parse it if we need more details on those services
						CaGridActivityItem item = new CaGridActivityItem();
						
						item.setOperation(operation);
						
						//make use of "use" and "style" to facilitate metadata-based sorting
						item.setUse(operation);
						//CaGrid services are all DOCUMENT style
						item.setStyle("document");
						
						item.setUrl(caGridService.getServiceName()+"?wsdl");
						
						if(!caGridService.getResearchCenterName().equals("")){
							item.setResearchCenter(caGridService.getResearchCenterName());	
						}
			
						// Check if operation is secure						
						if (secureOperationsMap.containsKey(operation)) { // is our operation among the secure ones?
							try{
								item.setSecure(true);
								configureSecurity(securityMetadata, caGridService, secureOperationsMap.get(operation), item);
								
								// Now is a good place to ask for a caGrid username/password
								// to obtain proxy key and certificate
							}
							catch(RemoteException rex){
								logger.error("Error getting user's proxy for operation "
												+ operation
												+ " of service: "
												+ caGridService
														.getServiceName()
												+ "?wsdl. Skipping this operation.");
								rex.printStackTrace();
								continue;
							}		
						} else { // operation is not secure
							item.setSecure(false);
						}	

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
	
	/**
	 * Configures security properties for the CaGridActivityItem (i.e. its operation) 
	 * based on the security metadata obtained from the getServiceSecurityMetadata() 
	 * method on the caGridService.
	 * 
	 * @param securityMetadata
	 * @param caGridService
	 * @param operation
	 * @param item
	 * @throws RemoteException
	 */
	public static void configureSecurity(ServiceSecurityMetadata securityMetadata,
			CaGridService caGridService, Operation operation,
			CaGridActivityItem item) throws RemoteException {

		boolean anonymousPrefered = true;
		
		boolean https = false;
		if (caGridService.getServiceWSDLLocation().toLowerCase().startsWith("https")) {
			https = true;
		}
			
		CommunicationMechanism mechanism = operation.getCommunicationMechanism();

		boolean anonymousAllowed = true;
		boolean authorizationAllowed = true;
		boolean delegationAllowed = true;
		boolean credentialsAllowed = true;
		
		GlobusCredential proxy = null;
		
		Authorization authorization= null;
		
		String delegationMode = null;

		if ((https) && (mechanism.getGSITransport() != null)) {
			ProtectionLevelType level = mechanism.getGSITransport().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					item.setGSITransport(org.globus.wsrf.security.Constants.ENCRYPTION);
				} else {
					item.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				item.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
			}
			delegationAllowed = false;

		} else if (https) {
			item.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
			delegationAllowed = false;
		} else if (mechanism.getGSISecureConversation() != null) {
			ProtectionLevelType level = mechanism.getGSISecureConversation().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					item.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);

				} else {
					item.setGSISecureConversation(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				item.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);
			}

		} else if (mechanism.getGSISecureMessage() != null) {
			ProtectionLevelType level = mechanism.getGSISecureMessage().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					item.setGSISecureMessage(org.globus.wsrf.security.Constants.ENCRYPTION);
				} else {
					item.setGSISecureMessage(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				item.setGSISecureMessage(org.globus.wsrf.security.Constants.ENCRYPTION);
			}
			delegationAllowed = false;
			anonymousAllowed = false;
		} else {
			anonymousAllowed = false;
			authorizationAllowed = false;
			delegationAllowed = false;
			credentialsAllowed = false;
		}

		if ((anonymousAllowed) && (mechanism.isAnonymousPermitted()) && anonymousPrefered) {
			item.setGSIAnonymouos(Boolean.TRUE);
		} else if ((credentialsAllowed) && (proxy != null)) {
			try {
				org.ietf.jgss.GSSCredential gss = new org.globus.gsi.gssapi.GlobusGSSCredentialImpl(proxy,
					org.ietf.jgss.GSSCredential.INITIATE_AND_ACCEPT);
				item.setGSICredentials(gss);
			} catch (org.ietf.jgss.GSSException ex) {
				throw new RemoteException(ex.getMessage());
			}
		}

		if (authorizationAllowed) {
			if (authorization == null) {
				item.setAuthorisation(NoAuthorization.getInstance());
			} else {
				item.setAuthorisation(authorization);
			}
		}
		if (delegationAllowed) {
			if (delegationMode != null) {
				item.setGSIMode(delegationMode);
			}
		}

	}

}
