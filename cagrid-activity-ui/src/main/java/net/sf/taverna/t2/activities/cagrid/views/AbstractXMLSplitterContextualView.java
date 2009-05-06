/*********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.activities.cagrid.views;

import java.io.IOException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.cagrid.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.cagrid.OutputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.cagrid.actions.AbstractAddXMLSplitterAction;
import net.sf.taverna.t2.activities.cagrid.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.activities.cagrid.actions.AddXMLOutputSplitterAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.cagrid.wsdl.parser.TypeDescriptor;
import net.sf.taverna.cagrid.wsdl.parser.UnknownOperationException;

import org.apache.log4j.Logger;

/**
 * Represents an abstract contextual view that is subclassed by either CaGridActivityContextualView
 * or XMLSplitterContextualView.
 *
 * @param <BeanType>
 */
@SuppressWarnings("serial")
public abstract class AbstractXMLSplitterContextualView<BeanType> extends
		HTMLBasedActivityContextualView<BeanType> {

	private static Logger logger = Logger
			.getLogger(AbstractXMLSplitterContextualView.class);

	public AbstractXMLSplitterContextualView(Activity<?> activity) {
		super(activity);
	}

	protected void addOutputSplitter(final JComponent mainFrame,
			JPanel flowPanel) {
		if (getActivity() instanceof OutputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors;
			try {
				descriptors = ((OutputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForOutputPorts();
				if (!AbstractAddXMLSplitterAction
						.filterDescriptors(descriptors).isEmpty()) {
					AddXMLOutputSplitterAction outputSplitterAction = new AddXMLOutputSplitterAction(
							(OutputPortTypeDescriptorActivity) getActivity(),
							mainFrame);
					flowPanel.add(new JButton(outputSplitterAction));
				}
			} catch (UnknownOperationException e) {
				logger.warn("Could not find operation for " + getActivity(), e);
			} catch (IOException e) {
				logger
						.warn("Could not read definition for " + getActivity(),
								e);
			}
		}
	}

	protected void addInputSplitter(final JComponent mainFrame, JPanel flowPanel) {
		if (getActivity() instanceof InputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors;
			try {
				descriptors = ((InputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForInputPorts();
				if (!AbstractAddXMLSplitterAction
						.filterDescriptors(descriptors).isEmpty()) {
					AddXMLInputSplitterAction inputSplitterAction = new AddXMLInputSplitterAction(
							(InputPortTypeDescriptorActivity) getActivity(),
							mainFrame);
					flowPanel.add(new JButton(inputSplitterAction));
				}
			} catch (UnknownOperationException e) {
				logger.warn("Could not find operation for " + getActivity(), e);
			} catch (IOException e) {
				logger
						.warn("Could not read definition for " + getActivity(),
								e);
			}
		}
	}

	protected String describePorts() {
		StringBuilder html = new StringBuilder();

		if (!getActivity().getInputPorts().isEmpty()) {
			html.append("<tr><th colspan=\"2\" align=\"left\">Inputs</th></tr>");
			for (ActivityInputPort port : getActivity().getInputPorts()) {
				TypeDescriptor descriptor=null;
				if (getActivity() instanceof InputPortTypeDescriptorActivity) {
					try {
						descriptor = ((InputPortTypeDescriptorActivity) getActivity())
								.getTypeDescriptorForInputPort(port.getName());
						
					} catch (UnknownOperationException e) {
						logger.warn(
								"Could not find operation for " + getActivity(), e);
					} catch (IOException e) {
						logger.warn("Could not read definition for "
								+ getActivity(), e);
					}
				}
				if (descriptor==null) {
					html.append(describePort(port));
				}
				else {
					html.append(describePort(port, descriptor));
				}
				
				
			}
		}
		
		if (!getActivity().getOutputPorts().isEmpty()) {
			html.append("<tr><th colspan=\"2\" align=\"left\">Outputs</th></tr>");
			for (OutputPort port : getActivity().getOutputPorts()) {
				TypeDescriptor descriptor=null;
				if (getActivity() instanceof OutputPortTypeDescriptorActivity)
				{
					try {
						descriptor = ((OutputPortTypeDescriptorActivity) getActivity())
								.getTypeDescriptorForOutputPort(port.getName());
					} catch (UnknownOperationException e) {
						logger.warn(
								"Could not find operation for " + getActivity(), e);
					} catch (IOException e) {
						logger.warn("Could not read definition for "
								+ getActivity(), e);
					}
				}
				if (descriptor==null) {
					html.append(describePort(port));
				}
				else {
					html.append(describePort(port, descriptor));
				}
			}
		}

		return html.toString();
	}

	
	private String describePort(Port port, TypeDescriptor descriptor) {
		String html = "<tr><td>"+port.getName()+"</td><td>";
		if (descriptor!=null && descriptor.isOptional()) {
			html += "<em>optional</em><br>";
		}
		html+="Depth:"+port.getDepth()+"<br>";
		if (descriptor != null )html+="<code>"+descriptor.getQname().toString()+"</code><br>";
		html+="</td></tr>";
		return html;
	}
	
	private String describePort(Port port) {
		String html = "<tr><td>"+port.getName()+"</td><td>";
		html+="Depth:"+port.getDepth()+"<br>";
		html+="</td></tr>";
		return html;
	}

}