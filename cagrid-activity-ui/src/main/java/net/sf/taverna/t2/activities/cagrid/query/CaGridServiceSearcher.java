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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.cagrid.servicedescriptions.CaGridServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.log4j.Logger;

public class CaGridServiceSearcher {

	private static Logger logger = Logger.getLogger(CaGridServiceSearcher.class);

	private String indexServiceURL;
	private ServiceQuery[] serviceQueryList;
	private String defaultAuthNServiceURL;
	private String defaultDorianServiceURL;
	
	public CaGridServiceSearcher(String indexServiceURL,
			ServiceQuery[] serviceQueryList, String authNServiceURL,
			String dorianServiceURL)
			throws Exception {
		
		this.indexServiceURL = indexServiceURL;
		this.serviceQueryList = serviceQueryList;
		this.defaultAuthNServiceURL = authNServiceURL;
		this.defaultDorianServiceURL = dorianServiceURL;
	}

	/**
	 * 
	 * @return an ArrayList of CaGridActivityItemS
	 * @throws Exception
	 *             if something goes wrong
	 */
	public synchronized ArrayList<CaGridActivityItem> getCaGridActivityItems()
			throws Exception {

        List<CaGridService> services = null;                	
        try {
			services=CaGridServiceQueryUtility.load(indexServiceURL, serviceQueryList);
        }
		catch (Exception ex) {
            	logger.error("Failed to load Index Service", ex);
            	ex.printStackTrace();
            	throw(ex);
        }

		ArrayList<CaGridActivityItem> searchResultsActivityItems = new ArrayList<CaGridActivityItem>();		
    	if(services != null){
			logger.info("Discovered "+ services.size() + " caGrid services.");
			for(CaGridService caGridService : services){
												      						
				List<String> operationNames = caGridService.getOperations();
				logger.info("Found caGrid service: "+ caGridService.getServiceName());	
				logger.info("Discovered " + operationNames.size() + " operation(s) of the service.");
				
				for (String operation : operationNames) {
					logger.info("Adding operation: "+ operation + " for service " + caGridService.getServiceName());

					CaGridActivityItem item = new CaGridActivityItem();
					
					item.setOperation(operation);
					//make use of "use" and "style" to facilitate metadata-based sorting
					item.setUse(operation);
					//CaGrid services are all DOCUMENT style
					item.setStyle("document");
					item.setUrl(caGridService.getServiceName()+"?wsdl");
					if(!caGridService.getResearchCenterName().equals("")){
						item.setResearchCenter(caGridService.getResearchCenterName());	
					}
					
					item.setIndexServiceURL(indexServiceURL);
					item.setDefaultAuthNServiceURL(defaultAuthNServiceURL);
					item.setDefaultDorianServiceURL(defaultDorianServiceURL);
					
					// Security properties of the item will be set later
					// at the time of adding the activity to the diagram
					searchResultsActivityItems.add(item);
				}
			}
    	}	
    	return searchResultsActivityItems;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {

	       List<CaGridService> services = null;                	
	        try {
				services=CaGridServiceQueryUtility.load(indexServiceURL, serviceQueryList);
	        }
			catch (Exception ex) {
				callBack.fail("An error occured when contacting the Index Service - could not load caGrid services.", ex);
				return;
	        }

			List<CaGridServiceDescription> serviceDescriptions = new ArrayList<CaGridServiceDescription>();
			logger.info("Discovered "+ services.size() + " caGrid services.");
			for(CaGridService caGridService : services){
												      						
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
					
					serviceDesc.setIndexServiceURL(indexServiceURL);
					serviceDesc.setDefaultAuthNServiceURL(defaultAuthNServiceURL);
					serviceDesc.setDefaultDorianServiceURL(defaultDorianServiceURL);
					
					// Security properties of the item will be set later
					// at the time of adding the activity to the diagram
					serviceDescriptions.add(serviceDesc);
				}
			}
			logger.info("Added " + services.size() + " caGrid services to Service Panel.");
	    	callBack.partialResults(serviceDescriptions);
	    	callBack.finished();
		}
}

