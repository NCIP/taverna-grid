/*******************************************************************************
 * Copyright (C) 2007 -2008 The University of Manchester   
 * Copyright (C) 2007 The University of Chicago
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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.ExampleValue;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.ProvenanceConnectorRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.InvalidDataflowReport;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchPanel;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.cagrid.CaGridComponent;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;
import net.sf.taverna.t2.workbench.file.FileManager;

import org.apache.log4j.Logger;

public class RunAsCaGridServiceAction extends AbstractAction {

	

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(RunAsCaGridServiceAction.class);
	private CaGridComponent runComponent;
	private PerspectiveSPI resultsPerspective;

	public RunAsCaGridServiceAction() {
		
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
		

	}

	public void actionPerformed(ActionEvent e) {
		runComponent = CaGridComponent.getInstance();
		Object model = ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		if (!(model instanceof Dataflow)) {
			return;
		}
		final Dataflow dataflow = (Dataflow) model;
		Thread t = new Thread("Preparing to run caGridworkflow "
				+ dataflow.getLocalName()) {
			public void run() {
				try {
					runDataflow(dataflow);
				} catch (Exception ex) {
					String message = "Could not run caGrid workflow "
							+ dataflow.getLocalName();
					logger.warn(message);
					InvalidDataflowReport.showErrorDialog(ex.getMessage(), message);			
				}
			};
		};
		t.setDaemon(true);
		t.start();		
	}
	
	protected void runDataflow(Dataflow dataflow) {
		XMLSerializer serialiser = new XMLSerializerImpl();
		XMLDeserializer deserialiser = new XMLDeserializerImpl();
		Dataflow dataflowCopy = null;
		try {
			dataflowCopy = deserialiser.deserializeDataflow(serialiser
					.serializeDataflow(dataflow));
		} catch (SerializationException e1) {
			logger.error("Unable to copy workflow", e1);
		} catch (DeserializationException e1) {
			logger.error("Unable to copy workflow", e1);
		} catch (EditException e1) {
			logger.error("Unable to copy workflow", e1);
		}

		if (dataflowCopy != null) {
			WorkflowLaunchPanel.getDataflowCopyMap()
					.put(dataflowCopy, dataflow);
			
			if (dataflowCopy.getInputPorts().isEmpty()){// No input ports - we can run immediately
				// TODO check if the database has been created and create if needed
				// if provenance turned on then add an IntermediateProvLayer to each
				// Processor
				final ReferenceService referenceService = runComponent
						.getReferenceService();
				//no provenance for now
				ProvenanceConnector provenanceConnector = null;
		
				final InvocationContextImpl context = new InvocationContextImpl(
						referenceService, provenanceConnector);
				
				
				final WorkflowInstanceFacade facade;
				try {
					facade = new EditsImpl().createWorkflowInstanceFacade(
							dataflowCopy, context, "");
				} catch (InvalidDataflowException ex) {
					InvalidDataflowReport.invalidDataflow(ex.getDataflowValidationReport());
					return;
				}		
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						//switchToResultsPerspective();
						runComponent.runWorkflow(facade, (Map) null);
					}
				});			
			}
			else{
				final Dataflow copy = dataflowCopy;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						showInputDialog(copy);
					}
				});	
			}
		} else {
			InvalidDataflowReport.showErrorDialog("Unable to make a copy of the workflow to run",
					"Workflow copy failed");
		}
	}
	

	private void switchToResultsPerspective() {
		if (resultsPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance()
					.getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("results")) {
					resultsPerspective = perspective;
					break;
				}
			}
		}
		if (resultsPerspective != null) {
			ModelMap.getInstance().setModel(
					ModelMapConstants.CURRENT_PERSPECTIVE, resultsPerspective);
		}
	}

	private AnnotationTools annotationTools = new AnnotationTools();

	@SuppressWarnings("serial")
	private void showInputDialog(Dataflow dataflow) {
		// Create and set up the window.

		String title = annotationTools.getAnnotationString(dataflow, DescriptiveTitle.class, "");
		String dialogTitle = "Workflow ";
		if ((title != null) && (!title.equals(""))) {
			dialogTitle = title + ": ";
		}
		dialogTitle += "input values";
		final JDialog dialog = new JDialog((JFrame) null, dialogTitle, true);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		WorkflowLaunchPanel wlp = new WorkflowLaunchPanel(dataflow, runComponent.getReferenceService()) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				//switchToResultsPerspective();
				runComponent = CaGridComponent.getInstance();
				runComponent.runWorkflow(getFacade(), workflowInputs);
				
			}

			@Override
			public void handleCancel() {
				dialog.dispose();
			}
		};
		wlp.setOpaque(true); // content panes must be opaque

		List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(
				dataflow.getInputPorts());
		Collections.sort(inputPorts, new PortComparator());
		for (DataflowInputPort input : inputPorts) {
			// input.getAnnotations();

			String portDescription = annotationTools.getAnnotationString(input,
					FreeTextDescription.class, null);
			String portExample = annotationTools.getAnnotationString(input,
					ExampleValue.class, null);

			wlp.addInput(input.getName(), input.getDepth(), portDescription,
					portExample);
		}

		dialog.setContentPane(wlp);

		// Display the window.
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
