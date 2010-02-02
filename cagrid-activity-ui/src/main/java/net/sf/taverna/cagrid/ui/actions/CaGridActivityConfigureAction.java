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
package net.sf.taverna.cagrid.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.cagrid.activity.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * Action to configure CaGridActivity. This action only applies to secure caGrid services
 * and enables user to set AuthN or Dorian service URLs that are specific to this 
 * caGrid service/operation only. They will be serialised and saved in the wf definition file.
 * 
 * @author Alex Nenadic
 * 
 * Modified by Wei Tan on 1/26/2010, adding CDS URL in CaGridActivity configuration
 *
 */
@SuppressWarnings("serial")
public class CaGridActivityConfigureAction extends ActivityConfigurationAction<CaGridActivity,CaGridActivityConfigurationBean> {

	private static Logger logger = Logger.getLogger(CaGridActivityConfigureAction.class);

	private CaGridActivityConfigurationBean configurationBean;
	private JComponent owner;
	
	public CaGridActivityConfigureAction(CaGridActivity activity, JComponent owner) {
		super(activity);
		putValue(NAME, "Configure");
		this.configurationBean = activity.getConfiguration();
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {

		CaGridActivityConfigureDialog configurationDialog = new CaGridActivityConfigureDialog(configurationBean);
		Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();
		configurationDialog.setLocationRelativeTo(owner);
		configurationDialog.pack();
		configurationDialog.setModal(true);
		configurationDialog.setVisible(true);
		
		String authNServiceURL = configurationDialog.getAuthNServiceURL();
		String dorianServiceURL = configurationDialog.getDorianServiceURL();
		String cdsServiceURL = configurationDialog.getCDSServiceURL();
		
		if (authNServiceURL == null && dorianServiceURL == null){ // user cancelled
			// Do nothing
			return;
		}
		
		boolean beanChanged = false;
		// AuthN service
		if (configurationBean.getAuthNServiceURL()==null){
			if (!authNServiceURL.equals("")){
				// AuthN service has been changed - it has not been set before in the
				// bean and has now been set in the config. dialog
				configurationBean.setAuthNServiceURL(authNServiceURL);
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: Authentication Service property changed");
			}
		}
		else{
			// AuthN service has been set before in the bean - has it changed?
			if (!configurationBean.getAuthNServiceURL().equals(authNServiceURL)){
				// Has been changed
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: Authentication Service property changed");
			}
		}
		// Dorian service
		if (configurationBean.getDorianServiceURL()==null){
			if (!dorianServiceURL.equals("")){
				// Dorian service has been changed - it has not been set before in the
				// bean and has now been set in the config. dialog
				configurationBean.setDorianServiceURL(dorianServiceURL);
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: Dorian Service property changed");
			}
		}
		else{
			// Dorian service has been set before in the bean - has it changed?
			if (!configurationBean.getDorianServiceURL().equals(dorianServiceURL)){
				// Has been changed
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: Authentication Service property changed");
			}
		}
		
		// CDS service
		if (configurationBean.getCDSURL()==null){
			if (!cdsServiceURL.equals("")){
				// CDS service has been changed - it has not been set before in the
				// bean and has now been set in the config. dialog
				configurationBean.setCDSURL(cdsServiceURL);
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: CDS Service property changed");
			}
		}
		else{
			// CDS service has been set before in the bean - has it changed?
			if (!configurationBean.getCDSURL().equals(cdsServiceURL)){
				// Has been changed
				beanChanged = true;
				logger.info("CaGridActivityConfiguration: CDS Service property changed");
			}
		}
		
    	// Has anything actually changed? 
		if (beanChanged){
			if (authNServiceURL.equals("")) // when empty we actually want to delete it
				authNServiceURL = null;
			if (dorianServiceURL.equals("")) // when empty we actually want to delete it
				dorianServiceURL = null;
			if (cdsServiceURL.equals("")) // when empty we actually want to delete it
				cdsServiceURL = null;
			configurationBean.setAuthNServiceURL(authNServiceURL);
			configurationBean.setDorianServiceURL(dorianServiceURL);
			configurationBean.setCDSURL(cdsServiceURL);
			//configureActivity(configurationBean);
			ActivityConfigurationDialog.configureActivity(owningDataflow, activity, configurationBean);
		}
	}

}
