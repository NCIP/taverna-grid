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

import gov.nih.nci.cadsr.umlproject.domain.Project;
import gov.nih.nci.cadsr.umlproject.domain.UMLClassMetadata;
import gov.nih.nci.cadsr.umlproject.domain.UMLPackageMetadata;
import gov.nih.nci.cagrid.cadsr.client.CaDSRServiceClient;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import gov.nih.nci.cagrid.metadata.security.CommunicationMechanism;
import gov.nih.nci.cagrid.metadata.security.Operation;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadataOperations;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.ShadedLabel;

/**
 * Dialog for searching for caGrid services and adding them to Taverna's service panel. 
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class CaGridQueryDialog extends JDialog{

	private static Logger logger = Logger.getLogger(CaGridQueryDialog.class);
	
	// This static block is needed in case some of the caGrid services require 
	// https which is more than likely and needs to be executed before we start loading 
	// caGrid services or otherwise some of these services will fail. 
	// Some caGrid services requiring https have a weird CN in their server certificates - 
	// instead of CN=<HOSTNAME> they have CN="host/"+<HOSTNAME>, i.e. string 
	// "host/" prepended so we have to tell Java's SSL to accept these hostnames as well.
	// This is not very good at is sets this hostname verifier across all 
	// https connections created in the JVM from now on, but solves the problem 
	// with such caGrid services.
	static {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostName, SSLSession session) {
				String hostNameFromCertificate = null;
				try {
					hostNameFromCertificate = session.getPeerPrincipal()
							.getName().substring(3,
									session.getPeerPrincipal().getName()
											.indexOf(','));
				} catch (SSLPeerUnverifiedException e) {
					e.printStackTrace();
					return false;
				}
				logger.info("Hostname verifier: host from url: " + hostName + " vs. host from certificate: "+ hostNameFromCertificate);
				System.out.println();
				return (hostName.equals(hostNameFromCertificate) || ("host/"+hostName)
						.equals(hostNameFromCertificate));
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	final int query_size=3;//max query item size
    public int query_count =1; // current number of queries

	// Index Services
	private String[] indexServicesURLs = { "http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", 
			"http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService"};
	
	// Map of caDSR Services corresponding to each of the Index Services
	private Map<String,String> caDSRServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "http://cagrid-service.nci.nih.gov:8080/wsrf/services/cagrid/CaDSRService");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://cadsr.training.cagrid.org:8443/wsrf/services/cagrid/CaDSRService");
	    }
	};	
	
	// Map of Authentication Services corresponding to each of the Index Services 
	// (should be a list of Authentication Services (and not just one) for each Index Service really)
	private Map<String,String> authenticationServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-auth.nci.nih.gov:8443/wsrf/services/cagrid/AuthenticationService");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};
	
	// Map of Dorian Services corresponding to each of the Index Services
	private Map<String,String> dorianServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};	
	
	public JComboBox indexServicesURLsComboBox = new JComboBox(indexServicesURLs);
	
	public JComboBox[]  queryList = new JComboBox[query_size];
	private String[] queryStrings = { "None", "Search String", "Point Of Contact", "Service Name", "Operation Name", "Operation Input",
			"Operation Output","Operation Class", "Research Center","Concept Code",
			"Domain Model for Data Services"};
	public JComboBox[] queryValue = new JComboBox[query_size];
	private String[] queryValues = {};
	
	public JButton addQueryButton = new JButton("Add service query");
    public JButton removeQueryButton = new JButton("Remove service query");
    
	private Thread searchThread;

	private JList searchResultsList;
	private DefaultListModel searchResultsListModel;
	private JScrollPane searchResultsScrollPane;
	private JLabel searchStatusLabel = new JLabel("Status: search results empty.");
	private List<CaGridActivityItem> searchResultsActivityItems;
	
    public CaGridQueryDialog(){
        super();
		this.setTitle("Search for caGrid services");		
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JPanel indexServicePanel = new JPanel(new BorderLayout());
        indexServicePanel.setBorder(new EmptyBorder(5,5,5,5));
        for(int i=0;i<query_size;i++){
        	queryValue[i]=new JComboBox(queryValues);
        	queryValue[i].setEditable(true);
        	FontMetrics fm = getFontMetrics(queryValue[i].getFont());
        	int width = 50 + fm.stringWidth("RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen"); //the longest string
        	queryValue[i].setPreferredSize(new Dimension(width,queryValue[i].getPreferredSize().height));
        	queryValue[i].setMinimumSize(new Dimension(width,queryValue[i].getPreferredSize().height));
        	queryList[i] = new JComboBox(queryStrings);     
        	
        	final JComboBox final_queryValue = queryValue[i];
            // Listener for queryList comboBox - if selected service query criteria is "Operation Class",
        	// "Operation Input" or "Operation Output", retrieve possible query values from caDSR Service
			queryList[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String queryCriteriaString = (String) ((JComboBox) ae
							.getSource()).getSelectedItem();
					// Use values from classNameArray - later on should get them from caDSR
					if (queryCriteriaString.equals("Operation Class")
							|| queryCriteriaString.equals("Operation Input")
							|| queryCriteriaString.equals("Operation Output")) {
						// Should contact caDSR really for the up-to-date values
						// of classNameArray
						final_queryValue.setModel(new DefaultComboBoxModel(
								classNameArray));
					} else {
						// keep the combobox empty and editible
						String[] emptyValue = {};
						final_queryValue.setModel(new DefaultComboBoxModel(emptyValue));

					}
					final_queryValue.validate();
				}
			});
        }
        indexServicePanel.add(new ShadedLabel("Location (URL) of the Index Service: ", ShadedLabel.BLUE, true), BorderLayout.WEST);
        //indexServiceURLs.setEditable(true);
        indexServicesURLsComboBox.setToolTipText("caGrid Services will be retrieved from the Index Service whose URL you specify here");
        indexServicePanel.add(indexServicesURLsComboBox, BorderLayout.CENTER);
        mainPanel.add(indexServicePanel, BorderLayout.NORTH);
        
        JPanel serviceQueryPanel = new JPanel(new BorderLayout());
        serviceQueryPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder(EtchedBorder.LOWERED)));
        // Panel with queries
        JPanel queryPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5,0,5,5);
        queryPanel.add(new ShadedLabel("Service query criteria: ", ShadedLabel.BLUE, true), c);        
		c.gridx = 1;
		c.gridy = 0;
        queryPanel.add(new ShadedLabel("Service query value: ", ShadedLabel.BLUE, true), c);
        
        c.gridy = 1;
        for (int i=0 ; i<query_size; i++){
        	c.gridx = 0;
        	queryValue[i].setToolTipText("Service search will use the query value you specify here");
        	queryPanel.add(queryList[i], c);
        	c.gridx = 1;
        	queryPanel.add(queryValue[i], c);  	
        	c.gridy++;
        }
        serviceQueryPanel.add(queryPanel, BorderLayout.CENTER);

        // Panel with buttons
        JPanel queryButtonsPanel = new JPanel();
        queryButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        queryButtonsPanel.setBorder(new EmptyBorder(0,0,0,25));
        JButton updateCaDSRDataButton = new JButton("Update caDSR data");
        updateCaDSRDataButton.setToolTipText("Get an updated UML class list from caDSR Service. \n" +
		"This operation may take a few minutes depending on network status.");
        updateCaDSRDataButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				Thread updateCaDSRDataThread = new Thread("Updating caDSR metadata") {
					public void run() {
						// Update the value of classNameArray
                    	 ArrayList<String> classNameList = new ArrayList<String>();
    					 Project[] projs = null;
    					 CaDSRServiceClient cadsr  =null;
    					 UMLPackageMetadata[] packs = null;
    					 UMLClassMetadata[] classes = null;
    					 System.out.println("===========Updating caDSR Metadata================");
    					 
    					 //Note: the try-catch module should be with fine granularity
    					 try {
    						 cadsr = new CaDSRServiceClient(caDSRServicesMap.get(indexServicesURLsComboBox.getSelectedItem()));		                					            
    					     projs = cadsr.findAllProjects();
    					 }
    					 catch (Exception e) {
    						 e.printStackTrace();
    					 }
    					 
    					 if(projs !=null){
    						 for (int i = 0; i<projs.length;i++){
    							 Project project = projs[i];
    							 //System.out.println("\n"+ project.getShortName());
    							 //the bridg and c3pr project always yield error -- don't know why. so simply bypass them
    							 if(!project.getShortName().equals("BRIDG")&&!project.getShortName().equals("C3PR")){
    								 try {
    									 packs = cadsr.findPackagesInProject(project);
    								 }
    								 catch (Exception e) {
    									 e.printStackTrace();
    								 }
    								 if(packs !=null){
    									 for(int j= 0;j<packs.length;j++){
    										 UMLPackageMetadata pack = packs[j];
    										 //System.out.println("\t-" + pack.getName());
    										 try {
    											 classes = cadsr.findClassesInPackage(project, pack.getName());
    										 }
    										 catch (Exception e) {
    											 e.printStackTrace();
    										 }
    										 if(classes !=null){
    											 for (int k=0;k<classes.length;k++){
    												 UMLClassMetadata clazz = classes [k];
    												 //System.out.println("\t\t-"+clazz.getName());
    												 if(!classNameList.contains(clazz.getName()))
    													 //classNameList is updated here!
    													 classNameList.add((String)clazz.getName());
    												 else {
    													 //System.out.println("Duplicated Class Name Found.");
    												 }
    											 }
    										 }
    									 }
    								 }
    							 }
    						 }
    					 }
    					 
    					 String [] clsNameArray;
    					 // If the retrieved class name list is not empty, update the static datatype classNameArray
    					 if(!classNameList.isEmpty()){
    						 clsNameArray = (String[]) classNameList.toArray(new String[0]);
    						 Arrays.sort(clsNameArray,String.CASE_INSENSITIVE_ORDER);		                					       
    						 System.out.println("=========Class Names Without Duplications=============");
    						 for(int i=0;i<clsNameArray.length;i++){
    							 System.out.println(clsNameArray[i]);
    						 }
    						 classNameArray  = clsNameArray;
    						 JOptionPane.showMessageDialog(null, "caDSR data has been updated. \n Now there are " + classNameArray.length + " UML classes in the list.", null, JOptionPane.INFORMATION_MESSAGE);   
    					 }
    					 else{
    						 //the current value of the GT4ScavengerHelper.classNameArray is not updated
    						 //System.out.println("Empty class name list retrieved, so classNameArray is not updated!");
    						 JOptionPane.showMessageDialog(null,"Empty class name list retrieved from caDSR Service, so UML class names have not been updated!", null, JOptionPane.INFORMATION_MESSAGE);
    					 }	 
                    }
            	};
            	updateCaDSRDataThread.start();
            }
		});
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				searchResultsListModel.removeAllElements();
				searchThread = new Thread("Searching Index Service for available caGrid services") {
					public void run() {
						updateSearchStatus("Status: searching for services...");
		                
						String indexServiceURL = (String) indexServicesURLsComboBox.getSelectedItem();
		                 // Gather service queries - if any
		                int [] flag = new int[query_count];
		                int count = 0;
		                for (int i=0;i<query_count;i++){
		                	if(!getQueryCriteria(i).equals("None")&&!getQueryValue(i).equals("")){
		                		count ++ ;
		                		flag[i]=1;
		                	}	
		                }
		                ServiceQuery[] sqList= null;
		                if(count>0){
		                	sqList = new ServiceQuery[count];
		                	int j = 0;
		                	for (int i=0;i<query_count;i++){
		                		if(flag[i]==1){
		                			sqList[j++] = new ServiceQuery(getQueryCriteria(i),getQueryValue(i));
		                			System.out.println("Adding Query: "+ sqList[j-1].queryCriteria + "  = " + sqList[j-1].queryValue);
		                		}	
		                	}
		                }
		                
		                List<CaGridService> services = null;                	
    					boolean searchStopped = false;
    					int serviceCounter = 0;
		                try {
	            			services=CaGridServiceQueryUtility.load(indexServiceURL, sqList);
		                }
		                catch (InterruptedException intrex) {
		                    // Allow thread to exit 
		                	searchStopped = true;
        					updateSearchStatus("Status: search stopped. Found 0 services.");
        					Thread.currentThread().interrupt();
		                }
	            		catch (Exception ex) {
			    				updateSearchStatus("Status: search failed. Failed to load Index Service.");
			                	JOptionPane
			                	.showMessageDialog(null,
			                			"Failed to load Index Service - please try again later!",
			                			"Error!",
			                			JOptionPane.ERROR_MESSAGE);
			                	logger.error(ex);
			                	ex.printStackTrace();
			            }

	            		
            			if((!Thread.currentThread().isInterrupted()) && (!searchStopped) && (services != null)){
        					logger.info("Discovered "+ services.size() + " caGrid services.");

        					Iterator<CaGridService> iter = services.iterator(); // services' iterator
        					while((!Thread.currentThread().isInterrupted()) && iter.hasNext()){
        						
        						CaGridService caGridService = (CaGridService) iter.next();
        						      						
            					try{
	            					List<String> operationNames = caGridService.getOperations();
	            					logger.info("Found caGrid service: "+ caGridService.getServiceName());	
        							logger.info("Discovered " + operationNames.size() + " operation(s) of the service.");
	            					
	            					// Get security metadata for all operations/methods of this service
	            					// by invoking getServiceSecurityMetadata() method on the service
	            					ServiceSecurityClient ssc = null;
	            					try {
	            						ssc = new ServiceSecurityClient(caGridService.getServiceWSDLLocation());
	            					} catch (MalformedURIException e) {
	            						// TODO Auto-generated catch block
	            						e.printStackTrace();
	            					} catch (RemoteException e) {
	            						// TODO Auto-generated catch block
	            						e.printStackTrace();
	            					}
	            					ServiceSecurityMetadata securityMetadata = null;
	            					if (ssc != null) {
	            						try {
	            							securityMetadata = ssc.getServiceSecurityMetadata();
	            						} catch (RemoteException e) {
	            							// TODO Auto-generated catch block
	            							e.printStackTrace();
	            						}
	            					}
	            					// Get all secure OperationS of the service which security properties differ from the 
	            					// default security properties for the service itself and map them to their names.
	            					// Only operations which security properties are different from those of the service itself 
	            					// will be detected here - whether because they require more stringent or more loose security.
	            					Map<String, Operation> secureOperationsMap = new HashMap<String, Operation>();
	            					ServiceSecurityMetadataOperations ssmo = null; 
	            					if (securityMetadata != null){
	            						ssmo = securityMetadata.getOperations(); // all operations of the service requiring GSI security properties
	            					}
	            					if (ssmo != null) {
	            						Operation[] ops = ssmo.getOperation();
	            						if (ops != null) {
	            							for (int i = 0; i < ops.length; i++) {
	            								/*String lowerMethodName = ops[i].getName().substring(0, 1)
	            										.toLowerCase()
	            										+ ops[i].getName().substring(1);
	            								secureOperationsMap.put(lowerMethodName, ops[i]);
	            								System.out.print(" " + lowerMethodName);
	            								System.out.println();
	            								*/
	            								secureOperationsMap.put(ops[i].getName(), ops[i]);
	            							}
	            						}
	            					}
	            					
	            					for (String operation : operationNames) {
	            						logger.info("Adding operation: "+ operation + " for service " + caGridService.getServiceName());
	            						// An ActivityItem corresponds to an operation.
	            						// Service contains service metadata -- no wsdl parser is needed?
	            						// We can add a parser to parse it if we need more details on those services
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
	            						
	            						CommunicationMechanism serviceDefaultCommunicationMechanism = securityMetadata.getDefaultCommunicationMechanism();
	            						CommunicationMechanism communicationMechanism = null;
	            						if (secureOperationsMap.containsKey(operation)) {
	            							Operation op = (Operation) secureOperationsMap.get(operation);
	            							communicationMechanism = op.getCommunicationMechanism(); // specific for this operation, may differ from service default
	            						} else {
	            							communicationMechanism = serviceDefaultCommunicationMechanism;
	            						}
	            						
	            						// Configure security properties for the operation, if any
            							try{
            								logger.info("Configuring security properties for operation: "+ operation);
            								CaGridQuery.configureSecurity(caGridService,
            										communicationMechanism,
            										indexServiceURL,
            										authenticationServicesMap.get(indexServiceURL), 
            										dorianServicesMap.get(indexServiceURL), 
            										item);
            							}
            							catch(Exception ex){
            								logger.error(ex.getMessage() + 
            												". Skipping this operation.");
            								ex.printStackTrace();
            								continue; // skip this operation
            							}

		            					searchResultsActivityItems.add(item);
	            					}
	            					updateSearchResultsList(caGridService.getServiceName());
	        						serviceCounter ++;
	            					Thread.sleep(500); // Give a chance to another thread to update the services JList
            					}
        		                catch (InterruptedException intrex) {
        		                    // Allow thread to exit 
        		                	searchStopped = true;
        		                    Thread.currentThread().interrupt();
        		                }
        		                catch (Exception ex) { // anything else that went wrong
        		    				logger.error(ex);
        		                	continue; // skip this service
        		                }
        						
        					}
        					
            				if (searchStopped){
            					logger.info("caGrid service search stopped. Found " + serviceCounter + " services.");
            					updateSearchStatus("Status: search stopped. Found " + serviceCounter + " services.");
            				}
            				else{
            					logger.info("caGrid service search complete. Found " + serviceCounter + " services.");
            					updateSearchStatus("Status: search done. Found " + serviceCounter + " services.");
            				}
            			}	
					}					
				};
				searchThread.start();
            }
        });
        JButton stopButton = new JButton("Stop search");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	Thread stopThread = new Thread("Stopping the search for caGrid services"){
            		public void run(){
                    	if ((searchThread != null) && searchThread.isAlive()){
                    	    updateSearchStatus("Status: stopping search...");
                    		searchThread.interrupt();            		
                    		while(!searchThread.getState().equals(State.TERMINATED)){
                    			continue;
                    		}
                    	}
                    	//updateSearchStatus("Status: search stopped."); // status will be updated from the searchThread
            		}
            	};
            	stopThread.start();
            }
        });
        queryButtonsPanel.add(searchButton);
        queryButtonsPanel.add(stopButton);
        queryButtonsPanel.add(updateCaDSRDataButton);
        serviceQueryPanel.add(queryButtonsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(serviceQueryPanel, BorderLayout.CENTER);

        // Search results panel
        JPanel searchResultsPanel = new JPanel(new BorderLayout());
        searchResultsPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder(EtchedBorder.LOWERED)));
        searchResultsActivityItems = new ArrayList<CaGridActivityItem>();
        searchResultsListModel = new DefaultListModel();
        searchResultsList = new JList(searchResultsListModel);	
        searchResultsScrollPane = new JScrollPane(searchResultsList);
        JPanel searchScrollPanel = new JPanel(new BorderLayout());
        searchScrollPanel.add(searchResultsScrollPane, BorderLayout.CENTER);
        searchScrollPanel.add(searchStatusLabel, BorderLayout.SOUTH);
        JPanel searchResultsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectAllButton = new JButton("Select all");
        selectAllButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			    int start = 0;
			    int end = searchResultsListModel.getSize()-1;
			    if (end >= 0) {
			    	searchResultsList.setSelectionInterval(start, end); 
			    }
			}
        });
        searchResultsButtonsPanel.add(selectAllButton);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				searchResultsListModel.removeAllElements();
			}
        });
        //searchResultsButtonsPanel.add(clearButton);
        JButton addServicesButton = new JButton("Add services");
        searchResultsButtonsPanel.add(addServicesButton);
        searchResultsPanel.add(searchScrollPanel, BorderLayout.CENTER);
        searchResultsPanel.add(searchResultsButtonsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(searchResultsPanel, BorderLayout.SOUTH);
        
        this.getContentPane().add(mainPanel);
        this.setLocation(50,50);
        this.pack();
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
            	closeDialog();
            }
        });
        this.setVisible(true);
    }
    
    private void updateSearchStatus(final String newStatus) {
    	  SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
      	    	synchronized(searchStatusLabel){
      	    		searchStatusLabel.setText(newStatus);
      	    	}
    	    }
    	  });
    }
    
    private void updateSearchResultsList(final String operation) {
  	  SwingUtilities.invokeLater(new Runnable() {
  	    public void run() {
  	    	synchronized(searchResultsListModel){
  	  	      // Append an item
  	  	      searchResultsListModel.addElement(operation);
  	    	}
  	    }
  	  });
  }


    private void closeDialog(){
    	if ((searchThread != null) && searchThread.isAlive()){
    		searchThread.interrupt();
    	}
        setVisible(false);
        dispose();
    }
    
    /**
     * 
     * @return the selected Index Service URL
     */
    public String getIndexServiceURL() {
        return (String) indexServicesURLsComboBox.getSelectedItem();
    }
    
    /**
     * 
     * @return the Authentication Service URL that corresponds to the selected Index Service
     */
 /*   public String getAuthenticationServiceURL() {
        return (String) authenticationServicesURLs[indexServiceURLs.getSelectedIndex()];
    }*/
    
    /**
     * 
     * @return the Dorian Service URL that corresponds to the selected Index Service
     */
 /*   public String getDorianServiceURL() {
        return (String) dorianServicesURLs[indexServiceURLs.getSelectedIndex()];
    }*/

    /**
     * 
     * @return the string representation of the i-th QueryCriteria
     */
    public String getQueryCriteria(int i) {
        return (String) queryList[i].getSelectedItem();
    }
    
    /**
     * 
     * @return the string representation of the i-th QueryValue
     */
    public String getQueryValue(int i) {
        return (String) queryValue[i].getSelectedItem();
    }
    
    /*
     // Sets the busy cursor while action is being performed.
      private final static class CursorController {
    	private CursorController() {}
    	
    	public static ActionListener createListener(final Component component, final ActionListener mainActionListener) {
    		ActionListener actionListener = new ActionListener() {
        
    	    	public void actionPerformed(ActionEvent ae) {
    	    		try {
    	    			component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	    			mainActionListener.actionPerformed(ae);
    	    		} 
    	    		finally { // restore the cursor to normal state
    	    			component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    	    		}
    	        }
    	      };
      
    	     return actionListener;   
    	}
    }*/
    
	// Give an initial value to classNameArray.
	// The data is retrieved at 8:30 am, June 16th, 2008.
	public static String []classNameArray = {
		"A2Conjugate", 
		"A2Experiment", "A2LP4Parameters", "A2Plate", "A2Sample", 
		"A2Spot42", "A2SpotData", "A2SpotSetup", "A2SpotsStatistics", 
		"A2StandardCurve", "A2Well", "AbsoluteCodingSchemeVersionReference", "AbsoluteCodingSchemeVersionReferenceList", 
		"AbsoluteNeutrophilCount", "AbstractAdverseEventTerm", "AbstractArrayData", "AbstractBioMaterial", 
		"AbstractCaArrayEntity", "AbstractCaArrayObject", "AbstractCancerModel", "AbstractCharacteristic", 
		"AbstractContact", "AbstractDataColumn", "AbstractDesignElement", "AbstractDomainObject", 
		"Abstraction", "AbstractMeddraDomain", "AbstractProbe", "AbstractProbeAnnotation", 
		"AbstractStudyDisease", "Accession", "AccessionCharacteristics", "AccessRights", 
		"AcquisitionProcedure", "ActionSuccessor", "ActivationMethod", "ActiveIngredient", 
		"ActiveObservation", "ActiveSite", "Activity", "ActivityRelationship", 
		"ActivitySummary", "ActivityType", "AcuteGraftVersusHostDisease", "Add", 
		"AdditionalFindings", "AdditionalInformation", "AdditionalOrganismName", "Address", 
		"AddToReferenceParameters", "AdjacencyMatrix", "AdministeredComponent", "AdministeredComponentClassSchemeItem", 
		"AdministeredComponentContact", "AdministeredDrug", "AdverseEvent", "AdverseEventAttribution", 
		"AdverseEventCtcTerm", "AdverseEventDetail", "AdverseEventMeddraLowLevelTerm", "AdverseEventNotification", 
		"AdverseEventResponseDescription", "AdverseEventTherapy", "AeTerminology", "Agent", 
		"AgentOccurrence", "AgentSynonym", "AgentTarget", "Alignment", 
		"Aliquot", "Allergy", "AlternateProteinHit", "Amendment", 
		"AmendmentApproval", "AnalysisGroup", "AnalysisGroupResult", "AnalysisParameters", 
		"AnalysisRecord", "AnalysisRoutine", "AnalysisRun", "AnalysisRunSet", 
		"AnalysisVariable", "Analyte", "AnalyteProcessingStep", "AnatomicEntity", 
		"AnatomicSite", "Animal", "AnimalAvailability", "AnimalDistributor", 
		"AnimalModel", "AnnotatableEntity", "AnnotatableEvent", "Annotation", 
		"AnnotationEventParameters", "AnnotationManager", "AnnotationOfAnnotation", "AnnotationSet", 
		"Anomaly", "Antibody", "Antigen", "Application", 
		"ApplicationContext", "AppliedParameter", "ApprovalStatus", "AracneParameter", 
		"Array", "ArrayDataType", "ArrayDesign", "ArrayDesignDetails", 
		"ArrayGroup", "ArrayManufacture", "ArrayManufactureDeviation", "ArrayReporter", 
		"ArrayReporterCytogeneticLocation", "ArrayReporterPhysicalLocation", "Assay", "AssayDataPoint", 
		"AssayType", "Assembly", "Assessment", "AssessmentRelationship", 
		"AssociatedElement", "AssociatedFile", "AssociatedObservationWrapper", "Association", 
		"AssociativeFunction", "Atom", "Attachment", "AttributeSetDescriptor", 
		"AttributeTypeMetadata", "Audit", "AuditEvent", "AuditEventDetails", 
		"AuditEventLog", "AuditEventQueryLog", "Availability", "AveragedSpotInformation", 
		"BACCloneReporter", "BaselineData", "BaselineHistoryPE", "BaselineStage", 
		"BasicHistologicGrade", "BehavioralMeasure", "BibliographicReference", "BindingSite", 
		"BioAssay", "BioAssayCreation", "BioAssayData", "BioAssayDataCluster", 
		"BioAssayDatum", "BioAssayDimension", "BioAssayMap", "BioAssayMapping", 
		"BioAssayTreatment", "BiocartaMap", "BiocartaReport", "BiocartaSource", 
		"BioDataCube", "BioDataTuples", "BioDataValues", "BioEvent", 
		"Biohazard", "BiologicalProcess", "BioMaterial", "BioMaterial_package", 
		"BioMaterialMeasurement", "Biopolymer", "BioSample", "BioSequence", 
		"BioSource", "BlackoutDate", "BloodContact", "BooleanColumn", 
		"BooleanParameter", "BreastCancerAccessionCharacteristics", "BreastCancerBiomarkers", "BreastCancerTNMFinding", 
		"BreastNegativeSurgicalMargin", "BreastPositiveSurgicalMargin", "BreastSpecimenCharacteristics", "BreastSurgicalPathologySpecimen", 
		"CaArrayFile", "CalciumBindingRegion", "CalculatedMeasurement", "CalculatedMeasurementProtocol", 
		"Calculation", "CalculationResult", "Canceled", "CancerModel", 
		"CancerResearchGroup", "CancerStage", "CancerTNMFinding", "Capacity", 
		"CarbonNanotube", "CarbonNanotubeComposition", "CarcinogenExposure", "CarcinogenicIntervention", 
		"caseDetail", "CaseReportForm", "Caspase3Activation", "CatalystActivity", 
		"Category", "CategoryObservation", "CategorySummaryExport", "CategorySummaryReportRow", 
		"CD", "CellLine", "CellLysateFinding", "cells", 
		"CellSpecimen", "CellSpecimenRequirement", "CellSpecimenReviewParameters", "CellViability", 
		"CentralLaboratory", "CFU_GM", "cghSamples", "Chain", 
		"Change", "ChangedGenes", "Channel", "Characterization", 
		"CharacterizationProtocol", "CharacterParameter", "CheckInCheckOutEventParameter", "ChemicalAssociation", 
		"ChemicalClass", "ChemicalStressor", "ChemicalStressorProtocol", "ChemicalTreatment", 
		"Chemotaxis", "Chemotherapy", "ChemotherapyData", "ChimerismSample", 
		"ChromatogramPoint", "Chromosome", "ChromosomeMap", "ChronicGraftVersusHostDisease", 
		"Chunk", "Circle", "citation", "ClassComparisonAnalysis", 
		"ClassComparisonAnalysisFinding", "ClassificationScheme", "ClassificationSchemeItem", "ClassificationSchemeItemRelationship", 
		"ClassificationSchemeRelationship", "ClassMembership", "ClassSchemeClassSchemeItem", "ClinicalAssessment", 
		"ClinicalFinding", "ClinicalMarker", "ClinicalReport", "ClinicalResult", 
		"ClinicalTrial", "ClinicalTrialProtocol", "ClinicalTrialSite", "ClinicalTrialSponsor", 
		"ClinicalTrialSubject", "Clone", "CloneRelativeLocation", "Cluster", 
		"CNSAccessionCharacteristics", "CNSCarcinoma", "CNSHistologicGrade", "CNSNeoplasmHistologicType", 
		"CNSSpecimenCharacteristics", "Coagulation", "codedContext", "codedEntry", 
		"CodedNodeSetImpl", "CodeSequence", "CodingSchemeRendering", "CodingSchemeRenderingList", 
		"CodingSchemeSummary", "CodingSchemeSummaryList", "CodingSchemeTag", "CodingSchemeTagList", 
		"CodingSchemeURNorName", "CodingSchemeVersionOrTag", "CodingSchemeVersionStatus", "Coefficients", 
		"Cohort", "CoiledCoil", "CoiledCoilRegion", "Coils", 
		"CollaborativeStaging", "CollectionEventParameters", "CollectionProtocol", "CollectionProtocolEvent", 
		"CollectionProtocolRegistration", "CollisionCell", "ColorectalAccessionCharacteristics", "ColorectalCancerTNMFinding", 
		"ColorectalHistologicGrade", "ColorectalSpecimenCharacteristics", "CometScores", "comment", 
		"Comment", "Comments", "CommonLookup", "Comorbidity", 
		"ComparativeMarkerSelectionParameterSet", "ComparativeMarkerSelectionResultCollection", "ComplementActivation", "Complex", 
		"ComplexComponent", "ComplexComposition", "ComponentConcept", "ComponentLevel", 
		"ComponentName", "ComposingElement", "CompositeCompositeMap", "CompositeGroup", 
		"CompositePosition", "CompositeSequence", "CompositeSequenceDimension", "CompositeSequenceSummary", 
		"CompositionallyBiasedRegion", "Compound", "CompoundMeasurement", "ConcentrationUnit", 
		"Concept", "ConceptClassification", "conceptCode", "ConceptCodes", 
		"ConceptDerivationRule", "ConceptDescriptor", "conceptProperty", "ConceptReference", 
		"ConceptReferenceList", "ConceptReferent", "concepts", "ConceptualDomain", 
		"ConcomitantMedication", "ConcomitantMedicationAttribution", "ConcomitantMedicationDetail", "ConcomitantProcedure", 
		"ConcomitantProcedureDetail", "Condition", "Conditional", "Conditionality", 
		"ConditionGroup", "ConditionMessage", "ConfidenceIndicator", "ConsensusClusteringParameterSet", 
		"ConsensusClusterResultCollection", "ConsensusIdentifierData", "ConsensusMatrix", "ConsensusMatrixRow", 
		"Constraint", "Contact", "ContactCommunication", "ContactDetails", 
		"ContactInfo", "ContactMechanismBasedRecipient", "ContactMechanismType", "Container", 
		"ContainerInfo", "ContainerType", "Context", "context", 
		"Contour", "Control", "ControlGenes", "ControlledVocabularyAnnotation", 
		"Coordinate", "CopyNumberFinding", "CourseAgent", "CourseAgentAttribution", 
		"CourseDate", "CrossLink", "Ctc", "CtcCategory", 
		"CtcGrade", "CtcTerm", "CtepStudyDisease", "Culture", 
		"CultureProtocol", "CurationData", "CustomProperties", "CutaneousMelanoma", 
		"CutaneousMelanomaAccessionCharacteristics", "CutaneousMelanomaAdditionalFindings", "CutaneousMelanomaNegativeSurgicalMargin", "CutaneousMelanomaNeoplasmHistologicType", 
		"CutaneousMelanomaPositiveSurgicalMargin", "CutaneousMelanomaSpecimenCharacteristics", "CutaneousMelanomaSurgicalPathologySpecimen", "CutaneousMelanomaTNMFinding", 
		"Cycle", "Cytoband", "CytobandPhysicalLocation", "CytogeneticLocation", 
		"CytokineInduction", "Cytotoxicity", "Data", "Database", 
		"DatabaseCrossReference", "DatabaseEntry", "DatabaseSearch", "DatabaseSearchParameters", 
		"DatabaseSearchParametersOntologyEntry", "DataElement", "DataElementConcept", "DataElementConceptRelationship", 
		"DataElementDerivation", "DataElementRelationship", "DataFile", "DataFileLimsFile", 
		"DataItem", "DataRetrievalRequest", "DataServiceInstance", "DataSet", 
		"DataSource", "DataStatus", "DataType", "Datum", 
		"dbxref", "dc", "DeathSummary", "DefinedParameter", 
		"Definition", "definition", "DefinitionClassSchemeItem", "DeliveryStatus", 
		"Delta", "Demographics", "Dendrimer", "DendrimerComposition", 
		"Department", "DerivationType", "DerivedArrayData", "DerivedBioAssay", 
		"DerivedBioAssayData", "DerivedBioAssays", "DerivedDataElement", "DerivedDataFile", 
		"DerivedDatum", "DerivedDNACopySegment", "DerivedSignal", "DescLogicConcept", 
		"DescLogicConceptVocabularyName", "describable", "Describable", "Description", 
		"Designation", "DesignationClassSchemeItem", "DesignElement", "DesignElementDimension", 
		"DesignElementGroup", "DesignElementList", "DesignElementMap", "DesignElementMapping", 
		"Detection", "DeviceAttribution", "DeviceOperator", "Diagnosis", 
		"DICOMImageReference", "DiffFoldChangeFinding", "Dige", "DigeGel", 
		"DigeSpotDatabaseSearch", "DigeSpotMassSpec", "Dimension", "DirectedAcyclicGraph", 
		"Disease", "DiseaseAttribution", "DiseaseCategory", "DiseaseEvaluation", 
		"DiseaseEvaluationDetail", "DiseaseExtent", "DiseaseHistory", "DiseaseOntology", 
		"DiseaseOntologyRelationship", "DiseaseOutcome", "DiseaseResponse", "DiseaseTerm", 
		"DiseaseTerminology", "DisposalEventParameters", "DistanceUnit", "DistantSite", 
		"DistributedItem", "Distribution", "DistributionProtocol", "DistributionProtocolAssignment", 
		"DistributionProtocolOrganization", "DisulfideBond", "DNA", "DNABindingRegion", 
		"DNAcopyAssays", "DNAcopyParameter", "DNASpecimen", "Documentation", 
		"Domain", "DomainDescriptor", "DomainName", "Donor", 
		"DonorInfo", "DonorRecipient", "Dose", "DoubleColumn", 
		"DoubleParameter", "Drug", "DrugSurgeryData", "Duration", 
		"EdgeProperties", "EditActionDate", "Electrospray", "Ellipse", 
		"EmbeddedEventParameters", "Emulsion", "EmulsionComposition", "Encapsulation", 
		"EndpointCode", "EngineeredGene", "EnsemblGene", "EnsemblPeptide", 
		"EnsemblTranscript", "EntityAccession", "entityDescription", "EntityMap", 
		"EntityName", "EntrezGene", "EnucleationInvasiveProstateCarcinoma", "EnucleationProstateSpecimenCharacteristics", 
		"EnucleationProstateSurgicalPathologySpecimen", "EnumeratedValueDomain", "EnvironmentalFactor", "EnzymeInduction", 
		"Epoch", "EpochDelta", "Equipment", "Error", 
		"Event", "EventEntity", "EventEntitySet", "EventParameters", 
		"EventRecords", "Evidence", "EvidenceCode", "EvidenceKind", 
		"EVSDescLogicConceptSearchParams", "EVSHistoryRecordsSearchParams", "EVSMetaThesaurusSearchParams", "EVSSourceSearchParams", 
		"Examination", "ExcisionCutaneousMelanomaSpecimenCharacteristics", "ExcisionCutaneousMelanomaSurgicalPathologySpecimen", "Execution", 
		"ExocrinePancreasAccessionCharacteristics", "ExocrinePancreasSpecimenCharacteristics", "ExocrinePancreaticCancerTNMFinding", "Exon", 
		"ExonArrayReporter", "ExonProbeAnnotation", "ExpectedValue", "ExpeditedAdverseEventReport", 
		"Experiment", "Experiment2DGelList", "Experiment2DLiquidChromatography", "Experiment2DLiquidChromatography1stSetup", 
		"Experiment2DLiquidChromatography2ndSetup", "ExperimentalFactor", "ExperimentalFeatures", "ExperimentalStructure", 
		"ExperimentContact", "ExperimentDesign", "ExperimentRun", "ExperimentTo2DGel", 
		"ExperimentToDatabaseSearch", "ExperimentToDige", "ExperimentToMassSpec", "Exponent", 
		"ExportStatus", "ExpressedSequenceTag", "ExpressionArrayReporter", "ExpressionData", 
		"ExpressionFeature", "ExpressionLevelDesc", "ExpressionProbeAnnotation", "ExpressoParameter", 
		"Extendable", "ExtensionDescription", "ExtensionDescriptionList", "ExternalIdentifier", 
		"ExternalReference", "Extract", "Facility", "Factor", 
		"FactorValue", "Failed", "FamilyHistory", "FastaFiles", 
		"FastaSequences", "Feature", "FeatureData", "FeatureDefect", 
		"FeatureDimension", "FeatureExtraction", "FeatureGroup", "FeatureInformation", 
		"FeatureLocation", "FeatureReporterMap", "FeatureType", "FemaleReproductiveCharacteristic", 
		"Ffas", "Fiducial", "File", "FileType", 
		"Filters", "Finding", "FirstCourseRadiation", "FirstCourseTreatmentSummary", 
		"FISHFinding", "FixedEventParameters", "FloatColumn", "FloatingPointQuantity", 
		"FloatParameter", "FluidSpecimen", "FluidSpecimenRequirement", "FluidSpecimenReviewEventParameters", 
		"Fold", "Folder", "Followup", "Form", 
		"FormatType", "FormElement", "Fraction", "FractionAnalyteSteps", 
		"Fractions", "FrozenEventParameters", "FuhrmanNuclearGrade", "Fullerene", 
		"FullereneComposition", "Function", "functionalCategory", "FunctionalDNADomain", 
		"FunctionalizingEntity", "FunctionalProteinDomain", "FunctionalRole", "GbmDrugs", 
		"GbmPathology", "GbmSlide", "GbmSurgery", "Gel", 
		"Gel2d", "GelImage", "GelImageType", "GelPlug", 
		"GelSpot", "GelSpotList", "GelStatus", "Genbank", 
		"GenBankAccession", "GenBankmRNA", "GenBankProtein", "Gene", 
		"gene_product", "GeneAgentAssociation", "GeneAlias", "GeneAnnotation", 
		"GeneBiomarker", "GeneCategoryExport", "GeneCategoryMatrix", "GeneCategoryMatrixRow", 
		"GeneCategoryReportRow", "GeneCytogeneticLocation", "GeneDelivery", "GeneDiseaseAssociation", 
		"GeneExprReporter", "GeneFunction", "GeneFunctionAssociation", "GeneGenomicIdentifier", 
		"GeneNeighborsParameterSet", "GeneOntology", "GeneOntologyRelationship", "GenePhysicalLocation", 
		"GenePubmedSummary", "GeneRelativeLocation", "GeneReporterAnnotation", "GenericArray", 
		"GenericReporter", "GeneticAlteration", "GeneVersion", "GenomeEncodedEntity", 
		"GenomicIdentifier", "GenomicIdentifierSet", "GenomicIdentifierSolution", "GenomicSegment", 
		"Genotype", "GenotypeDiagnosis", "GenotypeFinding", "GenotypeSummary", 
		"Genus", "GeometricShape", "GleasonHistologicGrade", "GleasonHistopathologicGrade", 
		"GlycosylationSite", "GominerGene", "GominerTerm", "GOTerm", 
		"Grade", "GradientStep", "Graft", "GraftVersusHostDisease", 
		"GraftVersusHostDiseaseOutcome", "Group", "GroupRoleContext", "Hap2Allele", 
		"Haplotype", "Hardware", "HardwareApplication", "header", 
		"HealthcareSite", "HealthCareSite", "HealthcareSiteInvestigator", "HealthcareSiteParticipant", 
		"HealthcareSiteParticipantRole", "Helix", "HematologyChemistry", "Hemolysis", 
		"HemTransplantEndocrineProcedure", "Hexapole", "HierarchicalCluster", "HierarchicalClusteringMage", 
		"HierarchicalClusteringParameter", "HierarchicalClusterNode", "HighLevelGroupTerm", "HighLevelTerm", 
		"HistologicGrade", "Histology", "HistopathologicGrade", "Histopathology", 
		"HistopathologyGrade", "History", "HistoryRecord", "Hmmpfam", 
		"HomologAlignment", "HomologousAssociation", "HormoneTherapy", "Hybridization", 
		"HybridizationData", "Hypothesis", "Identifiable", "IdentificationScheme", 
		"IdentifiedPathologyReport", "IdentifiedPatient", "IdentifiedSection", "Identifier", 
		"IHCFinding", "II", "Image", "ImageAcquisition", 
		"ImageAnnotation", "ImageContrastAgent", "ImageDataItem", "ImageReference", 
		"ImageType", "ImageView", "ImageViewModifier", "Imaging", 
		"ImagingFunction", "ImagingObservation", "ImagingObservationCharacteristic", "ImmuneCellFunction", 
		"Immunologic", "Immunotherapy", "Immunotoxicity", "ImmunoToxicity", 
		"INDHolder", "InducedMutation", "InitiatorMethionine", "Input", 
		"InputFile", "Instance", "Institution", "Instruction", 
		"instruction", "Instrument", "InstrumentConfiguration", "InstrumentType", 
		"IntegerColumn", "IntegerParameter", "IntegerQuantity", "IntegrationType", 
		"Interaction", "InternetSource", "Intron", "InvasiveBreastCarcinoma", 
		"InvasiveBreastCarcinomaNeoplasmHistologicType", "InvasiveColorectalCarcinoma", "InvasiveColorectalCarcinomaNeoplasmHistologicType", "InvasiveExocrinePancreaticCarcinoma", 
		"InvasiveExocrinePancreaticCarcinomaNeoplasmHistologicType", "InvasiveKidneyCarcinoma", "InvasiveKidneyCarcinomaNeoplasmHistologicType", "InvasiveLungCarcinoma", 
		"InvasiveLungCarcinomaNeoplasmHistologicType", "InvasiveProstateCarcinoma", "InvasiveProstateCarcinomaNeoplasmHistologicType", "Investigation", 
		"InvestigationalNewDrug", "Investigator", "InvestigatorHeldIND", "InvitroCharacterization", 
		"InvivoResult", "Invoker", "IonSource", "IonTrap", 
		"JaxInfo", "JpegImage", "Jpred", "Keyword", 
		"KidneyAccessionCharacteristics", "KidneyAdditionalFindings", "KidneyCancerTNMFinding", "KidneySpecimenCharacteristics", 
		"Lab", "LabeledExtract", "LabFile", "LabGeneral", 
		"LabGroup", "LabMember", "Laboratory", "LaboratoryEquipment", 
		"LaboratoryFinding", "LaboratoryPersonnel", "LaboratoryProject", "LaboratoryResult", 
		"LaboratorySamplePlate", "LaboratoryStorageDevice", "LaboratoryTest", "LabSpecial", 
		"LabValue", "Lambda", "LesionDescription", "LesionEvaluation", 
		"LeukocyteProliferation", "LevelOfExpressionIHCFinding", "LexBIGServiceImpl", "Library", 
		"LimsFile", "Lineage", "Linkage", "LinkType", 
		"LipidMoietyBindingRegion", "Liposome", "LiposomeComposition", "LiquidChromatographyColumn", 
		"List", "ListProcessing", "LiteratureRelationship", "LoadStatus", 
		"localId", "LocalNameList", "Location", "Log", 
		"LogEntry", "LogicalProbe", "LogLevel", "LOHFinding", 
		"LongColumn", "LongParameter", "LongTermFU", "LossOfExpressionIHCFinding", 
		"LowComplexityRegion", "LowLevelTerm", "Lsid", "LungAccessionCharacteristics", 
		"LungCancerTNMFinding", "LungDrugs", "LungExam", "LungNeoplasm", 
		"LungPathology", "LungSlide", "LungSpecimenCharacteristics", "LungSurgery", 
		"Macroprocess", "MAGE", "Maldi", "ManufactureLIMS", 
		"ManufactureLIMSBiomaterial", "Manufacturer", "Map", "Mapping", 
		"Marker", "MarkerAlias", "MarkerPhysicalLocation", "MarkerRelativeLocation", 
		"MarkerResult", "MarketingAuthorization", "MarketingAuthorizationHolderManufacturerDistributor", "MascotScores", 
		"MassQuery", "MassSpecDatabaseSearch", "MassSpecExperiment", "MassSpecMachine", 
		"MassSpecMassSpecFraction", "MassUnit", "MatchingParameters", "Material", 
		"MaterialSource", "MaterialType", "MathFile", "MeasuredBioAssay", 
		"MeasuredBioAssayData", "MeasuredBioAssays", "MeasuredSignal", "Measurement", 
		"MeasurementCharacteristic", "MeasurementProtocol", "MeasureUnit", "Meddra", 
		"MeddraStudyDisease", "MedicalDevice", "MedicinalProduct", "MessengerRNA", 
		"MetadataProperty", "MetadataPropertyList", "MetalIonBindingSite", "MetalParticle", 
		"MetalParticleComposition", "MetastasisSite", "MetastaticDiseaseSite", "MetaThesaurusConcept", 
		"Method", "MethodParameter", "Methylation", "MethylationDnaSequence", 
		"MethylationSite", "MethylationSiteMeasurement", "Microarray", "MicroArrayData", 
		"MicroarrayEventRecords", "MicroarraySet", "MismatchInformation", "Missed", 
		"MobilePhaseComponent", "Model", "ModelGroup1", "Modeller", 
		"ModelSection", "ModelStructure", "Modifications", "ModificationType", 
		"ModifiedResidue", "Module", "ModuleDescription", "ModuleDescriptionList", 
		"MolecularSpecimen", "MolecularSpecimenRequirement", "MolecularSpecimenReviewParameters", "MolecularWeight", 
		"Morpholino", "Morphology", "Mouse", "mRNAGenomicIdentifier", 
		"MS2Runs", "Msi", "MultiPoint", "MultiProcessParameters", 
		"MutagenesisSite", "MutationIdentifier", "MutationVariation", "MZAnalysis", 
		"MzAssays", "MzSpectrum", "MZXMLSubFiles", "Name", 
		"NameAndValue", "NameAndValueList", "NameValueType", "Nanoparticle", 
		"NanoparticleDatabaseElement", "NanoparticleEntity", "NanoparticleSample", "NanoparticleStudy", 
		"NeedleBiopsyInvasiveProstateCarcinoma", "NeedleBiopsyProstateSpecimenCharacteristics", "NeedleBiopsyProstateSurgicalPathologySpecimen", "NegativeControl", 
		"Neoplasm", "NeoplasmHistologicType", "NeoplasmHistopathologicType", "NKCellCytotoxicActivity", 
		"Node", "NodeContents", "NodeValue", "Noise", 
		"Nomenclature", "NonCancerDirectedSurgery", "NonConsecutiveResidues", "NonenumeratedValueDomain", 
		"NonTerminalResidue", "NonverifiedSamples", "NormalChromosome", "NormalizeInvariantSetParameter", 
		"NormalizeMethodParameter", "NormalizeQuantilesRobustParameter", "NotApplicable", "NotificationBodyContent", 
		"NottinghamHistologicGrade", "NottinghamHistopathologicGrade", "NucleicAcidPhysicalLocation", "NucleicAcidSequence", 
		"NucleotideBindingRegion", "NucleotidePhosphateBindingRegion", "Null", "NumericalRangeConstraint", 
		"NumericMeasurement", "NumericOID", "ObjectClass", "ObjectClassRelationship", 
		"Observation", "ObservationConcept", "ObservationData", "ObservationProtocol", 
		"ObservationRelationship", "ObservationState", "ObservedThing", "ObservedThingToObservationConnection", 
		"ObservedThingToObservedThingRelationship", "ObservedThingToOntologyElementRelationship", "Occurred", "OctaveFile", 
		"OddsRatio", "OffTreatment", "OMIM", "OnStudy", 
		"OntologyDimension", "OntologyElement", "OntologyElementSlot", "OntologyElementSlotSet", 
		"OntologyElementToOntologyElementRelationship", "OntologyEntry", "OntologyGroup", "OntologyGroupToOntologyGroupRelationship", 
		"OrderItem", "OrderOfNodeTraversal", "OrderSet", "Organ", 
		"Organelle", "Organism", "OrganismName", "Organization", 
		"OrganizationAssignedIdentifier", "OrganizationHeldIND", "OrganOntology", "OrganOntologyRelationship", 
		"OrthologousGene", "OtherAnalyte", "OtherAnalyteAnalyteProcessingSteps", "OtherAnalyteOntologyEntry", 
		"OtherAnalyteProcessingSteps", "OtherAnalyteProcessingStepsOntologyEntry", "OtherBreastCancerHistologicGrade", "OtherBreastCancerHistopathologicGrade", 
		"OtherCause", "OtherCauseAttribution", "OtherChemicalAssociation", "OtherFunction", 
		"OtherFunctionalizingEntity", "OtherIonisation", "OtherIonisationOntologyEntry", "OtherMZAnalysis", 
		"OtherMZAnalysisOntologyEntry", "OtherNanoparticleEntity", "OtherProcedure", "OtherTarget", 
		"OtherTherapy", "Outcome", "OutcomeType", "Output", 
		"OutputFile", "OvarianDrugs", "OvarianExam", "OvarianNeoplasm", 
		"OvarianPathology", "OvarianSlide", "OvarianSurgery", "OxidativeBurst", 
		"OxidativeStress", "Package", "Parameter", "Parameterizable", 
		"ParameterizableApplication", "ParameterList", "ParameterValue", "Participant", 
		"ParticipantEligibilityAnswer", "ParticipantHistory", "ParticipantMedicalIdentifier", "Participation", 
		"ParticleComposition", "Party", "PartyRole", "Password", 
		"Pathology", "PathologyEventRecords", "PathologyReport", "Pathway", 
		"Patient", "PatientIdentifier", "PatientVisit", "Pdb", 
		"Pdbblast", "Peak", "PeakDetectionParameters", "PeakList", 
		"PeakLocation", "PeakSpecificChromint", "PedigreeGraph", "PedigreeNode", 
		"Peptide", "PeptideHit", "PeptideHitModifications", "PeptideHitOntologyEntry", 
		"PeptideHitProteinHit", "PeptideMembers", "PeptidesBase", "Percentile", 
		"PercentX", "PerformingLaboratory", "Period", "PeriodDelta", 
		"PermissibleValue", "Person", "person", "PersonContact", 
		"PersonName", "Personnel", "PersonOccupation", "PETEvaluation", 
		"PETEvaluationDetail", "Phagocytosis", "PharmaceuticalProduct", "Pharmacokinetics", 
		"Phase", "Phenomenon", "PhenomenonType", "Phenotype", 
		"PhenotypeDiagnosis", "PhysicalArrayDesign", "PhysicalBioAssay", "PhysicalCharacterization", 
		"PhysicalEntity", "PhysicalExam", "PhysicalLocation", "PhysicalParticipant", 
		"PhysicalPosition", "PhysicalProbe", "PhysicalState", "Physician", 
		"Place", "PlannedActivity", "PlannedActivityDelta", "PlannedCalendar", 
		"PlannedCalendarDelta", "PlannedEmailNotification", "PlannedNotification", "PlasmaProteinBinding", 
		"Platelet", "PlateletAggregation", "PloidyStruct", "Point", 
		"PolyAlleleFrequency", "PolyGenoFrequency", "Polyline", "Polymer", 
		"PolymerComposition", "Polymorphism", "PolynomialDegree", "PolypectomySpecimenCharacteristics", 
		"Population", "PopulationFrequency", "Portion", "Position", 
		"PositionDelta", "PositiveControl", "PostAdverseEventStatus", "PreExistingCondition", 
		"PreferredTerm", "Prep", "PreprocessDatasetParameterSet", "PresentAbsent", 
		"presentation", "PresentationThresholds", "PrimaryDiseasePresentation", "PrimarySiteSurgery", 
		"Primer", "PrimerPairs", "PrincipleComponentAnalysis", "PrincipleComponentAnalysisFinding", 
		"PriorTherapy", "PriorTherapyAgent", "Privilege", "ProbabilityMap", 
		"Probe", "ProbeGroup", "Procedure", "ProcedureDataFile", 
		"ProcedureDataType", "ProcedureEventParameters", "ProcedureSample", "ProcedureSampleType", 
		"ProcedureType", "ProcedureTypeProtocol", "ProcedureUnit", "Process", 
		"ProcessDataFile", "ProcessDetails", "ProcessEquipment", "ProcessLog", 
		"ProcessLogLimsFile", "PROcessParameter", "ProcessSample", "ProcessState", 
		"ProcessStatus", "ProcessType", "ProductInfused", "Profile", 
		"Project", "Project2Sample", "Project2SNP", "ProjectDataFile", 
		"Projection", "ProjectPersonnel", "ProjectProcedure", "ProjectReport", 
		"ProjectReportLimsFile", "ProjectSample", "Promoter", "Propeptide", 
		"properties", "Property", "property", "PropertyChange", 
		"PropertyDescriptor", "propertyId", "propertyLink", "propertyQualifier", 
		"propertyQualifierId", "PropertyValue", "ProstateAccessionCharacteristics", "ProstateAdditionalFindings", 
		"ProstateCancerTNMFinding", "ProstateSpecimenCharacteristics", "ProstateSurgicalPathologySpecimen", "ProtectionElement", 
		"ProtectionGroup", "ProtectionGroupRoleContext", "Protein", "Protein2MMDB", 
		"ProteinAlias", "ProteinBiomarker", "ProteinDige", "ProteinDomain", 
		"ProteinEncodingGeneFeature", "ProteinFeature", "ProteinGenomicIdentifier", "ProteinGroupMembers", 
		"ProteinGroups", "ProteinHit", "ProteinHomolog", "ProteinName", 
		"ProteinProphetFiles", "Proteins", "ProteinSequence", "ProteinSpotSet", 
		"ProteinStructure", "ProteinSubunit", "ProteomicsEventRecords", "Protocol", 
		"Protocol_package", "ProtocolAction", "ProtocolApplication", "ProtocolAssociation", 
		"ProtocolDefinition", "ProtocolFile", "ProtocolFormsSet", "ProtocolFormsTemplate", 
		"ProtocolLimsFile", "ProtocolStatus", "ProtocolStep", "ProtSequences", 
		"Provenance", "Publication", "PublicationSource", "PublicationStatus", 
		"PubMed", "Purity", "PValue", "QaReport", 
		"Quadrupole", "Qualifier", "QualitativeEvaluation", "Quantile", 
		"QuantitationType", "QuantitationTypeDimension", "QuantitationTypeMap", "QuantitationTypeMapping", 
		"Quantity", "QuantityInCount", "QuantityInGram", "QuantityInMicrogram", 
		"QuantityInMilliliter", "QuantityUnit", "QuantSummaries", "QuantumDot", 
		"QuantumDotComposition", "Query", "Question", "QuestionCondition", 
		"QuestionConditionComponents", "QuestionRepetition", "Radiation", "RadiationAdministration", 
		"RadiationAttribution", "RadiationIntervention", "RadiationTherapy", "RadicalProstatectomyGleasonHistologicGrade", 
		"RadicalProstatectomyGleasonHistopathologicGrade", "RadicalProstatectomyInvasiveProstateCarcinoma", "RadicalProstatectomyProstateNegativeSurgicalMargin", "RadicalProstatectomyProstatePositiveSurgicalMargin", 
		"RadicalProstatectomyProstateSpecimenCharacteristics", "RadicalProstatectomyProstateSurgicalMargin", "RadicalProstatectomyProstateSurgicalPathologySpecimen", "Ratio", 
		"RawArrayData", "RawSample", "RBC", "Reaction", 
		"ReceivedEventParameters", "Receptor", "Recipient", "RecipientDisease", 
		"RecipientLaboratoryFinding", "Recurrence", "ReexcisionCutaneousMelanomaSpecimenCharacteristics", "ReexcisionCutaneousMelanomaSurgicalPathologySpecimen", 
		"Reference", "ReferenceChemical", "ReferencedAnnotation", "ReferencedCalculation", 
		"ReferenceDocument", "ReferenceEntity", "ReferenceGene", "ReferenceLink", 
		"ReferenceProtein", "ReferenceRNA", "ReferenceSequence", "ReferenceSpotInformation", 
		"RefSeqmRNA", "RefSeqProtein", "RefseqProtein", "RegionalDistantSurgery", 
		"RegionalLymphNodeSurgery", "RegionalNodeLymphadenectomyCutaneousMelanomaSpecimenCharacteristics", "RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen", "Registration", 
		"Regulation", "Regulator", "RegulatoryElement", "RegulatoryElementType", 
		"RelatedGelItem", "RelatedGelItemProteinHit", "RelationshipObservation", "RelationshipType", 
		"RelativeLocation", "RelativeRecurringBlackout", "Remove", "RenderingDetail", 
		"Reorder", "Repeat", "Report", "ReportDefinition", 
		"ReportDelivery", "ReportDeliveryDefinition", "Reporter", "ReporterBasedAnalysis", 
		"ReporterCompositeMap", "ReporterDimension", "ReporterGroup", "ReporterPosition", 
		"ReportMandatoryFieldDefinition", "ReportPerson", "ReportSection", "ReportStatus", 
		"ReportVersion", "RepositoryInfo", "Representation", "ResearchInstitutionSource", 
		"ResearchStaff", "ResectionColonSpecimenCharacteristics", "Resolution", "ResolvedConceptReference", 
		"ResolvedConceptReferenceList", "ResponseAssessment", "ReviewEventParameters", "RFile", 
		"RNA", "RnaAnnotation", "RnaExperiment", "RnaReport", 
		"RnaResult", "Role", "RoleBasedRecipient", "RouteOfAdministration", 
		"RoutineAdverseEventReport", "RoutineOption", "Run", "RunProtocol", 
		"RunSampleContainer", "SAEReportPreExistingCondition", "SAEReportPriorTherapy", "Sample", 
		"SampleAnalyteProcessingSteps", "SampleCategory", "SampleComposition", "SampleContainer", 
		"SampleLocation", "SampleLog", "SampleManagement", "SampleOrigin", 
		"SamplePlate", "SampleProvider", "SampleSampleOrigin", "SampleSOP", 
		"SampleSOPFile", "SampleType", "Scalar", "ScannerProperties", 
		"Scheduled", "ScheduledActivity", "ScheduledActivityState", "ScheduledCalendar", 
		"ScheduledEmailNotification", "ScheduledEvent", "ScheduledNotification", "ScheduledStudySegment", 
		"ScheduledTimeLineEvent", "ScreeningResult", "SecondaryParticipantIdentifier", "SecondarySpecimenIdentier", 
		"SecondarySpecimenIdentifier", "SecondaryStructure", "Security", "SecurityGroup", 
		"Seg", "SegmentType", "Selenocysteine", "SemanticMetadata", 
		"SemanticType", "SeqFeature", "SeqFeatureLocation", "Sequence", 
		"SequenceAnnotation", "SequenceConflict", "SequencePosition", "SequenceVariant", 
		"SequestScores", "Series", "Sets", "SexDistribution", 
		"Shape", "ShortColumn", "ShortParameter", "ShortSequenceMotif", 
		"Signalp", "SignalPeptide", "Silo", "SimpleName", 
		"SimpleTimePeriod", "Site", "SiteInvestigator", "Size", 
		"SkyCase", "SkyCase_header", "skyCellHeader", "skyChromosome", 
		"SkyDataFile", "SkyDataFileSet", "skyFrag", "SkyMageMappings", 
		"Slide", "SmallMolecule", "SmallMoleculeEntity", "SNP", 
		"SNP2Allele", "SNP2Gene", "SNPAnalysisGroup", "SnpAnalysisResult", 
		"SnpAnnotation", "SNPAnnotation", "SnpArrayExperiment", "SNPArrayReporter", 
		"SNPAssay", "SNPAssociationAnalysis", "SNPAssociationFinding", "SNPCytogeneticLocation", 
		"snpFinding", "SNPFrequencyFinding", "SNPMapping", "SNPPanel", 
		"SNPPhysicalLocation", "SNPProbeAnnotation", "SnpResult", "SocialHistory", 
		"Software", "SoftwareApplication", "Solubility", "SomaticMutationFinding", 
		"SomCluster", "SomClusteringParameter", "SortContext", "SortDescription", 
		"SortDescriptionList", "SortOption", "SortOptionList", "Source", 
		"source", "SourceMeasurement", "SourceMeasurementProtocol", "SourceReference", 
		"Span", "SpatialCoordinate", "SpecializedQuantitationType", "Species", 
		"species", "SpeciesType", "SpecificDateBlackout", "Specimen", 
		"SpecimenAcquisition", "SpecimenArray", "SpecimenArrayContent", "SpecimenArrayType", 
		"SpecimenBasedAnalysis", "SpecimenBasedFinding", "SpecimenBasedMolecularFinding", "SpecimenCharacteristics", 
		"SpecimenCollection", "SpecimenCollectionGroup", "SpecimenEventParameters", "SpecimenProtocol", 
		"SpecimenRequirement", "SpectraData", "SpliceVariant", "SpontaneousMutation", 
		"Spot", "SpotAnalyteProcessingSteps", "SpotDetectionParameters", "SpotDige", 
		"SpotImage", "SpotMap", "SpotMapGroup", "SpotRatio", 
		"SpotSet", "SpotSetSpot", "SpunEventParameters", "StainingMethod", 
		"StandardQuantitationType", "StaticRelationship", "Status", "StorageContainer", 
		"StorageContainerCapacity", "StorageContainerDetails", "StorageDevice", "StorageElement", 
		"StorageType", "Strain", "Strand", "Strength", 
		"StringColumn", "StringParameter", "StructuralAlignment", "STS", 
		"Study", "StudyAgent", "StudyCoordinatingCenter", "StudyFundingSponsor", 
		"StudyInvestigator", "StudyObservation", "StudyOrganization", "StudyParticipant", 
		"StudyParticipantAssignment", "StudyPersonnel", "StudyProtocol", "StudyPublication", 
		"StudySegment", "StudySegmentDelta", "StudySite", "StudyStressor", 
		"StudySubject", "StudySubjectAssignment", "StudyTherapy", "StudyTimePoint", 
		"Subject", "SubjectAssignment", "SubjectGroup", "SubProject", 
		"SubstanceAdministration", "Subsystem", "SubsystemCell", "Summation", 
		"SuppGenotype", "SupportedElement", "SupportedElementList", "Surface", 
		"SurfaceChemistry", "SurfaceGroup", "Surgery", "SurgeryAttribution", 
		"SurgeryIntervention", "SurgeryTreatment", "SurgicalMargin", "SurgicalPathologySpecimen", 
		"Swissprot", "SynopticSurgicalPathologyReport", "SystemAssignedIdentifier", "SystemOrganClass", 
		"TaggingProcess", "TandemSequenceData", "Target", "TargetedModification", 
		"TargetingFunction", "Taxon", "TemperatureUnit", "Term", 
		"term", "term2term", "TermBasedCharacteristic", "TermSource", 
		"text", "TextAnnotation", "TextMeasurement", "ThawEventParameters", 
		"TherapeuticFunction", "TherapeuticProcedure", "Therapy", "ThreeDimensionalSize", 
		"ThreeDimensionalTumorSize", "ThreeDSpatialCoordinate", "Threshold", "Tile", 
		"TimeCourse", "TimeOfFlight", "TimePeriod", "TimePoint", 
		"TimeRecord", "TimeScaleUnit", "TimeSeries", "TimeUnit", 
		"Tissue", "TissueSpecimen", "TissueSpecimenRequirement", "TissueSpecimenReviewEventParameters", 
		"Tmhmm", "TopologicalDomain", "Topology", "TotalGenes", 
		"TotalProteinContent", "Toxicity", "Trace2Genotype", "TransanalDiskExcisionSpecimenCharacteristics", 
		"Transcript", "TranscriptAnnotation", "TranscriptArrayReporter", "TranscriptPhysicalLocation", 
		"TransferEventParameters", "Transformation", "Transgene", "TransitPeptide", 
		"TransmembraneRegion", "TransurethralResectionInvasiveProstateCarcinoma", "TransurethralResectionProstateSpecimenCharacteristics", "TransurethralResectionProstateSurgicalPathologySpecimen", 
		"TreatedAnalyte", "TreatedAnalyteAnalyteProcessingSteps", "Treatment", "TreatmentAssignment", 
		"TreatmentInformation", "TreatmentSchedule", "TreeNode", "TrialDataProvenance", 
		"TriggerAction", "tsBoolean", "tsCaseIgnoreDirectoryString", "tsCaseIgnoreIA5String", 
		"tsCaseSensitiveDirectoryString", "tsCaseSensitiveIA5String", "tsInteger", "tsTimestamp", 
		"tsURN", "TumorCode", "TumorSpecimenBiopsy", "Turn", 
		"TwoDLiquidChromatography1stDimension", "TwoDLiquidChromatography2ndDimension", "TwoDSpatialCoordinate", "TwoDSpotDatabaseSearch", 
		"TwoDSpotMassSpec", "TypeEnumerationMetadata", "UCUM", "UMLAssociationMetadata", 
		"UMLAttributeMetadata", "UMLClassMetadata", "UMLGeneralizationMetadata", "UMLPackageMetadata", 
		"UnclassifiedAgent", "UnclassifiedAgentTarget", "UnclassifiedLinkage", "UniGene", 
		"UnigeneTissueSource", "UniprotAccession", "UniProtKB", "UniprotkbAccession", 
		"Unit", "UnsureResidue", "URL", "URLSourceReference", 
		"User", "UserComments", "UserGroup", "UserOrganization", 
		"UserProtectionElement", "UserRoleContext", "ValidValue", "Value", 
		"ValueDomain", "ValueDomainPermissibleValue", "ValueDomainRelationship", "ValueMeaning", 
		"ValuesNearToCutoff", "VariationFinding", "VariationReporter", "version", 
		"versionable", "versionableAndDescribable", "versionReference", "Vocabulary", 
		"Volume", "VolumeUnit", "WebImageReference", "WebServicesSourceReference", 
		"WeekDayBlackout", "Window", "Xenograft", "XmlReport", 
		"XTandemScores", "YeastModel", "ZincFingerRegion", "Zone", 
		"ZoneDefect", "ZoneGroup", "ZoneLayout"
	};
    
}
