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
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
//import org.apache.log4j.Logger;


public class CaGridServiceFromWSDLSearcher {

	//private static Logger logger = Logger.getLogger(CaGridServiceFromWSDLSearcher.class);

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
		try {
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
		}

	    callBack.partialResults(serviceDescriptions);
	    callBack.finished();
	}
}


