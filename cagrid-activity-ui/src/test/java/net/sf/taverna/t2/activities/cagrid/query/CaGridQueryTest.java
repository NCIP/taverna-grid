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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import gov.nih.nci.cagrid.metadata.security.Operation;
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

import javax.net.ssl.HttpsURLConnection;

import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;

import org.apache.axis.types.URI.MalformedURIException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

public class CaGridQueryTest {

	private static String indexURL;
	@BeforeClass
	public static void setup() {
		//TODO should read from an external resource
		indexURL="http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	}
	
	
	@Ignore("This test takes ages as it loads all caGrid services so we have disabled it.")
	@Test
	public void testDoQuery() {
		CaGridQuery q = new CaGridQuery(indexURL,null);
		q.doQuery();
		CaGridActivityItem i = (CaGridActivityItem)q.toArray()[0];
		assertEquals("The type should be caGrid Services","caGrid Services",i.getType());
		assertEquals("The style should be document","document",i.getStyle());
		assertNotNull("The operation should be set",i.getOperation());
		assertTrue("The operation should be have some content",i.getOperation().length()>2);
	}
	
	//@Ignore
	@Test
	public void testSecurityMetadata(){
		
		//String serviceURL = "http://crchbioinfo.org:18080/wsrf/services/cagrid/CaArraySvc";
		//String serviceURL = "http://scigridserver.arc.georgetown.edu:18080/wsrf/services/cagrid/CaArraySvc";
		String serviceURL = "https://cagrid-gts-master.nci.nih.gov:8443/wsrf/services/cagrid/GTS";
		CaGridService caGridService = new CaGridService(serviceURL+"?wsdl", serviceURL);

		// Get the service's operations
		WSDLParser parser = null;
		List<javax.wsdl.Operation> operations = new ArrayList<javax.wsdl.Operation>(); 
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
		try {
			parser = new WSDLParser(serviceURL + "?wsdl");
			operations = parser.getOperations();
		}
		catch(Exception ex){
			System.out.println("There was an error with the wsdl: "+ serviceURL + "?wsdl");
			ex.printStackTrace();
		}
		
		for (javax.wsdl.Operation operation : operations){
			System.out.println("Discovered operation " + operation.getName());
			caGridService.addOperation(operation.getName());
		}
		
		List<String> operationNames = caGridService.getOperations();
		
		System.out.println("Discovered " + operations.size() + " operations of the service.");
		
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
				System.out.println("Discovered " + ops.length + " secure operations:");
				for (int i = 0; i < ops.length; i++) {
					String lowerMethodName = ops[i].getName().substring(0, 1)
							.toLowerCase()
							+ ops[i].getName().substring(1);
					secureOperationsMap.put(lowerMethodName, ops[i]);
					System.out.println(lowerMethodName);

				}
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

			// Check if operation is secure						
			//if (secureOperationsMap.containsKey(secureOperation)) { // is our operation among the secure ones?
				try{
					item.setSecure(true);
					CaGridQuery.configureSecurity(securityMetadata, caGridService, secureOperationsMap.get(secureOperation), item);
					System.out.println("Security properties for operation: " + secureOperation);
					System.out.println("GSI_TRANSPORT: " + item.getGSITransport());
					System.out.println("GSI_SEC_CONV: " + item.getGSISecureConversation());
					System.out.println("GSI_SEC_MSG: " + item.getGSISecureMessage());
					System.out.println("GSI_ANONYMOUS: " + item.getGSIAnonymouos());
					System.out.println("GSI_MODE: " + item.getGSIMode());
					System.out.println("GSI_AUTHORISATION: " + item.getAuthorisation());
					System.out.println();
					
					// Now is a good place to ask for a caGrid username/password
					// to obtain proxy key and certificate
				}
				catch(RemoteException rex){
					/*logger.error("Error getting user's proxy for operation "
									+ operation
									+ " of service: "
									+ caGridService
											.getServiceName()
									+ "?wsdl. Skipping this operation.");*/
					rex.printStackTrace();
					continue;
				}		
			/*} else { // operation is not secure
				item.setSecure(false);
			}*/	
		}
		
	}
	
}
