/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester   
 * Copyright (C) 2008 The University of Chicago
 * @author Wei Tan
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
package net.sf.taverna.t2.workbench.cagrid;

import gov.nih.nci.cagrid.cadsr.client.CaDSRServiceClient;
import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.cagrid.workflow.factory.client.TavernaWorkflowServiceClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.io.File;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.io.FileUtils;


import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import org.springframework.context.ApplicationContext;

import workflowmanagementfactoryservice.WorkflowOutputType;
import workflowmanagementfactoryservice.WorkflowStatusType;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;
import net.sf.taverna.t2.workbench.run.DataflowRun;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

public class CaGridComponent extends JPanel implements UIComponentSPI, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private static CaGridComponent singletonInstance;
	
      private static Logger logger = Logger.getLogger(CaGridComponent.class);
      
	private ReferenceService referenceService;
	
	private String referenceContext;
      
      public JComboBox services;
           
      private int row = -1;
      
      private JButton runButton;
      
      private JButton refreshButton;
        
      private JButton inputButton;
     
      private JButton testButton;
     
      private JList runList;
      
      private DefaultListModel runListModel;
      
      //result panel is divided into two parts
      //left part is a list of run
      //right part is a tabbed pane showing results of each run
      
      private JSplitPane resultPanel;
      private JPanel runListPanel;
      private JPanel outputPanel;
	
	private CaGridComponent() {
		  super(new GridBagLayout());
          addHeader();
          addcagridService();
          addRunButton();
          addInputButton();
          addRefreshButton();
          checkButtons();
          addResultPanel();
          
        //force reference service to be constructed now rather than at first workflow run
  		getReferenceService();
  		
		
	}
	   private void addResultPanel() {
		   GridBagConstraints c = new GridBagConstraints();
           c.gridx = 0;
           c.gridy = ++row;
           c.fill = GridBagConstraints.BOTH;
           c.weightx = 0.01;
           c.weighty = 0.01;
           c.anchor = GridBagConstraints.SOUTHEAST;
           c.gridwidth = GridBagConstraints.REMAINDER;
		    resultPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			resultPanel.setDividerLocation(200);
			resultPanel.setBorder(null);
			runListModel = new DefaultListModel();
			runList = new JList(runListModel);
			runList.setBorder(new EmptyBorder(5,5,5,5));
			runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
			
			runListPanel = new JPanel(new BorderLayout());
			runListPanel.setBorder(LineBorder.createGrayLineBorder());
			
			JLabel worklflowRunsLabel = new JLabel("Workflow Runs");
			worklflowRunsLabel.setBorder(new EmptyBorder(5,5,5,5));
			runListPanel.add(worklflowRunsLabel, BorderLayout.NORTH);
			
			JScrollPane scrollPane = new JScrollPane(runList);
			scrollPane.setBorder(null);
			runListPanel.add(scrollPane, BorderLayout.CENTER);
			runList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						Object selection = runList.getSelectedValue();
						if (selection instanceof CaGridRun) {
							CaGridRun dataflowRun = (CaGridRun) selection;
							//TODO refresh outputPanel
							if(dataflowRun.status.equals("completed")){
								String resultDisplayString = "";
								for(Iterator it = dataflowRun.outputMap.entrySet().iterator(); it.hasNext();) {
								    Map.Entry entry = (Map.Entry) it.next();
								    Object key = entry.getKey();
								    Object value = entry.getValue();
								    resultDisplayString  = resultDisplayString + (String) key + ":--" +(String) value + "\n";
								}
								JTextArea resultText = new JTextArea(resultDisplayString);
							outputPanel.removeAll();
							outputPanel.revalidate();
							outputPanel.add(resultText, BorderLayout.CENTER);
							outputPanel.revalidate();
							revalidate();	
							}
							
						}
					}
				}
			});

			resultPanel.setTopComponent(runListPanel);
			
			//TODO outputPanel should be a tabbed pane?
			//each output should be a (xml) string
			outputPanel = new JPanel();
			
			//outputPanel = new JPanel(new BorderLayout());
			outputPanel.setBorder(LineBorder.createGrayLineBorder());
			outputPanel.setBackground(Color.WHITE);
			outputPanel.add(new JLabel("Workflow Execution Outputs shows here.", JLabel.CENTER), BorderLayout.CENTER);
			resultPanel.setBottomComponent(outputPanel);
			add(resultPanel,c);
			
		
	}
	protected void addHeader() {
           GridBagConstraints c = new GridBagConstraints();
           c.gridx = 0;
           c.gridy = ++row;
           c.fill = GridBagConstraints.HORIZONTAL;
           c.weightx = 0.1;
           c.anchor = GridBagConstraints.NORTHWEST;
           c.gridwidth = GridBagConstraints.REMAINDER;
           add(new JLabel("Execute workflow as a caGrid Service"), c);
   }
   
   protected void addcagridService() {
           GridBagConstraints c = new GridBagConstraints();
           c.gridx = 0;
           c.gridy = ++row;
           c.anchor = GridBagConstraints.LINE_END;
           c.ipadx = 5;
           c.ipady = 5;
           add(new JLabel("caGird Service URI :"), c);
           
           c.weightx = 0.1;
           c.anchor = GridBagConstraints.LINE_START;
           c.fill = GridBagConstraints.HORIZONTAL;
           c.gridx = GridBagConstraints.RELATIVE;
           
           services = new JComboBox();
           services.addItem("http://128.135.125.17:51000/wsrf/services/cagrid/TavernaWorkflowService");
           services.addItem("http://test.cagrid.org//wsrf/services/cagrid/TavernaWorkflowService");
           services.setSelectedIndex(0);
           //set the list of available caGrid workflow services here
           //services.addActionListener(new ServiceSelectionListener());
           services.setEditable(true);
           add(services, c);
           c.weightx = 0;
   
           //Action connectService = new ConnectServiceAction();
           testButton = new JButton("Test Service", WorkbenchIcons.updateIcon);
           testButton.setActionCommand("test");
           testButton.addActionListener(this);
           add(testButton,c);
           
           //Action addService = new NewServiceAction();
           //add(new JButton("addService"), c);
           
           //Action editService = new EditServiceAction();
           //editButton = new JButton("editService");
           //add(editButton, c);
           
           //Action removeService = new RemoveServiceAction();
           //removeButton = new JButton("removeService");
           //add(removeButton, c);
   }
   
   protected void addRunButton() {
           GridBagConstraints c = new GridBagConstraints();
           c.gridx = 0;
           c.gridy = ++row;
           //runButton = new JButton("Run Workflow", WorkbenchIcons.runIcon);
           runButton = new JButton(new RunAsCaGridServiceAction());
           runButton.setEnabled(true);
           add(runButton, c);
   }
   
   private void checkButtons() {
          
           //removeButton.setEnabled(context != null);
           //editButton.setEnabled(context != null);
           
           refreshButton.invalidate();
          // removeButton.invalidate();
          // editButton.invalidate();
   }
   
   private void addInputButton() {
       GridBagConstraints c = new GridBagConstraints();
       c.gridx = 1;
       c.gridy = row;
       c.anchor = GridBagConstraints.WEST;
       inputButton = new JButton("Add Input", WorkbenchIcons.inputIcon);
       inputButton.setEnabled(false);
       add(inputButton, c);
       //TODO add a tabbed dialog to configure the input?
       //each input is either a file or a string
}
   
   private void addRefreshButton() {
           GridBagConstraints c = new GridBagConstraints();
           c.gridx = 2;
           c.gridy = row;
           c.anchor = GridBagConstraints.WEST;
           refreshButton = new JButton("Refresh Results", WorkbenchIcons.refreshIcon);
           refreshButton.setEnabled(false);
           add(refreshButton, c);
          
   }
   
   
 
   public void runWorkflow(WorkflowInstanceFacade facade, Map<String, T2Reference> inputs) {
	   //TODO invoke caGrid workflow execution service
		CaGridRun runComponent = new CaGridRun();
		runListModel.add(0, runComponent);
		runList.setSelectedIndex(0);	
		//TODO print out the information of the workflow
		System.out.println("Workflow is running.");
		//TODO traverse inputports, get all strings and combine to a String[]
		Map<String, String> inputMap = new HashMap <String, String>();
		for (int i=0;i<facade.getDataflow().getInputPorts().size();i++){
			//TODO: sequence will mess up!
			DataflowInputPort ip = facade.getDataflow().getInputPorts().get(i);
			T2Reference inputRef = (T2Reference) inputs.get(ip.getName());
			//System.out.println(inputRef.toString());
			
			String inputString = (String) facade.getContext().getReferenceService().renderIdentifier(inputRef,
					String.class, null);
			inputMap.put(ip.getName(),inputString);
			
		}
		//traverse the inputMap
		String inputDisplayString = "The input of this workflow is as follows:\n";
		for(Iterator it = inputMap.entrySet().iterator(); it.hasNext();) {
		    Map.Entry entry = (Map.Entry) it.next();
		    Object key = entry.getKey();
		    Object value = entry.getValue();
		    inputDisplayString  = inputDisplayString + (String) key + "---: " +(String) value + "\n";
		}
		System.out.println("Input of the workflow is: " + inputDisplayString);
        
         Dataflow dataflow = facade.getDataflow();
		XMLSerializer serialiser = new XMLSerializerImpl();
		String[] outputs = null;
		try {
			Element workflowDef = serialiser.serializeDataflow(dataflow);
			XMLOutputter outputter = new XMLOutputter();
			
			//outputter.output(workflowDef, System.out);
			
			String workflowDefString = outputter.outputString(workflowDef);
			//TODO write this string into a file to be consumed by Dina's service
			 File file = new File(dataflow.getLocalName());
		      FileUtils.writeStringToFile(file, workflowDefString);	
		      System.out.println("File name: " + file.getAbsolutePath());
		    System.out.println("----------------Workflow Definition----------------------");
			System.out.println(workflowDefString);
			System.out.println("----------------End of Workflow Definition---------------");
			//String url = "http://128.135.125.17:51000/wsrf/services/cagrid/TavernaWorkflowService";
			String url =  (String) services.getSelectedItem();
			TavernaWorkflowServiceClient client = new TavernaWorkflowServiceClient(url);
			String workflowName = "Test1";
			System.out.println("\n1. Running createWorkflow ..");

			//WMSOutputType wMSOutputElement =  client.createWorkflow(input);
			EndpointReferenceType resourceEPR = TavernaWorkflowServiceClient.setupWorkflow(url, file.getAbsolutePath(), workflowName);
			// 2. Start Workflow Operations Invoked.
			//

			String[] inputArgs = new String[inputMap.size()];
			int i = 0;
			for(Iterator it = inputMap.entrySet().iterator(); it.hasNext();) {
			    Map.Entry entry = (Map.Entry) it.next();
			    Object key = entry.getKey();
			    Object value = entry.getValue();
			    inputArgs[i++] =  (String) value;
			}
			
			//String[] inputArgs = {"caCore"}; 
			
			System.out.println("\n2. Now starting the workflow ..");
			

			//This method runs the workflow with the resource represented by the EPR.
			// If there is no inputFile for the workflow, give "null"
			WorkflowStatusType workflowStatusElement =  TavernaWorkflowServiceClient.startWorkflow(inputArgs, resourceEPR);
			
			//TODO should be a while loop?
			if (workflowStatusElement.equals(WorkflowStatusType.Done))
			{
				System.out.println("Workflow successfully executed..");
			}
			else
			{
				throw new Exception("Failed to execute the workflow! Please try again.");
			}
			// 3. Get Status operation invoked.
			System.out.println("\n3. Checking the status of the workflow..");
			WorkflowStatusType workflowStatus = TavernaWorkflowServiceClient.getStatus(resourceEPR);
			if(workflowStatus.equals(WorkflowStatusType.Done))
			{
				System.out.println("Workflow Executions is Completed.");
			}
			else if (workflowStatus.equals(WorkflowStatusType.Failed))
			{
				System.out.println("Workflow failed to execute.");
			}
			else if (workflowStatus.equals(WorkflowStatusType.Pending))
			{
				System.out.println("Workflow execution is pending.");
			}

			//4. Get output of workflow.

			System.out.println("\n4. Getting back the output string..");
			WorkflowOutputType workflowOutput = TavernaWorkflowServiceClient.getOutput(resourceEPR);
			
			 outputs = workflowOutput.getOutputFile();			
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 DataflowOutputPort op = facade.getDataflow().getOutputPorts().get(0);
		System.out.println(op.getName());
		
		//TODO get a list of xml strings,add to output panel
		Map<String, String> outputMap = new HashMap <String, String>();
		for (int i=0;i<facade.getDataflow().getOutputPorts().size();i++){
			//TODO: sequence will mess up!
			op = facade.getDataflow().getOutputPorts().get(i);
			outputMap.put(op.getName(),outputs[i]);
			
		}
		runComponent.outputMap = outputMap;
		//show the outputMap in resultPanel
		//traverse the outputMap
		String resultDisplayString = "";
		for(Iterator it = outputMap.entrySet().iterator(); it.hasNext();) {
		    Map.Entry entry = (Map.Entry) it.next();
		    Object key = entry.getKey();
		    Object value = entry.getValue();
		    resultDisplayString  = resultDisplayString + (String) key + ":\n" +(String) value;
		}
		JTextArea resultText = new JTextArea(resultDisplayString);
		runComponent.status = "completed";
		outputPanel.removeAll();
		outputPanel.revalidate();
		outputPanel.add(resultText, BorderLayout.CENTER);
		outputPanel.revalidate();
		revalidate();
		
		//CaGridComponent.getInstance()
		
	}
	public static CaGridComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new CaGridComponent();
		}
		return singletonInstance;
	}
	
	 public void actionPerformed(ActionEvent e) {
	        if("test".equals(e.getActionCommand())){
	        	//TODO test whether the caGrid execution service is online
	        	System.out.println("testing caGrid services......successful!");
	        	      	
	        }
	    }
	 //return the ReferenceService -- used to manipulate data
		public ReferenceService getReferenceService() {
			String context = ReferenceConfiguration.getInstance().getProperty(
					ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
			if (!context.equals(referenceContext)) {
				referenceContext = context;
				ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
						context);
				referenceService = (ReferenceService) appContext
						.getBean("t2reference.service.referenceService");
			}
			return referenceService;

		}

	
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
