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

import net.sf.taverna.t2.activities.cagrid.servicedescriptions.CaGridServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;


public class CaGridServiceFromWSDLSearcher {

	private static Logger logger = Logger.getLogger(CaGridServiceFromWSDLSearcher.class);

	private String wsdlURL;
	private String caGridName;
	private String indexServiceURL;

	public CaGridServiceFromWSDLSearcher(String wsdlURL, String caGridName, String indexServiceURL)
			throws Exception {
		
		this.wsdlURL = wsdlURL;
		this.caGridName = caGridName;
		this.indexServiceURL = indexServiceURL;
	}

	/**
	 * 
	 * @return an ArrayList of CaGridActivityItemS
	 * @throws Exception
	 *             if something goes wrong
	 */
	public synchronized ArrayList<CaGridActivityItem> getCaGridActivityItems()
			throws Exception {

		String addressString = wsdlURL.replaceFirst("[?]wsdl$", "");
		org.apache.axis.types.URI address = new org.apache.axis.types.URI(addressString);
		EndpointReferenceType epr = new EndpointReferenceType(address);

		ArrayList<CaGridActivityItem> caGridActivityItemsSearchResults = new ArrayList<CaGridActivityItem>();		
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
					Operation[] ops = srvcontx
							.getOperationCollection()
							.getOperation();

					for (Operation op : ops) {
						CaGridActivityItem item = new CaGridActivityItem();
						item.setOperation(op.getName());
						item.setUse(op.getName());
						//CaGrid services are all DOCUMENT style
						item.setStyle("document");
						item.setUrl(wsdlURL);
						if(researchCenter!= null && !researchCenter.equals("")){
							item.setResearchCenter(researchCenter);	
						}
						item.setCaGridName(caGridName);
						item.setIndexServiceURL(indexServiceURL);
						
						// Security properties of the item will be set later
						// at the time of adding the activity to the diagram
						caGridActivityItemsSearchResults.add(item);
					}
				}
			}
		}
		catch (Exception ex) {
        	logger.error("Failed to add caGrid service " + wsdlURL, ex);
        	ex.printStackTrace();
			throw(ex);
		}
		
    	return caGridActivityItemsSearchResults;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
			
		ArrayList<CaGridActivityItem> itemList = null;
		try {
			itemList = this.getCaGridActivityItems();
		} catch (Exception ex) {
			callBack.fail("An error occurred while trying to add caGrid service " + wsdlURL, ex);
			return;
		}

		List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
		for(CaGridActivityItem item : itemList){
												      						
			CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
			serviceDesc.setOperation(item.getOperation());
			serviceDesc.setUse(item.getUse());
			serviceDesc.setStyle(item.getStyle());
			serviceDesc.setURI(URI.create(wsdlURL));
			if(item.getResearchCenter()!= null && !item.getResearchCenter().equals("")){
					serviceDesc.setResearchCenter(item.getResearchCenter());	
			}
				
			serviceDesc.setCaGridName(caGridName);
			serviceDesc.setIndexServiceURL(indexServiceURL);
					
			// Security properties of the item will be set later
			// at the time of adding the activity to the diagram
			serviceDescriptions.add(serviceDesc);

		}
	    callBack.partialResults(serviceDescriptions);
	    callBack.finished();
	}
}


