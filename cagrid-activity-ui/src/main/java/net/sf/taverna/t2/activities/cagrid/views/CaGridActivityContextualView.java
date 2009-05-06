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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A contextual view for CaGridActivity - extends AbstractXMLSplitterContextualView
 * to handle cases where complex-typed input/output parameters of the activity 
 * require XML splitting.
 *
 */
public class CaGridActivityContextualView extends
		AbstractXMLSplitterContextualView<CaGridActivityConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;
	
	public CaGridActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	/**
	 * Gets the component from the {@link HTMLBasedActivityContextualView} and
	 * adds buttons to it allowing XML splitters to be added.
	 */
	@Override
	public JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel flowPanel = new JPanel(new FlowLayout());

		addInputSplitter(mainFrame, flowPanel);
		addOutputSplitter(mainFrame, flowPanel);
		
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}
	
	@Override
	public String getBackgroundColour() {
		return "#4b539e";
	}
	
	@Override
	public CaGridActivity getActivity() {
		return (CaGridActivity) super.getActivity();
	}
	
	@Override
	public String getViewTitle() {
		return "caGrid service";
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summary="<tr><td>WSDL URL</td><td>"+getConfigBean().getWsdl() + "</td></tr>";
		summary+="<tr><td>Operation</td><td>"+getConfigBean().getOperation() +"</td></tr>";
		summary+="<tr><td>Index Service URL</td><td>"+getConfigBean().getIndexServiceURL() +"</td></tr>";
		
		if (getConfigBean().isSecure()){
			summary+="<tr><th colspan=\"2\" align=\"left\"><b>Security settings</b></th></tr>";
			summary+="<tr><td>Authentication Service URL</td><td>"+getConfigBean().getAuthNServiceURL()+ "</td></tr>";
			summary+="<tr><td>Dorian Service URL</td><td>"+getConfigBean().getDorianServiceURL()+ "</td></tr>";
			if (getConfigBean().getGSITransport() == null){
				summary+="<tr><td>GSI Transport</td><td>Not set</td></tr>";
			}
			else if (getConfigBean().getGSITransport().equals(org.globus.wsrf.security.Constants.ENCRYPTION)){
				summary+="<tr><td>GSI Transport</td><td>ENCRYPTION</td></tr>";
			}
			else if (getConfigBean().getGSITransport().equals(org.globus.wsrf.security.Constants.SIGNATURE)){
				summary+="<tr><td>GSI Transport</td><td>SIGNATURE</td></tr>";
			}
		}
		
		summary += describePorts();

		return summary;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
	}
	
}
