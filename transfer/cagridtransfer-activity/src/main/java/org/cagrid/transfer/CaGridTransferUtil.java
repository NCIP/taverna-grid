package org.cagrid.transfer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


//import net.sf.taverna.t2.security.credentialmanager.CMException;
//import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.cagrid.transfer.context.client.*;
import org.cagrid.transfer.context.stubs.types.*;
import org.cagrid.transfer.context.client.helper.*;
import org.cagrid.transfer.descriptor.Status;
import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.xml.sax.InputSource;

public class CaGridTransferUtil {
	
public static EndpointReferenceType readEprFromString(String eprString) throws Exception {
		StringReader in = null;
		EndpointReferenceType ref = new EndpointReferenceType();
		try {
			in = new StringReader(eprString);
			ref = (EndpointReferenceType) ObjectDeserializer.deserialize(new InputSource(in),
					EndpointReferenceType.class);
		} finally {
			if (in != null) {
				try { in.close(); } catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ref;
	}

public static void uploadInput(String epr ,String filePath)throws Exception{
		//create transfer is a method that staged some data and returned the Reference
		EndpointReferenceType endpointReference = new EndpointReferenceType();
		try {
			endpointReference =  readEprFromString(epr);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		////////////////////////////////////////////////////////
		 GlobusCredential proxy = null;
		/*
		  // assume Credential Manager already has a proxy for this operation, and use it
      CredentialManager credManager = null;
      try{
      	credManager = CredentialManager.getInstance();
      }
      catch (CMException cme){
      	System.out.println(cme.getMessage());
      	throw cme;
      }
      GlobusCredential proxy = null;
      PrivateKey privateKey;
      X509Certificate[] x509CertChain;
      
      //the up-to-date auth and dorian urls
      String authNServiceURL = "https://dorian.cvrgrid.cci.emory.edu:8443/wsrf/services/cagrid/Dorian";
      String dorianServiceURL = "https://dorian.cvrgrid.cci.emory.edu:8443/wsrf/services/cagrid/Dorian";
      
      //String authNServiceURL = "https://dorian.bmi.ohio-state.edu:9443/wsrf/services/cagrid/Dorian";
      //String dorianServiceURL = "https://dorian.bmi.ohio-state.edu:9443/wsrf/services/cagrid/Dorian";
   	privateKey = credManager.getCaGridProxyPrivateKey(authNServiceURL, dorianServiceURL);
  	Certificate[] certChain = credManager.getCaGridProxyCertificateChain(authNServiceURL, dorianServiceURL);
  	x509CertChain = convertCertificatesToX509CertificateObjects(certChain);
  	proxy = new GlobusCredential(privateKey, x509CertChain);
  	*/
      ///////////////////////////////////////////////////////////
		TransferServiceContextReference ref = new TransferServiceContextReference();
		
		ref.setEndpointReference(endpointReference);
		//create a client that enables me to talk to my transfer resource
		TransferServiceContextClient tclient;
		try {
			System.out.println("START UPLOAD:"+ filePath);		
			tclient = new TransferServiceContextClient(ref.getEndpointReference(),proxy);			
			//upload data
			BufferedInputStream bis = null;
			File file = new File(filePath);
			long size = file.length();
			bis = new BufferedInputStream(new FileInputStream(file));
			TransferClientHelper.putData(bis, size, tclient.getDataTransferDescriptor(),proxy);
			tclient.setStatus(Status.Staged);
			System.out.println("DONE UPLOAD:"+ filePath);				
		} catch (MalformedURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Downloads the result file from the server using the caGrid Transfer interface.
	 
	public static String downloadResult(String epr, String filePath) throws Exception {
		EndpointReferenceType endpointReference = new EndpointReferenceType();
		try {
			endpointReference =  readEprFromString(epr);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("START DOWNLOAD:"+ filePath);	
		////////////////////////////////////////////////////////
		 GlobusCredential proxy = null;
		 /*
		  // assume Credential Manager already has a proxy for this operation, and use it
        CredentialManager credManager = null;
        try{
        	credManager = CredentialManager.getInstance();
        }
        catch (CMException cme){
        	System.out.println(cme.getMessage());
        	throw cme;
        }
        GlobusCredential proxy = null;
        PrivateKey privateKey;
        X509Certificate[] x509CertChain;
        
        //the up-to-date auth and dorian urls
        String authNServiceURL = "https://dorian.cvrgrid.cci.emory.edu:8443/wsrf/services/cagrid/Dorian";
        String dorianServiceURL = "https://dorian.cvrgrid.cci.emory.edu:8443/wsrf/services/cagrid/Dorian";
        
        //String authNServiceURL = "https://dorian.bmi.ohio-state.edu:9443/wsrf/services/cagrid/Dorian";
        //String dorianServiceURL = "https://dorian.bmi.ohio-state.edu:9443/wsrf/services/cagrid/Dorian";
     	privateKey = credManager.getCaGridProxyPrivateKey(authNServiceURL, dorianServiceURL);
    	Certificate[] certChain = credManager.getCaGridProxyCertificateChain(authNServiceURL, dorianServiceURL);
    	x509CertChain = convertCertificatesToX509CertificateObjects(certChain);
    	proxy = new GlobusCredential(privateKey, x509CertChain);
    	*/
        ///////////////////////////////////////////////////////////
		TransferServiceContextClient tclient = 
			new TransferServiceContextClient(endpointReference,proxy);
		// use the TransferClientHelper to get an InputStream to the data
		InputStream stream = 
			TransferClientHelper.getData(tclient.getDataTransferDescriptor(),proxy);

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			int c;
			while ((c = stream.read()) != -1) {
				out.write(c);
			}
		} catch (Exception e) {
			
		} finally {
			if (out != null) out.close();
		}
		System.out.println("DONE DOWNLOAD:"+ filePath);
		return filePath;		
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
		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        ByteArrayInputStream bais = new ByteArrayInputStream(
            certIn.getEncoded());
        return (X509Certificate) cf.generateCertificate(bais);

    }

}
