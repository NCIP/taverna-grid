package org.cagrid.transfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.rmi.RemoteException;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.cagrid.transfer.context.client.*;
import org.cagrid.transfer.context.stubs.types.*;
import org.cagrid.transfer.context.client.helper.*;
import org.cagrid.transfer.descriptor.Status;
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

public static void uploadInput(String epr ,String filePath){
		//create transfer is a method that staged some data and returned the Reference
		EndpointReferenceType endpointReference = new EndpointReferenceType();
		try {
			endpointReference =  readEprFromString(epr);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TransferServiceContextReference ref = new TransferServiceContextReference();
		
		ref.setEndpointReference(endpointReference);
		//create a client that enables me to talk to my transfer resource
		TransferServiceContextClient tclient;
		try {
			System.out.println("START UPLOAD:"+ filePath);		
			tclient = new TransferServiceContextClient(ref.getEndpointReference());			
			//upload data
			BufferedInputStream bis = null;
			File file = new File(filePath);
			long size = file.length();
			bis = new BufferedInputStream(new FileInputStream(file));
			TransferClientHelper.putData(bis, size, tclient.getDataTransferDescriptor());
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
		TransferServiceContextClient tclient = 
			new TransferServiceContextClient(endpointReference);
		// use the TransferClientHelper to get an InputStream to the data
		InputStream stream = 
			TransferClientHelper.getData(tclient.getDataTransferDescriptor());

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

}
