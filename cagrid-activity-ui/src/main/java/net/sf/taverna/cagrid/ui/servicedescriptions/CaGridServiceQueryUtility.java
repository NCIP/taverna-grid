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

//Comments: do not use CaGrid client API, use pure WS client instead

import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
import gov.nih.nci.cagrid.metadata.MetadataUtils;
import gov.nih.nci.cagrid.metadata.ServiceMetadata;
import gov.nih.nci.cagrid.metadata.ServiceMetadataServiceDescription; 
import gov.nih.nci.cagrid.metadata.common.PointOfContact;
import gov.nih.nci.cagrid.metadata.common.UMLClass;
import gov.nih.nci.cagrid.metadata.exceptions.QueryInvalidException;
import gov.nih.nci.cagrid.metadata.exceptions.RemoteResourcePropertyRetrievalException;
import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
import gov.nih.nci.cagrid.metadata.service.Operation; 
import gov.nih.nci.cagrid.metadata.service.ServiceContext; 
import gov.nih.nci.cagrid.metadata.service.ServiceContextOperationCollection;
import gov.nih.nci.cagrid.metadata.service.ServiceServiceContextCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;

/**
 * An agent to query Index Service to determine the available categories and
 * services.
 * 
 * @author Wei Tan
 * 
 */

public class CaGridServiceQueryUtility {

	private static Logger logger = Logger.getLogger(CaGridServiceQueryUtility.class);

	// private static Logger logger =
	// Logger.getLogger(CaGridServiceQueryUtility.class);

	/**
	 * Returns a list of GT4 services, containing a list of their operations.
	 * Throws Exception if a service cannot be found.
	 */
	public static List<CaGridService> load(String indexURL, CaGridServiceQuery[] sq)
			throws Exception {
		List<CaGridService> services = new ArrayList<CaGridService>();

		// Get the categories for this installation
		boolean foundSome = loadServices(indexURL, sq, services);
		if (!foundSome || services.isEmpty()) { // should be enough to just check foundSome but anyhow
            JOptionPane.showMessageDialog(null,
                    "caGrid services search did not find any matching services", 
                    "caGrid services search",
                    JOptionPane.INFORMATION_MESSAGE);  		
        }
		return services;
	}

	// Load services & operations by caGrid discovery service API
	private static boolean loadServices(String indexURL, CaGridServiceQuery[] sq,
			List<CaGridService> services) throws Exception {
		boolean foundSome = false;

		if (sq.length == 0){
			logger.info("caGrid services search is searching for all services.");
		}
		else{
			logger.info("caGrid services search is using " + sq.length + " search criteria.");
		}
		EndpointReferenceType[] servicesList = null;
		servicesList = getEPRListByServiceQueryArray(indexURL, sq);
		logger.info("caGrid DiscoveryClient loaded and EPR to services returned.");
		if (servicesList == null){
			// Did not find any - this should really be an empty array and not null
			logger.error("caGrid search: resulting caGrid service list returned is null (empty).");
			return foundSome;
		}
		else{
			for (EndpointReferenceType epr : servicesList) {
				if (epr != null) {
					foundSome = true;
					// Add a service node
					String serviceAddress = epr.getAddress().toString();					
					// TODO add more metadata to s -- like research institute,
					// operation class?
					CaGridService service = new CaGridService(serviceAddress + "?wsdl",
							serviceAddress);
					//System.out.println(serviceAddress + "?wsdl");
					try {
						ServiceMetadata serviceMetadata = MetadataUtils
								.getServiceMetadata(epr);
						ServiceMetadataServiceDescription serviceDes = serviceMetadata
								.getServiceDescription();

						// ServiceContextOperationCollection s =
						// serviceDes.getService().getServiceContextCollection().getServiceContext(0).getOperationCollection();

						ServiceServiceContextCollection srvContxCol = serviceDes
								.getService().getServiceContextCollection();
						ServiceContext[] srvContxs = srvContxCol
								.getServiceContext();

						service.setResearchCenterName(serviceMetadata
								.getHostingResearchCenter().getResearchCenter()
								.getDisplayName());
						for (ServiceContext srvcontx : srvContxs) {
							ServiceContextOperationCollection operationCollection = srvcontx.getOperationCollection();
							if (operationCollection != null){
								Operation[] ops = srvcontx
										.getOperationCollection()
										.getOperation();

								// TODO: portType is no longer needed??
								for (Operation op : ops) {
									// add an operation node
									// print out the name of an operation
									String operationName = op.getName();
									// OperationInputParameterCollection opp =
									// op.getInputParameterCollection();

									service.addOperation(operationName);
									// System.out.println(operationName);
								}
							}

						}
						services.add(service);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
			return foundSome;
		}
	}

	public static EndpointReferenceType[] getEPRListByServiceQuery(
			String indexURL, CaGridServiceQuery sq) {
		EndpointReferenceType[] servicesList  = null;
		DiscoveryClient client = null;
		try {
			client = new DiscoveryClient(indexURL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sq == null) {
			logger.info("Retrieving all caGrid services from the Index Service: "
							+ indexURL);
			try {
				servicesList = client.getAllServices(true);
			} catch (RemoteResourcePropertyRetrievalException e) {
				logger.error("Error retrieving all caGrid services from the Index Service", e);
				e.printStackTrace();
			} catch (QueryInvalidException e) {
				logger.error("Error retrieving all caGrid services from the Index Service", e);
				e.printStackTrace();
			} catch (ResourcePropertyRetrievalException e) {
				logger.error("Error retrieving all caGrid services from the Index Service", e);
				e.printStackTrace();
			}
		} else {

			// semanticQueryingClause = indexURL.substring(n1+2);

			logger.info("caGrid service query criteria: " + sq.queryCriteria + "  == "
					+ sq.queryValue);
			
			// TODO: semantic based service searching
			// query by Search String
			if (sq.queryCriteria.equals("Search String")) {
				logger.info("Searching by 'Search Sting' criteria.");
				try {
					servicesList = client
							.discoverServicesBySearchString(sq.queryCriteria);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Search String'", e);
					e.printStackTrace();
				}
			}
			// query by Research Center Name
			else if (sq.queryCriteria.equals("Research Center")) {
				logger.info("Searching by 'Research Center' criteria.");
				try {
					servicesList = client
							.discoverServicesByResearchCenter(sq.queryValue);
				}catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Research Center'", e);
					e.printStackTrace();
				}
			}
			// query by Point of Contact
			else if (sq.queryCriteria.equals("Point Of Contact")) {
				logger.info("Searching by 'Point Of Contact' criteria.");
				PointOfContact poc = new PointOfContact();
				int n3 = sq.queryValue.indexOf(" ");
				String firstName = sq.queryValue.substring(0, n3);
				String lastName = sq.queryValue.substring(n3 + 1);
				poc.setFirstName(firstName);
				poc.setLastName(lastName);
				try {
					servicesList = client.discoverServicesByPointOfContact(poc);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Point of Contact'", e);
					e.printStackTrace();
				}
			}
			// query by Service Name
			else if (sq.queryCriteria.equals("Service Name")) {
				logger.info("Searching by 'Service Name' criteria.");
				try {
					servicesList = client.discoverServicesByName(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Service Name'", e);
					e.printStackTrace();
				}
			}
			// query by Operation Name
			else if (sq.queryCriteria.equals("Operation Name")) {
				logger.info("Searching by 'Operation Name' criteria.");
				try {
					servicesList = client
							.discoverServicesByOperationName(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Name'", e);
					e.printStackTrace();
				}
			}
			// query by Operation Input
			else if (sq.queryCriteria.equals("Operation Input")) {
				logger.info("Searching by 'Operation Input' criteria.");
				UMLClass umlClass = new UMLClass();
				umlClass.setClassName(sq.queryValue);
				try {
					servicesList = client
							.discoverServicesByOperationInput(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Input'", e);
					e.printStackTrace();
				}
			}
			// query by Operation Output
			else if (sq.queryCriteria.equals("Operation Output")) {
				logger.info("Searching by 'Operation Output' criteria.");
				UMLClass umlClass = new UMLClass();
				umlClass.setClassName(sq.queryValue);
				try {
					servicesList = client
							.discoverServicesByOperationOutput(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Output'", e);
					e.printStackTrace();
				}
			}
			// query by Operation Class
			else if (sq.queryCriteria.equals("Operation Class")) {
				logger.info("Searching by 'Operation Class' criteria.");
				UMLClass umlClass = new UMLClass();
				umlClass.setClassName(sq.queryValue);
				try {
					servicesList = client
							.discoverServicesByOperationClass(umlClass);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Operation Class'", e);
					e.printStackTrace();
				}
			}
			// discoverServicesByConceptCode("C43418")
			else if (sq.queryCriteria.equals("Concept Code")) {
				logger.info("Searching by 'Concept Code' criteria.");
				try {
					servicesList = client
							.discoverServicesByConceptCode(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Concept Code'", e);
					e.printStackTrace();
				}
			}
			// discoverServicesByOperationConceptCode
			// discoverServicesByDataConceptCode
			// discoverServicesByPermissibleValue
			// getAllDataServices
			// discoverDataServicesByDomainModel("caCore")
			else if (sq.queryCriteria.equals("Domain Model for Data Services")) {
				logger.info("Searching by 'Domain Model for Data Services' criteria.");
				try {
					servicesList = client
							.discoverDataServicesByDomainModel(sq.queryValue);
				} catch (RemoteResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
					e.printStackTrace();
				} catch (QueryInvalidException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
					e.printStackTrace();
				} catch (ResourcePropertyRetrievalException e) {
					logger.error("Error retrieving caGrid services from the Index Service using search criteria 'Domain Model for Data Services'", e);
					e.printStackTrace();
				}
			}
			// discoverDataServicesByModelConceptCode
			// discoverDataServicesByExposedClass
			// discoverDataServicesByPermissibleValue
			// discoverDataServicesByAssociationsWithClass
			// discoverByFilter
		}
		return servicesList;

	}

	public static EndpointReferenceType[] getEPRListByServiceQueryArray(
			String indexURL, CaGridServiceQuery sq[]) {
		EndpointReferenceType[] servicesList = null;
		if ((sq == null) || (sq.length==0)) { // null or empty service query list
			return getEPRListByServiceQuery(indexURL, null);
		} else if (sq.length == 1) {
			return getEPRListByServiceQuery(indexURL, sq[0]);
		}
		// sq holds more than 1 queries
		else if (sq.length > 1) {
			EndpointReferenceType[][] tempEPRList = new EndpointReferenceType[sq.length][];
			for (int i = 0; i < sq.length; i++) {
				tempEPRList[i] = getEPRListByServiceQuery(indexURL, sq[i]);
			}
			return CombineEPRList(tempEPRList);
		}
		return servicesList;

	}

	public static EndpointReferenceType[] CombineEPRList(
			EndpointReferenceType[][] tempEPRList) {
		EndpointReferenceType[] servicesList = null;
		String[][] addressList = new String[tempEPRList.length][];
		for (int i = 0; i < tempEPRList.length; i++) {
			addressList[i] = new String[tempEPRList[i].length];
			for (int j = 0; j < tempEPRList[i].length; j++) {
				addressList[i][j] = tempEPRList[i][j].getAddress().toString();
			}
		}
		List<String> alist = new ArrayList<String>(Arrays.asList(addressList[0]));
		for (int i = 1; i < tempEPRList.length; i++) {
			alist.retainAll(Arrays.asList(addressList[i]));
		}

		int count = 0;
		int[] flag = new int[tempEPRList[0].length];
		for (int i = 0; i < tempEPRList[0].length; i++) {
			if (alist.contains(tempEPRList[0][i].getAddress().toString())) {
				count++;
				flag[i] = 1;
			}
		}
		servicesList = new EndpointReferenceType[count];
		int j = 0;
		for (int i = 0; i < tempEPRList[0].length; i++) {

			if (flag[i] == 1) {
				servicesList[j++] = tempEPRList[0][i];
			}
		}
		return servicesList;
	}

}

class ServiceMetaData {
	String[] serviceAddress = null;
	String[][] operationName = null;

	ServiceMetaData() {
		//String[] serviceAddress = null;
		//String[][] operationName = null;
	}

}