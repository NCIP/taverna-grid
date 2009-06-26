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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.log4j.Logger;

public class CaGridServiceSearcher {

	private static Logger logger = Logger.getLogger(CaGridServiceSearcher.class);

	private String caGridName;
	private String indexServiceURL;
	private CaGridServiceQuery[] serviceQueryList;
	
	static {
		CaGridActivity.initializeSecurity();
	}
	
	public CaGridServiceSearcher(String caGridName, String indexServiceURL,
			CaGridServiceQuery[] serviceQueryList)
			throws Exception {
		
		this.caGridName = caGridName;
		this.indexServiceURL = indexServiceURL;
		this.serviceQueryList = serviceQueryList;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {

	       List<CaGridService> caGridServices = null;                	
	        try {
				caGridServices=CaGridServiceQueryUtility.load(indexServiceURL, serviceQueryList);
	        }
			catch (Exception ex) {
				callBack.fail("An error occured when contacting the Index Service - could not load caGrid services.", ex);
				return;
	        }

			List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
			logger.info("Discovered "+ caGridServices.size() + " caGrid services.");
			for(CaGridService caGridService : caGridServices){
												      						
				List<String> operationNames = caGridService.getOperations();
				logger.info("Discovered caGrid service: "+ caGridService.getServiceName() + 
						" and its " + operationNames.size() + " operation(s).");
				
				for (String operation : operationNames) {
					logger.info("Adding operation: "+ operation + " for caGrid service " + caGridService.getServiceName());

					CaGridServiceDescription serviceDesc = new CaGridServiceDescription();
					
					serviceDesc.setOperation(operation);
					serviceDesc.setUse(operation);
					//CaGrid services are all DOCUMENT style
					serviceDesc.setStyle("document");
					serviceDesc.setURI(URI.create(caGridService.getServiceName()+"?wsdl"));
					if(!caGridService.getResearchCenterName().equals("")){
						serviceDesc.setResearchCenter(caGridService.getResearchCenterName());	
					}
					
					serviceDesc.setCaGridName(caGridName);
					serviceDesc.setIndexServiceURL(indexServiceURL);
					
					// Security properties of the item will be set later
					// at the time of adding the activity to the diagram
					serviceDescriptions.add(serviceDesc);
				}
			}
			logger.info("Added " + caGridServices.size() + " caGrid services to Service Panel.");
	    	callBack.partialResults(serviceDescriptions);
	    	callBack.finished();
		}
}

