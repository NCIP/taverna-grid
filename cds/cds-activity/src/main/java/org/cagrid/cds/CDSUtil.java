package org.cagrid.cds;

import gov.nih.nci.cagrid.opensaml.SAMLAssertion;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.cagrid.activity.CaGridRenewProxyDialog;
import net.sf.taverna.cagrid.activity.GetCaGridPasswordDialog;
import net.sf.taverna.cagrid.activity.config.CaGridConfiguration;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.log4j.Logger;
import org.cagrid.gaards.authentication.BasicAuthentication;
import org.cagrid.gaards.authentication.client.AuthenticationClient;
import org.cagrid.gaards.dorian.client.GridUserClient;
import org.cagrid.gaards.dorian.federation.CertificateLifetime;
import org.globus.gsi.GlobusCredential;
import org.cagrid.gaards.cds.client.ClientConstants;
import org.cagrid.gaards.cds.client.DelegationUserClient;
import org.cagrid.gaards.cds.common.IdentityDelegationPolicy;
import org.cagrid.gaards.cds.common.ProxyLifetime;
import org.cagrid.gaards.cds.common.Utils;
import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.ietf.jgss.GSSCredential;

public class CDSUtil {
	private static Logger logger = Logger.getLogger(CDSUtil.class);
	// Whether user wishes to be asked again (after he's been asked once and he declined) 
	// to renew certificate when it is close to expiration or he wants to let it expire
	private static HashMap<String, Boolean> renewProxyAskMeAgain = new HashMap<String, Boolean>();
	public static GlobusCredential authenticate(String dorianURL, String authenticationServiceURL, String userId,
	        String password) throws Exception {
	        // Create credential

	        BasicAuthentication auth = new BasicAuthentication();
	        auth.setUserId(userId);
	        auth.setPassword(password);

	        // Authenticate to the IdP (DorianIdP) using credential

	        AuthenticationClient authClient = new AuthenticationClient(authenticationServiceURL);
	        SAMLAssertion saml = authClient.authenticate(auth);

	        // Requested Grid Credential lifetime (12 hours)

	        CertificateLifetime lifetime = new CertificateLifetime();
	        lifetime.setHours(12);

	        // Request PKI/Grid Credential
	        GridUserClient dorian = new GridUserClient(dorianURL);
	        GlobusCredential credential = dorian.requestUserCertificate(saml, lifetime);
	        return credential;
	    }

	//TODO some properties like lifetime and length, needs to be configured
	public static String delegateCredential(String caGridName,GlobusCredential credential, String party, 
			int dLifetime, int dPathLength, 
			int iLifetime,int iPathLength) throws Exception 
			{
		CaGridConfiguration configuration = CaGridConfiguration.getInstance();
		System.out.println(caGridName);
		String cdsURL = configuration.getPropertyStringList(caGridName).get(4);
		if (cdsURL == null || cdsURL.equals("")) { // if still empty - we are in trouble
			logger.error("Credential Delegation Service has not been configured for caGrid instance with name "
							+ caGridName);
			throw new Exception(
					"Credential Delegation Service has not been configured for caGrid instance with name "
					+ caGridName);
		}
        // Specifies how long the delegation service can delegated this
        // credential to other parties.

        ProxyLifetime delegationLifetime = new ProxyLifetime();
        delegationLifetime.setHours(dLifetime);
        delegationLifetime.setMinutes(0);
        delegationLifetime.setSeconds(0);

        // Specifies the path length of the credential being delegate the
        // minumum is 1.

        int delegationPathLength = dPathLength;

        // Specifies the how long credentials issued to allowed parties will
        // be valid for. e.g., 1 hr

        ProxyLifetime issuedCredentialLifetime = new ProxyLifetime();
        issuedCredentialLifetime.setHours(iLifetime);
        issuedCredentialLifetime.setMinutes(0);
        issuedCredentialLifetime.setSeconds(0);

        // Specifies the path length of the credentials issued to allowed
        // parties. A path length of 0 means that
        // the requesting party cannot further delegate the credential.

        int issuedCredentialPathLength = iPathLength;

        // Specifies the key length of the delegated credential

        int keySize = ClientConstants.DEFAULT_KEY_SIZE;

        // The policy stating which parties will be allowed to obtain a
        // delegated credential. The CDS will only
        // issue credentials to parties listed in this policy.

        List parties = new ArrayList();
        //TODO change to party
        parties.add(party);
        IdentityDelegationPolicy policy = Utils.createIdentityDelegationPolicy(parties);

        // Create an instance of the delegation client, specifies the CDS
        // Service URL and the credential
        // to be delegated.

        DelegationUserClient client = new DelegationUserClient(cdsURL, credential);
       
        // Delegates the credential and returns a reference which can later
        // be
        // used by allowed parties to
        // obtain a credential.

        DelegatedCredentialReference ref = client.delegateCredential(delegationLifetime, delegationPathLength, policy, issuedCredentialLifetime, issuedCredentialPathLength, keySize);
        QName qName = new QName("http://schemas.xmlsoap.org/ws/2004/03/addressing","EndpointReference");
    	String epr = ObjectSerializer.toString(ref.getEndpointReference(),qName);
        
	return epr;
    }
/**
 * Get proxy for the user (either from Credential Manager or by authenticating 
 * user with AuthN/Dorian) and use it to create GSSCredential
 */	
public static GlobusCredential getGlobusCredential(String caGridName) throws Exception{
	
	// Get the proxy certificate - proxy should be created only once
	// for all services belonging to the same caGrid until it expires
	GlobusCredential proxy = null;

    // Get AuthN Service and Dorian Service URLs - check if they are set in the configuration bean first,
    // if not - get them from the preferences for the CaGrid this service belongs to.
	CaGridConfiguration configuration = CaGridConfiguration.getInstance();
	System.out.println(caGridName);
	String authNServiceURL = configuration.getPropertyStringList(caGridName).get(1);
    if (authNServiceURL == null || authNServiceURL.equals("")) { // if still empty - we are in trouble
		logger.error("Authentication Service has not been configured for caGrid instance with name "
						+ caGridName);
		throw new Exception(
				"Authentication Service has not been configured for caGrid instance with name "
				+ caGridName);
	}
    
    String dorianServiceURL = configuration.getPropertyStringList(caGridName).get(2);
    if (dorianServiceURL == null || dorianServiceURL.equals("")){ // if still empty - we are in trouble
        	logger.error("Dorian Service has not been configured for caGrid instance with name "
						+ caGridName);
			throw new Exception("Dorian Service has not been configured for caGrid instance with name "
					+ caGridName);
    }
        
    // Check first if Credential Manager already has a proxy for this caGridName
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

	// We sync here on CDSActivity so no two CDSActivities can fetch proxy
	// at the same time. 
    //TODO needs to synchronized on CaGridActivity as well?
   
	synchronized(CDSActivity.class){	
		System.out.println("getting credential with Auth and Dorian URLs:\n"
				+authNServiceURL+ "\n"+ dorianServiceURL);
	   	privateKey = credManager.getCaGridProxyPrivateKey(authNServiceURL, dorianServiceURL);
    	Certificate[] certChain = credManager.getCaGridProxyCertificateChain(authNServiceURL, dorianServiceURL);
    	boolean newProxy= true; // whether to get a new proxy
        if (certChain != null && privateKey != null){
        	logger.info("Proxy caGrid "+ caGridName +" found by Credential Manager.");
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
        		Boolean askMe = renewProxyAskMeAgain.get(caGridName);
        		if (askMe == null || askMe == Boolean.TRUE){
            		CaGridRenewProxyDialog renewProxyDialog = new CaGridRenewProxyDialog(caGridName);
            		renewProxyDialog.setLocationRelativeTo(null);
            		renewProxyDialog.setVisible(true);
            		if (renewProxyDialog.renewProxy()){
                		newProxy = true; // renew the proxy
            		}
            		else{
                		newProxy = false; // do not get a new proxy
                    	renewProxyAskMeAgain.put(caGridName, new Boolean(renewProxyDialog.renewProxyAskMeAgain())); // whether to ask again to renew proxy for this caGrid or leave it till expires
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
        	logger.info("Proxy for the caGrid "+caGridName+" not found by Credential Manager - getting a new one.");
			try{
				// User's username and password pair for the AuthN service
				String [] unpassPair = null;
				// Check first if we have a saved username/password pair for this Authentication Service
				unpassPair = credManager.getUsernameAndPasswordForService(authNServiceURL);
				String username = null;
				String password = null;
				boolean shouldSaveUsernameAndPassword = false;
		        if (unpassPair != null){
		        	username = unpassPair[0];
		        	password = unpassPair[1];
		        }
		        else{
					GetCaGridPasswordDialog getPasswordDialog = new GetCaGridPasswordDialog(caGridName);
					getPasswordDialog.setLocationRelativeTo(null);
					getPasswordDialog.setVisible(true);

					username = getPasswordDialog.getUsername(); // get username
					password = getPasswordDialog.getPassword(); // get password
					shouldSaveUsernameAndPassword = getPasswordDialog.shouldSaveUsernameAndPassword();

					if (password == null) { // user cancelled - any of the above two variables is null 
						logger
						.error("User refused to enter username and password for caGrid"
								+ caGridName+ ". The credential delegation will fail.");
						throw new Exception("User refused to enter username and password for "
								+ caGridName+ ". The credential delegation will fail.");					
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

		        // Set the requested Grid credential lifetime - get it from user's preferences
		        CertificateLifetime lifetime = new CertificateLifetime();
		        String proxyLifetimeFromPreferences = configuration.getPropertyStringList(caGridName).get(3);
		        
		        if (proxyLifetimeFromPreferences == null || proxyLifetimeFromPreferences.equals("")){ // should not be null really but just in case
		        	lifetime.setHours(12); // set to 12 hours by default
			        logger.error("Proxy lifetime is missing from preferences - setting proxy lifetime to default (12 hours).");
		        }
		        else{
		        	try{
		        		lifetime.setHours(Integer.parseInt(proxyLifetimeFromPreferences));
				        logger.info("Setting proxy lifetime to: " + proxyLifetimeFromPreferences + " hours.");
		        	}catch(NumberFormatException nfex){
			        	lifetime.setHours(12); // set to 12 hours by default
				        logger.error("Proxy lifetime format in preferences is wrong - setting proxy lifetime to default (12 hours).");
		        	}
		        }

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
				logger.error("Error occured while authenticating the user with caGrid "
						+ caGridName);
				ex.printStackTrace();
				throw new Exception("Error occured while authenticating the user with caGrid "
						+ caGridName, ex);
			}
        }
        
	}
	return proxy;
}
/**
 * Convert Certificate objects to BC implementation of X509CertificateS
 * (called X509CertificateObjectS).
 */
private static X509Certificate[] convertCertificatesToX509CertificateObjects(
		Certificate[] certsIn) throws Exception{
	
    X509Certificate[] certsOut = new X509Certificate[certsIn.length];

    for (int iCnt = 0; iCnt < certsIn.length; iCnt++) {
        certsOut[iCnt] = convertCertificatesToX509CertificateObject(certsIn[iCnt]);
    }

    return certsOut;
}
private static X509Certificate convertCertificatesToX509CertificateObject(
		Certificate certIn) throws Exception
{
	//add BC provider otherwise certificate factory cannot find it.
	//it seems that caGrid activity does not add this provider but still works
	java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
    ByteArrayInputStream bais = new ByteArrayInputStream(
        certIn.getEncoded());
    return (X509Certificate) cf.generateCertificate(bais);

}

}
