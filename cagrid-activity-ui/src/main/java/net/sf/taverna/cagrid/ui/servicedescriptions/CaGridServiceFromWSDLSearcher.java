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
package net.sf.taverna.cagrid.ui.servicedescriptions;

import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription;
import gov.nih.nci.cagrid.metadata.service.Operation;
import gov.nih.nci.cagrid.metadata.service.ServiceContext;
import gov.nih.nci.cagrid.metadata.service.ServiceContextOperationCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;


public class CaGridServiceFromWSDLSearcher {

	private static Logger logger = Logger.getLogger(CaGridServiceFromWSDLSearcher.class);

	private String wsdlURL;
	private String caGridName;
	private String indexServiceURL;
	
	static {
		CaGridActivity.initializeSecurity();
	}

	public CaGridServiceFromWSDLSearcher(String wsdlURL, String caGridName, String indexServiceURL)
			throws Exception {
		
		this.wsdlURL = wsdlURL;
		this.caGridName = caGridName;
		this.indexServiceURL = indexServiceURL;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
			
		String addressString = wsdlURL.replaceFirst("[?]wsdl$", "");
		org.apache.axis.types.URI address;
		try {
			address = new org.apache.axis.types.URI(addressString);
		} catch (MalformedURIException ex) {
			callBack.fail("An error occurred while trying to create an URI for " + wsdlURL, ex);
			return;
		}
		EndpointReferenceType epr = new EndpointReferenceType(address);
		
		List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
		
		// Find serviceName from the URI
		URI uri = URI.create(epr.getAddress().toString());
		URI parentURI = uri.resolve(".");
		URI relativeURI = parentURI.relativize(uri);
		String serviceName = relativeURI.getPath();		
				
		// We'll just parse the wsdl here as using the commented out method below
		// we get the operations that are not from this wsdl but from the related
		// job resourcse
		URI wsdlURI = URI.create(wsdlURL);
		try {
			ServiceMetadata serviceMetadata = MetadataUtils.getServiceMetadata(epr);
			String researchCenter = serviceMetadata.getHostingResearchCenter().getResearchCenter().getDisplayName();
			ServiceMetadataServiceDescription serviceMetadataDesc = serviceMetadata.getServiceDescription();
			ServiceServiceContextCollection srvContxCol = serviceMetadataDesc.getService().getServiceContextCollection();						
			ServiceContext[] srvContxs = srvContxCol.getServiceContext();
			for (ServiceContext srvcontx : srvContxs) {
				ServiceContextOperationCollection operationCollection = srvcontx.getOperationCollection();
				String srvcontxServiceName = srvcontx.getName();
				if (operationCollection != null){
					Operation[] ops = srvcontx.getOperationCollection().getOperation();
					for (Operation op : ops) {
						// Add an operation as Taverna's ServiceDescription in Service Panel
						CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
						serviceDesc.setOperation(op.getName());
						serviceDesc.setUse(op.getName());
						//CaGrid services are all DOCUMENT style
						serviceDesc.setStyle("document");
						serviceDesc.setURI(wsdlURI);
						serviceDesc.setResearchCenter(researchCenter);	
						serviceDesc.setCaGridName(caGridName);
						serviceDesc.setIndexServiceURL(indexServiceURL);
						if (!srvcontxServiceName.equals(serviceName)){
							// This is a helper service
							serviceDesc.setHelperService(true);
							serviceDesc.setHelperServiceName(srvcontxServiceName);
							// The helper service has its own wsdl URL different from the one
							// of the master service
							serviceDesc.setURI(parentURI.resolve(srvcontxServiceName + "?wsdl"));
							serviceDesc.setMasterURI(wsdlURI);
						}
						logger.info("Adding operation "+ op.getDescription()+" under caGrid service "+ wsdlURL);
						serviceDescriptions.add(serviceDesc);
					}
				}
			}
			callBack.partialResults(serviceDescriptions);
		}
		catch (Exception e) {
			// This service probably did not have the getResourceProperty method defined
			// so getServiceMetadata failed - do the old fashioned wsdl parsing
			WSDLParser parser = null;
			try{
				parser = new WSDLParser(wsdlURL);
				List<javax.wsdl.Operation> operations = parser.getOperations();
				for (javax.wsdl.Operation operation : operations) {
					CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
					serviceDesc.setOperation(operation.getName());
					serviceDesc.setUse(operation.getName());
					//CaGrid services are all DOCUMENT style
					serviceDesc.setStyle("document");
					serviceDesc.setURI(wsdlURI);
					serviceDesc.setCaGridName(caGridName);
					serviceDesc.setIndexServiceURL(indexServiceURL);
					// Security properties of the service will be set later
					// at the time of invoking the activity
					serviceDescriptions.add(serviceDesc);
				}
				callBack.partialResults(serviceDescriptions);
			}
			catch (Exception ex){
				callBack.fail("Failed to create service description for caGrid service " + wsdlURL, ex);
			}
		}
		callBack.finished();
		
		// This was the original code for obtaining operations of a wsdl service from
		// service metadata etc, now replaced by the code above
		/*try {
			ServiceMetadata serviceMetadata = MetadataUtils.getServiceMetadata(epr);
			ServiceMetadataServiceDescription serviceDes = serviceMetadata.getServiceDescription();

			ServiceServiceContextCollection srvContxCol = serviceDes.getService().getServiceContextCollection();
			ServiceContext[] srvContxs = srvContxCol
					.getServiceContext();

			String researchCenter = serviceMetadata
			.getHostingResearchCenter().getResearchCenter()
			.getDisplayName();

			for (ServiceContext srvcontx : srvContxs) {
				ServiceContextOperationCollection operationCollection = srvcontx.getOperationCollection();
				if (operationCollection != null){
					Operation[] operations = srvcontx
							.getOperationCollection()
							.getOperation();

					for (Operation operation : operations) {
						
						CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
						serviceDesc.setOperation(operation.getName());
						serviceDesc.setUse(operation.getName());
						//CaGrid services are all DOCUMENT style
						serviceDesc.setStyle("document");
						serviceDesc.setURI(URI.create(wsdlURL));
						if(researchCenter!= null && !researchCenter.equals("")){
								serviceDesc.setResearchCenter(researchCenter);	
						}
						serviceDesc.setCaGridName(caGridName);
						serviceDesc.setIndexServiceURL(indexServiceURL);
						// Security properties of the service will be set later
						// at the time of invoking the activity
						serviceDescriptions.add(serviceDesc);
					}
				}
			}
		}
		catch (Exception ex) {
			callBack.fail("Failed to create service description for caGrid service " + wsdlURL, ex);
		}*/
	}
}


