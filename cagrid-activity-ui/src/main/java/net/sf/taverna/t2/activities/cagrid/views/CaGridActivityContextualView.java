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

import org.apache.log4j.Logger;
import org.globus.axis.gsi.GSIConstants;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.activities.cagrid.CaGridActivitySecurityProperties;
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
	private Logger logger = Logger.getLogger(CaGridActivityContextualView.class);
	
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
		
		// Try to get security properties for this operation from the cache first
		CaGridActivitySecurityProperties secProperties = null;
		if (CaGridActivity.securityPropertiesCache.keySet().contains(getConfigBean().getWsdl()+getConfigBean().getOperation())){
			secProperties = CaGridActivity.securityPropertiesCache.get(getConfigBean().getWsdl()+getConfigBean().getOperation());
		}
		if (secProperties == null){ // there was nothing in the cache - we have to try to get security metadata for the operation
			try {
				// Get security metadata but do not fetch the proxy as we are just 
				// displaying security properties
				CaGridActivity.configureSecurity(getConfigBean(), false);
				// Get the security properties from the cache - they should have been set now from the configureSecurity() method
				secProperties = CaGridActivity.securityPropertiesCache.get(getConfigBean().getWsdl()+getConfigBean().getOperation());
			} catch (Exception ex) {
				// Something went wrong - log the error and there will not anything to show about the security properties
				logger.error(ex.getMessage(), ex);
				ex.printStackTrace();
			}
		}
		
		if (secProperties != null){ // operation is not secure so do not show anything
			summary+="<tr><th colspan=\"2\" align=\"left\"><b>Security settings</b></th></tr>";
			summary+="<tr><td>Authentication Service URL</td><td>"+getConfigBean().getAuthNServiceURL()+ "</td></tr>";
			summary+="<tr><td>Dorian Service URL</td><td>"+getConfigBean().getDorianServiceURL()+ "</td></tr>";
			
			// GSI TRANSPORT
			if (secProperties.getGSITransport() == null){
				summary+="<tr><td>GSI Transport</td><td>Not set</td></tr>";
			}
			else if (secProperties.getGSITransport().equals(org.globus.wsrf.security.Constants.ENCRYPTION)){
				summary+="<tr><td>GSI Transport</td><td>ENCRYPTION</td></tr>";
			}
			else if (secProperties.getGSITransport().equals(org.globus.wsrf.security.Constants.SIGNATURE)){
				summary+="<tr><td>GSI Transport</td><td>SIGNATURE</td></tr>";
			}
			
			// GSI SEC MESG
			if (secProperties.getGSISecureMessage() == null){
				summary+="<tr><td>GSI Secure Message</td><td>Not set</td></tr>";
			}
			else if (secProperties.getGSISecureMessage().equals(org.globus.wsrf.security.Constants.ENCRYPTION)){
				summary+="<tr><td>GSI Secure Message</td><td>ENCRYPTION</td></tr>";
			}
			else if (secProperties.getGSISecureMessage().equals(org.globus.wsrf.security.Constants.SIGNATURE)){
				summary+="<tr><td>GSI Secure Message</td><td>SIGNATURE</td></tr>";
			}
		
			// GSI SEC CONV
			if (secProperties.getGSISecureConversation() == null){
				summary+="<tr><td>GSI Secure Conversation</td><td>Not set</td></tr>";
			}
			else if (secProperties.getGSISecureConversation().equals(org.globus.wsrf.security.Constants.ENCRYPTION)){
				summary+="<tr><td>GSI Secure Conversation</td><td>ENCRYPTION</td></tr>";
			}
			else if (secProperties.getGSISecureConversation().equals(org.globus.wsrf.security.Constants.SIGNATURE)){
				summary+="<tr><td>GSI Secure Conversation</td><td>SIGNATURE</td></tr>";
			}
			
			// GSI ANONYMOUS
			if (secProperties.getGSIAnonymouos() == null){
				summary+="<tr><td>GSI Anonymous</td><td>Not set</td></tr>";
			}
			else if (secProperties.getGSIAnonymouos().equals(Boolean.TRUE)){
				summary+="<tr><td>GSI Anonymous</td><td>Allowed</td></tr>";
			}
			else if (secProperties.getGSIAnonymouos().equals(Boolean.FALSE)){
				summary+="<tr><td>GSI Anonymous</td><td>Not allowed</td></tr>";
			}
			
			// GSI MODE
			if (secProperties.getGSIMode() == null){
				summary+="<tr><td>GSI Delegation</td><td>Not set</td></tr>";
			}
			else if(secProperties.getGSIMode().equals(GSIConstants.GSI_MODE_NO_DELEG)){
				summary+="<tr><td>GSI Delegation</td><td>Not allowed</td></tr>";
			}
			else {
				summary+="<tr><td>GSI Delegation</td><td>"+secProperties.getGSIMode()+"</td></tr>";
			}
			
			// GSI AUTHORIZATION
			if (secProperties.getGSIAuthorisation() == null){
				summary+="<tr><td>GSI Authorisation</td><td>Not set</td></tr>";
			}
			else if (secProperties.getGSIAuthorisation().equals(NoAuthorization.getInstance())){
				summary+="<tr><td>GSI Authorisation</td><td>Not required</td></tr>";
			}
			else {
				summary+="<tr><td>GSI Authorisation</td><td>Required</td></tr>";
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
