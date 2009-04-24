/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
 * Copyright (C) 2009 The University of Chicago
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;

import org.apache.axis.types.URI.MalformedURIException;
import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.junit.Test;

public class CaGridSecurityTest {

	//@Ignore
	@Test
	public void testSecurityMetadata(){
		
		// caGrid
		String authenticationServiceURL = "https://cagrid-auth.nci.nih.gov:8443/wsrf/services/cagrid/AuthenticationService";
		// Training caGrid
		//String authenticationServiceURL = "https://cagrid-auth.nci.nih.gov:8443/wsrf/services/cagrid/AuthenticationService";

		// caGrid
		String dorianServiceURL = "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian";
		// Training caGrid
		//String dorianServiceURL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";
		
		//System.out.println("org.globus.wsrf.security.Constants.ENCRYPTION: " + org.globus.wsrf.security.Constants.ENCRYPTION);
		//System.out.println("org.globus.wsrf.security.Constants.SIGNATURE: " + org.globus.wsrf.security.Constants.SIGNATURE);

		// caGrid
		//String serviceURL = "http://crchbioinfo.org:18080/wsrf/services/cagrid/CaArraySvc";
		//String serviceURL = "http://scigridserver.arc.georgetown.edu:18080/wsrf/services/cagrid/CaArraySvc";
		// Causes java.io.IOException: HTTPS hostname wrong
		//String serviceURL = "https://cagrid-gts-master.nci.nih.gov:8443/wsrf/services/cagrid/GTS";
		
		//String serviceURL = "https://tissueinventory.cabig.upmc.edu:8443/wsrf/services/cagrid/CaTissueSuite";
		// Training caGrid
		//String serviceURL = "https://globalmodelexchange.training.cagrid.org:8443/wsrf/services/cagrid/GlobalModelExchange";
		//String serviceURL = "https://workflow-bpel.training.cagrid.org:8443/wsrf/services/cagrid/WorkflowFactoryService";
		String serviceURL = "https://cspool94.cs.man.ac.uk:8443/wsrf/services/cagrid/SecureHelloWorldService";

		CaGridService caGridService = new CaGridService(serviceURL+"?wsdl", serviceURL);

		// Get the service's operations
		WSDLParser parser = null;
		List<javax.wsdl.Operation> operations = new ArrayList<javax.wsdl.Operation>(); 
		/*
		// Check first if service's url starts with https - in this case we first
		// have to load the service's certificate chain into Taverna's truststore
		// otherwise WSDLPArser will fail when trying to obtain the wsdl document
		if (serviceURL.toLowerCase().startsWith("https")){
			try {
				File keystoreFile = new File("/Users/alex/cagrid.truststore");				
				KeyStore keyStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				char[] password = "passwd".toCharArray();
				if (!keystoreFile.exists()){
					keystoreFile.createNewFile();
					keyStore.load(null, null);
				}
				else{
					// Load the keystore
					keyStore.load(new BufferedInputStream(new FileInputStream(
							keystoreFile)), password);
				}

				URL url = new URL(serviceURL + "?wsdl");

				// Create a connection
				HttpsURLConnection con = (HttpsURLConnection) url
						.openConnection();
				con.connect();
				Certificate certs[] = con.getServerCertificates();

				// Import the certificate chain (i.e. CA's certificates)
				for (int i = certs.length - 1; i > 0; i--) {
					keyStore.setCertificateEntry("", certs[i]);
				}
				// Import the service's certificate itself - the first one in
				// the chain
				keyStore.setCertificateEntry(serviceURL + "?wsdl", certs[0]);

				// Save the keystore
				keyStore.store(new BufferedOutputStream(new FileOutputStream(
						keystoreFile)), password);

			}
			catch(Exception ex){
				System.out.println("Failed to store service's certificate chain in the truststore.");
				ex.printStackTrace();
				return;
			}
		}
		*/
		/*if (serviceURL.toLowerCase().startsWith("https")){ // Set the truststore so we can open an https connection to the service
			System.out.println("Setting avax.net.ssl.trustStore property.");
			System.setProperty("javax.net.ssl.trustStore", "/Users/alex/Work/caGrid/trainingCagridTruststore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword", "m^s7a*");
		}*/
		
		// Some caGrid services have a weird CN in their certificates - 
		// instead of CN=<HOSTNAME> they have CN="host/"+<HOSTNAME>, i.e. string 
		// "host/" preprended so we have to tell Java's SSL to accept these hostnames as well.
		// This is not very good but solves the problem with these caGrid services.
		if (serviceURL.toLowerCase().startsWith("https")){
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String hostName, SSLSession session) {
					
					String hostNameFromCertificate = null;
					try {
						hostNameFromCertificate = session.getPeerPrincipal()
								.getName().substring(3,
										session.getPeerPrincipal().getName()
												.indexOf(','));
					} catch (SSLPeerUnverifiedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Host from url: " + hostName + " vs. host from certificate: "+ hostNameFromCertificate);
					System.out.println();
					return (hostName.equals(hostNameFromCertificate) || ("host/"+hostName)
							.equals(hostNameFromCertificate));
				}
			};
			// This will set the HostnameVerifies across all https connections 
			// created in the VM from now on!!!
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		}
		try {
			parser = new WSDLParser(serviceURL + "?wsdl");
			operations = parser.getOperations();
		}
		catch(Exception ex){
			System.out.println("There was an error with the wsdl: "+ serviceURL + "?wsdl");
			ex.printStackTrace();
		}
		
		for (javax.wsdl.Operation operation : operations){
			caGridService.addOperation(operation.getName());
		}
		
		List<String> operationNames = caGridService.getOperations();
		
		System.out.println("Discovered " + operations.size() + " operations of the service:");
		for (String operationName : operationNames){
			System.out.println(operationName);
		}
		System.out.println();

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
				System.out.println("Discovered " + ops.length + " operations that require Globus GSI security:");
				for (int i = 0; i < ops.length; i++) {
					String lowerMethodName = ops[i].getName().substring(0, 1)
							.toLowerCase()
							+ ops[i].getName().substring(1);
					secureOperationsMap.put(lowerMethodName, ops[i]);
					System.out.println(lowerMethodName);

				}
				System.out.println();
			}
		}
		
		for (String secureOperation : secureOperationsMap.keySet()) {

			// An ActivityItem corresponds to an operation
			// services contains service metadata -- no wsdl parser is needed?
			// we can add a parser to parse it if we need more details on those services
			CaGridActivityItem item = new CaGridActivityItem();
			
			item.setOperation(secureOperation);
			
			//make use of "use" and "style" to facilitate metadata-based sorting
			item.setUse(secureOperation);
			//CaGrid services are all DOCUMENT style
			item.setStyle("document");
			
			item.setUrl(caGridService.getServiceName()+"?wsdl");
			
			if(!caGridService.getResearchCenterName().equals("")){
				item.setResearchCenter(caGridService.getResearchCenterName());	
			}

			try{

				CaGridQuery.configureSecurity(caGridService,
						secureOperationsMap.get(secureOperation), 
						authenticationServiceURL,
						dorianServiceURL,
						item);
				
				item.setSecure(true);
				System.out.println("Security properties for operation: " + secureOperation);
				System.out.println("GSI_TRANSPORT: " + item.getGSITransport());
				System.out.println("GSI_SEC_CONV: " + item.getGSISecureConversation());
				System.out.println("GSI_SEC_MSG: " + item.getGSISecureMessage());
				System.out.println("GSI_ANONYMOUS: " + item.getGSIAnonymouos());
				System.out.println("GSI_MODE: " + item.getGSIMode());
				System.out.println("GSI_AUTHORISATION: " + item.getAuthorisation());
				System.out.println("GSI_CREDENTIALS: " + item.getGSICredentials());
				System.out.println();
					
					// Now is a good place to ask for a caGrid username/password
					// to obtain proxy key and certificate
			}
			catch(Exception ex){
				/*logger.error("Error getting user's proxy for operation "
									+ operation
									+ " of service: "
									+ caGridService
											.getServiceName()
									+ "?wsdl. Skipping this operation.");*/
				ex.printStackTrace();
				continue;
			}		
		}
	}
	
	
	public void configureSecurity(ServiceSecurityMetadata securityMetadata,
			CaGridService caGridService, Operation operation,
			CaGridActivityItem item) throws RemoteException, MalformedURIException {

		boolean anonymousPrefered = true;
		
		boolean https = caGridService.getServiceWSDLLocation().toLowerCase().startsWith("https");
			
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

		// Get the proxy certificate - hardcode the username and password
		/*if (credentialsAllowed){
			
			String authenticationServiceURL = "https://cagrid-auth.nci.nih.gov:8443/wsrf/services/cagrid/AuthenticationService";
			String dorianURL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";
		
			BasicAuthentication auth = new BasicAuthentication();
	        auth.setUserId("anenadic");
	        auth.setPassword("m^s7a*kpT302");

	        // Authenticate to the Authentication Service using the basic authN credential
	        AuthenticationClient authClient = new AuthenticationClient(authenticationServiceURL);
	        SAMLAssertion saml = authClient.authenticate(auth);

	        // Set requested Grid Credential lifetime (12 hours)
	        CertificateLifetime lifetime = new CertificateLifetime();
	        lifetime.setHours(12);

	        // Request PKI/Grid Credential
	        GridUserClient dorian = new GridUserClient(dorianURL);
	        proxy = dorian.requestUserCertificate(saml, lifetime);
		}*/
		
		if ((anonymousAllowed) && (mechanism.isAnonymousPermitted()) && anonymousPrefered) {
			item.setGSIAnonymouos(Boolean.TRUE);
		} else if ((credentialsAllowed) && (proxy != null)) {
			try {
				// This should be done later on when we ask the user for the username and password and
				// obtain his proxy
				org.ietf.jgss.GSSCredential gss = new org.globus.gsi.gssapi.GlobusGSSCredentialImpl(proxy,
					org.ietf.jgss.GSSCredential.INITIATE_AND_ACCEPT);
				item.setGSICredentials(gss);
				item.setRequiresGSiCredentials(true);
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
