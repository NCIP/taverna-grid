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
package net.sf.taverna.t2.workbench.cagrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Map;

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
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
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
      private JTabbedPane outputPanel;
	
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

			resultPanel.setTopComponent(runListPanel);
			//TODO outputPanel should be a tabbed pane?
			//each output should be a (xml) string
			outputPanel = new JTabbedPane();
			
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
           services.addItem("http://test.cagrid.org/service");
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
		DataflowInputPort ip = facade.getDataflow().getInputPorts().get(0);
		
		System.out.println(ip.getName());
         DataflowOutputPort op = facade.getDataflow().getOutputPorts().get(0);
		
		System.out.println(op.getName());
		T2Reference inputRef = (T2Reference) inputs.get("i");
		System.out.println(inputRef.toString());
		
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
