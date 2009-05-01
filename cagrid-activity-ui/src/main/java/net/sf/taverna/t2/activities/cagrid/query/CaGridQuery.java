/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.cagrid.query;

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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import gov.nih.nci.cagrid.opensaml.SAMLAssertion;
import org.cagrid.gaards.authentication.BasicAuthentication;
import org.cagrid.gaards.authentication.client.AuthenticationClient;
import org.cagrid.gaards.dorian.client.GridUserClient;
import org.cagrid.gaards.dorian.federation.CertificateLifetime;
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

@SuppressWarnings("serial")
public class CaGridQuery extends ActivityQuery {
	
	private static Logger logger = Logger.getLogger(CaGridQuery.class);
		
	// URL of Index Service	
	private String indexServiceURL; 
	
	// Map of Authentication Services corresponding to each of the Index Services 
	// (should be a list of Authentication Services for each Index Service really)
	private Map<String,String> authenticationServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-auth.nci.nih.gov:8443/wsrf/services/cagrid/AuthenticationService");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};
	
	// Map of Dorian Services corresponding to each of the Index Services
	// (should be a list of Dorian Services for each Index Service really)
	private Map<String,String> dorianServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};	
	
	private final  ServiceQuery[] sq; // query to be passed to Index Service to search for available matching caGrid services
	
	public CaGridQuery(String indexServiceURL) {
		this(indexServiceURL, null);
	}
	
	public CaGridQuery(String indexServiceURL,ServiceQuery[] sq) {
		super(indexServiceURL);
			
		this.indexServiceURL = indexServiceURL;
		this.sq = sq;
	}

	@Override
	public void doQuery() {
		List<CaGridService> services = null;

		try {
			services = CaGridServiceQueryUtility.load(indexServiceURL, sq);
		}
		catch(Exception ex){
			logger.warn("Unable to load service from the index. Index Service service used: " + indexServiceURL);
			ex.printStackTrace();
		}
		
		if(services != null){
			logger.info("Discovered "+ services.size() + " caGrid services.");
			for (CaGridService caGridService:services){
				List<String> operationNames = caGridService.getOperations();
				logger.info("Adding service: "+ caGridService.getServiceName());
				
				// Some caGrid services requiring https have a weird CN in their certificates - 
				// instead of CN=<HOSTNAME> they have CN="host/"+<HOSTNAME>, i.e. string 
				// "host/" prepended so we have to tell Java's SSL to accept these hostnames as well.
				// This is not very good at is sets this hostname verifier across all 
				// https connections created in the JVM from now on, but solves the problem 
				// with such caGrid services.
				// This should be a static block somewhere
				if (caGridService.getServiceName().toLowerCase().startsWith("https")){
					HostnameVerifier hv = new HostnameVerifier() {
						public boolean verify(String hostName, SSLSession session) {
							String hostNameFromCertificate = null;
							try {
								hostNameFromCertificate = session.getPeerPrincipal()
										.getName().substring(3,
												session.getPeerPrincipal().getName()
														.indexOf(','));
							} catch (SSLPeerUnverifiedException e) {
								e.printStackTrace();
								return false;
							}
							logger.info("Hostname verifier: host from url: " + hostName + " vs. host from certificate: "+ hostNameFromCertificate);
							return (hostName.equals(hostNameFromCertificate) || ("host/"+hostName)
									.equals(hostNameFromCertificate));
						}
					};
					HttpsURLConnection.setDefaultHostnameVerifier(hv);
				}
				
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
				// Get all secure OperationS of the service which security properties differ from the 
				// default security properties for the service itself and map them to their names.
				// Only operations which security properties are different from those of the service itself 
				// will be detected here - whether because they require more stringent or more loose security.
				Map<String, Operation> secureOperationsMap = new HashMap<String, Operation>();
				ServiceSecurityMetadataOperations ssmo = null; 
				if (securityMetadata != null){
					ssmo = securityMetadata.getOperations(); // all operations of the service requiring GSI security properties
				}
				if (ssmo != null) {
					Operation[] ops = ssmo.getOperation();
					if (ops != null) {
						logger.info("Discovered " + ops.length + " operation(s) of the service that require(s) Globus GSI security.");
						for (int i = 0; i < ops.length; i++) {
							/*System.out.println("Secure operation name: " + ops[i].getName());
							String lowerMethodName = ops[i].getName().substring(0, 1)
									.toLowerCase()
									+ ops[i].getName().substring(1);
							secureOperationsMap.put(lowerMethodName, ops[i]);
							System.out.println("Lowercase secure operation name: " + lowerMethodName);*/
							secureOperationsMap.put(ops[i].getName(), ops[i]);
						}
					}
				}
										
				for (String operation : operationNames) {
					logger.info("Adding operation: "+ operation );
					// An ActivityItem corresponds to an operation.
					// Service contains service metadata -- no wsdl parser is needed?
					// We can add a parser to parse it if we need more details on those services
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

					CommunicationMechanism serviceDefaultCommunicationMechanism = securityMetadata.getDefaultCommunicationMechanism();
					CommunicationMechanism communicationMechanism = null;
					if (secureOperationsMap.containsKey(operation)) {
						Operation op = (Operation) secureOperationsMap.get(operation);
						communicationMechanism = op.getCommunicationMechanism(); // specific for this operation, may differ from service default
					} else {
						communicationMechanism = serviceDefaultCommunicationMechanism;
					}
					
					// Configure security properties for the operation, if any
					try{
						logger.info("Configuring Globus GSI security for operation: "+ operation);
						configureSecurity(caGridService,
								communicationMechanism,
								indexServiceURL,
								authenticationServicesMap.get(indexServiceURL), 
								dorianServicesMap.get(indexServiceURL), 
								item);
					}
					catch(Exception ex){
						logger.error(ex.getMessage() + ". Skipping this operation.");
						ex.printStackTrace();
						continue;
					}

					// One CaGridActivityItem to be added to the activity/service
					// palette corresponds to one operation of a WSDL-based web service
					try{
						add(item);
					}
					catch(RuntimeException rex){
						logger.error("Unexpected error while adding operation " + operation + " to services panel. Skipping this operation.");
						rex.printStackTrace();
					}
				}
			}
			
		}	
		logger.info("CaGrid Service Query Complete.");
	}
	
	/**
	 * Configures security properties for the CaGridActivityItem (i.e. its operation) 
	 * based on the security metadata obtained from the getServiceSecurityMetadata() 
	 * method on the caGridService.
	 * 
	 */
	public static void configureSecurity(CaGridService caGridService,
			CommunicationMechanism communicationMechanism, String indexServiceURL, String authenticationServiceURL,
			String dorianServiceURL, CaGridActivityItem item)
			throws Exception {

		boolean anonymousPrefered = true;
		
		boolean https = caGridService.getServiceWSDLLocation().toLowerCase().startsWith("https");
			
		boolean anonymousAllowed = true;
		boolean authorizationAllowed = true;
		boolean delegationAllowed = true;
		boolean credentialsAllowed = true;
		
		GlobusCredential proxy = null;
		
		Authorization authorization= null;
		
		String delegationMode = null;

		item.setSecure(true); // set initially as secure - will change later on if we do not find any security properties
		item.setIndexServiceURL(indexServiceURL);
		item.setDefaultAuthNServiceURL(authenticationServiceURL);
		item.setDefaultDorianServiceURL(dorianServiceURL);
		
		if ((https) && (communicationMechanism.getGSITransport() != null)) {
			ProtectionLevelType level = communicationMechanism.getGSITransport().getProtectionLevel();
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
		} else if (communicationMechanism.getGSISecureConversation() != null) {
			ProtectionLevelType level = communicationMechanism.getGSISecureConversation().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					item.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);

				} else {
					item.setGSISecureConversation(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				item.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);
			}

		} else if (communicationMechanism.getGSISecureMessage() != null) {
			ProtectionLevelType level = communicationMechanism.getGSISecureMessage().getProtectionLevel();
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
			/*
			anonymousAllowed = false;
			authorizationAllowed = false;
			delegationAllowed = false;
			credentialsAllowed = false;
			*/
			item.setSecure(false);
			return;
		}
	
		if ((anonymousAllowed) && (communicationMechanism.isAnonymousPermitted()) && anonymousPrefered) {
			item.setGSIAnonymouos(Boolean.TRUE);
		} else if (credentialsAllowed) {
			// Get the proxy certificate - hardcoded username and password - proxy should be created only once
			// for all services belonging to the same caGrid!
			try{
				BasicAuthentication auth = new BasicAuthentication();
		        auth.setUserId("anenadic");
		        auth.setPassword("m^s7a*kpT302");

		        // Authenticate to the Authentication Service using the basic authN credential
				System.out.println("Trying to authenticated the user with AuthN Service: " + authenticationServiceURL);
		        AuthenticationClient authClient = new AuthenticationClient(authenticationServiceURL);
		        SAMLAssertion saml = authClient.authenticate(auth);
				logger.info("Authenticated the user with AuthN Service: " + authenticationServiceURL);
				System.out.println("Authenticated the user with AuthN Service: " + authenticationServiceURL);

		        // Set requested Grid Credential lifetime (12 hours)
		        CertificateLifetime lifetime = new CertificateLifetime();
		        lifetime.setHours(12);

		        // Request PKI/Grid Credential
		        System.out.println("Trying to obtain user's proxy from Dorian: "+ dorianServiceURL);					
		        GridUserClient dorian = new GridUserClient(dorianServiceURL);
		        proxy = dorian.requestUserCertificate(saml, lifetime);
		        logger.info("Obtained user's proxy from Dorian: "+ dorianServiceURL);	
		        System.out.println("Obtained user's proxy from Dorian: "+ dorianServiceURL);					
			}
			catch(Exception ex){
				logger.error("Error occured while trying to authenticate user with caGrid", ex);
				throw new Exception("Error occured while trying to authenticate user with caGrid", ex);
			}
			
			try {
				System.out.println("Trying to create GSSCredentials from the proxy.");
				org.ietf.jgss.GSSCredential gss = new org.globus.gsi.gssapi.GlobusGSSCredentialImpl(proxy,
					org.ietf.jgss.GSSCredential.INITIATE_AND_ACCEPT);
				logger.info("Created GSSCredentials from the proxy.");
				System.out.println("Created GSSCredentials from the proxy.");
				item.setGSICredentials(gss);
				item.setProxy(proxy);
			} catch (org.ietf.jgss.GSSException ex) {
				throw new Exception("Failed to create GSSCredentials with the user's proxy", ex);
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
