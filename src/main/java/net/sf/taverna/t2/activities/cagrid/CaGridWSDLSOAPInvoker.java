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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package net.sf.taverna.t2.activities.cagrid;

import gov.nih.nci.cagrid.opensaml.SAMLAssertion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.cagrid.wsdl.parser.UnknownOperationException;
import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;
import net.sf.taverna.cagrid.wsdl.soap.WSDLSOAPInvoker;
import net.sf.taverna.t2.activities.cagrid.config.CaGridConfiguration;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.cagrid.gaards.authentication.BasicAuthentication;
import org.cagrid.gaards.authentication.client.AuthenticationClient;
import org.cagrid.gaards.dorian.client.GridUserClient;
import org.cagrid.gaards.dorian.federation.CertificateLifetime;
import org.globus.gsi.GlobusCredential;
import org.ietf.jgss.GSSCredential;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;

/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x
 * and overrides the getCall(EngineConfiguration config) method to enable
 * invocation of secure Web Services.
 * 
 * @author Stuart Owen
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class CaGridWSDLSOAPInvoker extends WSDLSOAPInvoker {

	private static final String REFERENCE_PROPERTIES = "ReferenceProperties";
	private static final String ENDPOINT_REFERENCE = "EndpointReference";
	private static Logger logger = Logger.getLogger(CaGridWSDLSOAPInvoker.class);
	private static final Namespace wsaNS = Namespace.getNamespace("wsa", "http://schemas.xmlsoap.org/ws/2004/03/addressing");

	private String wsrfEndpointReference = null;
	
	// Configuration bean carries security settings for the operation
	private CaGridActivityConfigurationBean configurationBean;

	public CaGridWSDLSOAPInvoker(WSDLParser parser, CaGridActivityConfigurationBean bean,
			List<String> outputNames, String wsrfEndpointReference) {
		super(parser, bean.getOperation(), outputNames);
		this.wsrfEndpointReference = wsrfEndpointReference;	
		this.configurationBean = bean;
		
	}

	@SuppressWarnings("unchecked")
	protected void addEndpointReferenceHeaders(
			List<SOAPHeaderElement> soapHeaders) {
		// Extract elements
		// Add WSA-stuff
		// Add elements

		Document wsrfDoc;
		try {
			wsrfDoc = parseWsrfEndpointReference(wsrfEndpointReference);
		} catch (JDOMException e) {
			logger.warn("Could not parse endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		} catch (IOException e) {
			logger.error("Could not read endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		}
		
		//Element endpointRefElem = wsrfDoc.getRootElement();
		Element endpointRefElem = wsrfDoc.getRootElement().getChild(ENDPOINT_REFERENCE, wsaNS);
		if (endpointRefElem == null) {
			logger.warn("Could not find " + ENDPOINT_REFERENCE);
			return;
		}
		Element refPropsElem = endpointRefElem.getChild(REFERENCE_PROPERTIES, wsaNS);
		if (refPropsElem == null) {
			logger.warn("Could not find " + REFERENCE_PROPERTIES);
			return;
		}
		
		List<Element> refProps = refPropsElem.getChildren();
		// Make a copy of the list as it would be modified by
		// prop.detach();
		for (Element prop : new ArrayList<Element>(refProps)) {
			DOMOutputter domOutputter = new DOMOutputter();
			SOAPHeaderElement soapElem;
			prop.detach();
			try {
				org.w3c.dom.Document domDoc = domOutputter.output(new Document(prop));
				soapElem = new SOAPHeaderElement(domDoc.getDocumentElement());			
			} catch (JDOMException e) {
				logger.warn("Could not translate wsrf element to DOM:\n" + prop, e);
				continue;
			}
			soapElem.setMustUnderstand(false);
			soapElem.setActor(null);
			soapHeaders.add(soapElem);
		}
		
		

//		soapHeaders.add(new SOAPHeaderElement((Element) wsrfDoc
	//			.getDocumentElement()));
	}


	/**
	 * Returns an Axis-based Call, initialised for the operation that needs to
	 * be invoked.
	 * 
	 * @param config
	 *            - Axis engine configuration containing settings for the
	 *            handlers that are required in order to make a call to a caGrid service.
	 * @return Call object initialised for the operation that needs to be
	 *         invoked.
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws MalformedURLException
	 */
	@Override
	protected Call getCall(EngineConfiguration config) throws ServiceException,
			UnknownOperationException, MalformedURLException {
		
		Call call = super.getCall(config);
		
		// Configure caGrid security properties for the operation, if any.
		// This will configure the axis call with the appropriate GSI security 
		// configuration parameters based on the security metadata provided by the service.
		CaGridActivitySecurityProperties secProperties = CaGridActivity.securityPropertiesCache
		.get(configurationBean.getWsdl()
				+ configurationBean.getOperation()); // get security properties already populated from metadata by CaGridActivity

		if (secProperties != null){

			if (secProperties.getGSITransport() != null){
				call.setProperty(org.globus.wsrf.security.Constants.GSI_TRANSPORT, secProperties.getGSITransport());
			}

			if (secProperties.getGSISecureConversation() != null){
				call.setProperty(org.globus.wsrf.security.Constants.GSI_SEC_CONV, secProperties.getGSISecureConversation());
			}

			if (secProperties.getGSISecureMessage() != null){
				call.setProperty(org.globus.wsrf.security.Constants.GSI_SEC_MSG, secProperties.getGSISecureMessage());
			}

			if (secProperties.getGSIAnonymouos() != null){
				call.setProperty(org.globus.wsrf.security.Constants.GSI_ANONYMOUS, secProperties.getGSIAnonymouos());
			}

			if (secProperties.requiresProxy()){

				// Get the proxy and generate GSSCredential from it
				GSSCredential gss = null;
				try{
					gss = getGSSCredential();
					call.setProperty(org.globus.axis.gsi.GSIConstants.GSI_CREDENTIALS, gss);
				}
				catch(Exception ex){
					logger.error("Error occured while obtaning the user's "
							+ configurationBean.getCaGridName()
							+ " proxy for invoking the operation "
							+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl());				
				}
			}

			if (secProperties.getGSIAuthorisation() != null){
				call.setProperty(org.globus.wsrf.security.Constants.AUTHORIZATION, secProperties.getGSIAuthorisation());
			}

			if (secProperties.getGSIMode() != null){
				call.setProperty(org.globus.axis.gsi.GSIConstants.GSI_MODE, secProperties.getGSIMode());
			}
		}
		return call;
	}

	// Whether user wishes to be asked again (after he's been asked once and he declined) 
	// to renew certificate when it is close to expiration or he wants to let it expire
	private static HashMap<String, Boolean> renewProxyAskMeAgain = new HashMap<String, Boolean>();

	/**
	 * Get proxy for the user (either from Credential Manager or by authenticating 
	 * user with AuthN/Dorian) and use it to create GSSCredential
	 */	
	public GSSCredential getGSSCredential() throws Exception{
		
		// Get the proxy certificate - proxy should be created only once
		// for all services belonging to the same caGrid until it expires
		GlobusCredential proxy = null;

	    // Get AuthN Service and Dorian Service URLs - check if they are set in the configuration bean first,
	    // if not - get them from the preferences for the CaGrid this service belongs to.
		String authNServiceURL = configurationBean.getAuthNServiceURL();
		if (authNServiceURL == null) {
			CaGridConfiguration configuration = CaGridConfiguration
					.getInstance();
			authNServiceURL = configuration.getPropertyStringList(
					configurationBean.getCaGridName()).get(1);
		}
	    if (authNServiceURL == null) { // if still null - we are in trouble
			logger
					.error("Authentication Service has not been configured for the operation "
							+ configurationBean.getOperation()
							+ " of the service "
							+ configurationBean.getWsdl()
							+ " that expects user to authenticate");
			throw new Exception(
					"Authentication Service has not been configured for the operation "
							+ configurationBean.getOperation()
							+ " of the service " + configurationBean.getWsdl()
							+ " that expects user to authenticate");
		}
	    String dorianServiceURL = configurationBean.getDorianServiceURL();
		if (dorianServiceURL == null) {
			CaGridConfiguration configuration = CaGridConfiguration
					.getInstance();
			dorianServiceURL = configuration.getPropertyStringList(
					configurationBean.getCaGridName()).get(2);
		}
	    if (dorianServiceURL == null){ // if still null - we are in trouble
	        	logger.error("Dorian Service has not been configured for the operation "
						+ configurationBean.getOperation()
						+ " of the service "
						+ configurationBean.getWsdl() + " that expects user to have a proxy certificate");
				throw new Exception("Dorian Service has not been configured for the operation "
						+ configurationBean.getOperation()
						+ " of the service "
						+ configurationBean.getWsdl() + " that expects user to have a proxy certificate");
	    }
	        
        // Check first if Credential Manager already has a proxy for this operation
        CredentialManager credManager = null;
        try{
        	credManager = CredentialManager.getInstance();
        }
        catch (CMException cme){
        	logger.error(cme.getMessage());
        	throw cme;
        }
        
        PrivateKey privateKey;
        X509Certificate[] x509CertChain;

		// We sync here on CaGridActivity so no two CaGridActivities can fetch proxy
		// at the same time. This may a bit too harsh but is needed so that one CaGridActivity 
		// can finish with with getting the proxy from Dorian and saving it with Credential Manager 
		// so that the next one can pick it up from Credential Manager. In other words we want
		// getCaGridProxyXXX() and insertCaGridProxy() to be atomic so another
        // CaGridActivity would have to wait for this one to finish. This is not needed when 
		// two CaGrid Activities are from different caGrids but anyway.
		synchronized(CaGridActivity.class){	
			
		   	privateKey = credManager.getCaGridProxyPrivateKey(authNServiceURL, dorianServiceURL);
	    	Certificate[] certChain = credManager.getCaGridProxyCertificateChain(authNServiceURL, dorianServiceURL);
	    	boolean newProxy= true; // whether to get a new proxy
	        if (certChain != null && privateKey != null){
	        	logger.info("Proxy for the operation "+configurationBean.getOperation()+" found by Credential Manager.");
	        	x509CertChain = convertCertificatesToX509CertificateObjects(certChain);
	        	proxy = new GlobusCredential(privateKey, x509CertChain);
	        	// If it expires soon - ask the user to renew        	
	        	long timeLeft = proxy.getTimeLeft();
	        	logger.info("Time left for proxy before it expires (in seconds): " + timeLeft);
	        	if (timeLeft <= 0){
	        		// Already expired - get a new one
	            	logger.info("Proxy expired - getting a new one.");
	        		newProxy = true;
	        	}
	        	else if (timeLeft < 3600){ // less than one hour left
	            	logger.info("Proxy expires in less than an hour.");
	        		// Ask user - if he wishes to be asked :-)
	        		Boolean askMe = renewProxyAskMeAgain.get(configurationBean.getCaGridName());
	        		if (askMe == null || askMe == Boolean.TRUE){
	            		CaGridRenewProxyDialog renewProxyDialog = new CaGridRenewProxyDialog(configurationBean.getCaGridName());
	            		renewProxyDialog.setLocationRelativeTo(null);
	            		renewProxyDialog.setVisible(true);
	            		if (renewProxyDialog.renewProxy()){
	                		newProxy = true; // renew the proxy
	            		}
	            		else{
	                		newProxy = false; // do not get a new proxy
	                    	renewProxyAskMeAgain.put(configurationBean.getCaGridName(), new Boolean(renewProxyDialog.renewProxyAskMeAgain())); // whether to ask again to renew proxy for this caGrid or leave it till expires
	            		}
	                	logger.info("Ask again: " +  renewProxyDialog.renewProxyAskMeAgain());
	        		}    		
	        	}
	        	else{
	        		// Do not get a new proxy, this one is just fine
	        		newProxy = false;
	        	}
	        }
	        
	        if (newProxy){
	        	logger.info("Proxy for the operation "+configurationBean.getOperation()+" not found by Credential Manager - getting a new one.");
				try{
					
					String unpassPair = null;
					// Check first if we have a saved username/password pair for this Authentication Service
					unpassPair = credManager.getUsernameAndPasswordForService(authNServiceURL);

					String username = null;
					String password = null;
					boolean shouldSaveUsernameAndPassword = false;
			        if (unpassPair != null){
			        	username = unpassPair.substring(0, unpassPair.indexOf(' '));
			        	password = unpassPair.substring(unpassPair.indexOf(' ')+1);
			        }
			        else{
						GetCaGridPasswordDialog getPasswordDialog = new GetCaGridPasswordDialog(configurationBean.getCaGridName());
						getPasswordDialog.setLocationRelativeTo(null);
						getPasswordDialog.setVisible(true);

						username = getPasswordDialog.getUsername(); // get username
						password = getPasswordDialog.getPassword(); // get password
						shouldSaveUsernameAndPassword = getPasswordDialog.shouldSaveUsernameAndPassword();

						if (password == null) { // user cancelled - any of the above two variables is null 
							logger
							.error("User refused to enter username and password for "
									+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl() + ". The service invocation will most probably fail.");
							throw new Exception("User refused to enter username and password for "
									+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl() + ". The service invocation will most probably fail.");					
						}							
			        }

					BasicAuthentication auth = new BasicAuthentication();
					auth.setUserId(username);
			        auth.setPassword(password);
			        
					// Authentication succeeded - check if user wanted to permanently save 
			        // this username and password for this Authentication Service
					if (shouldSaveUsernameAndPassword){
				        try{
				        	// Get Credential Manager to save the username and passsoword			        	
							credManager.saveUsernameAndPasswordForService(username, password, authNServiceURL);
				        }
				        catch(CMException cme){
				        	// This is not fatal error but will probably cause problems 
				        	// in the long run as something is wrong with the keystore
				        	// Do nothing - the error is already logged
				        }
					}
			        
			        // Authenticate to the Authentication Service using the basic authN credential
			        AuthenticationClient authClient = new AuthenticationClient(authNServiceURL);	       
			        
			        SAMLAssertion saml = authClient.authenticate(auth);
			        logger.info("Authenticated the user with AuthN Service: " + authNServiceURL);

			        // Set the requested Grid credential lifetime (12 hours)
			        CertificateLifetime lifetime = new CertificateLifetime();
			        lifetime.setHours(12);

			        // Request PKI/Grid credential
			        GridUserClient dorian = new GridUserClient(dorianServiceURL);
			        proxy = dorian.requestUserCertificate(saml, lifetime);
			        logger.info("Obtained user's proxy from Dorian: "+ dorianServiceURL);
		        	
			        try{
			        	// Get Credential Manager to save the proxy	        	
				        credManager.saveCaGridProxy(proxy.getPrivateKey(), proxy.getCertificateChain(), authNServiceURL, dorianServiceURL);
			        }
			        catch(CMException cme){
			        	// This is not fatal error but will probably cause problems 
			        	// in the long run as something is wrong with the keystore
			        	// Do nothing - the error is already logged
			        }
				}
				catch(Exception ex){
					logger
					.error("Error occured while authenticating the user with caGrid for invoking operation "
							+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl());
					ex.printStackTrace();
					throw new Exception("Error occured while authenticating the user with caGrid for invoking operation "
							+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl(), ex);
				}
	        }
	        
		}
        
        // Create the GSS credential
        GSSCredential gss = null;
        try {
			gss = new org.globus.gsi.gssapi.GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_AND_ACCEPT);
			logger.info("Created GSSCredential from the proxy for operation " + configurationBean.getOperation());
		} catch (org.ietf.jgss.GSSException ex) {
			logger
			.error("Error occured while creating GSSCredential from the user's proxy for invoking operation "
					+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl());
			ex.printStackTrace();
			throw new Exception("Error occured while creating GSSCredential from the user's proxy for invoking operation "
					+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl(), ex);
		}
		return gss;
	}
	
	/**
	 * Convert Certificate objects to BC implementation of X509CertificateS
	 * (called X509CertificateObjectS).
	 */
	private X509Certificate[] convertCertificatesToX509CertificateObjects(
			Certificate[] certsIn) throws Exception{
		
        X509Certificate[] certsOut = new X509Certificate[certsIn.length];

        for (int iCnt = 0; iCnt < certsIn.length; iCnt++) {
            certsOut[iCnt] = convertCertificatesToX509CertificateObject(certsIn[iCnt]);
        }

        return certsOut;
	}
	private X509Certificate convertCertificatesToX509CertificateObject(
			Certificate certIn) throws Exception
    {
		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        ByteArrayInputStream bais = new ByteArrayInputStream(
            certIn.getEncoded());
        return (X509Certificate) cf.generateCertificate(bais);

    }
	
	@Override
	protected List<SOAPHeaderElement> makeSoapHeaders() {
		List<SOAPHeaderElement> soapHeaders = new ArrayList<SOAPHeaderElement>(
				super.makeSoapHeaders());
		if (wsrfEndpointReference != null && getParser().isWsrfService()) {
			addEndpointReferenceHeaders(soapHeaders);
		}
		return soapHeaders;
	}

	protected org.jdom.Document parseWsrfEndpointReference(
			String wsrfEndpointReference) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new StringReader(wsrfEndpointReference));
	}

}
