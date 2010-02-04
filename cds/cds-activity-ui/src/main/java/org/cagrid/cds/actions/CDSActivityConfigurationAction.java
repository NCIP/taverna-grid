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
package org.cagrid.cds.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.cagrid.cds.*;
import org.cagrid.cds.servicedescriptions.CDSActivityIcon;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class CDSActivityConfigurationAction extends ActivityConfigurationAction<CDSActivity, CDSConfigurationBean> {

    private static final long serialVersionUID = 2518716617809186975L;
    private final Frame owner;

    public CDSActivityConfigurationAction(CDSActivity activity, Frame owner) {
        super(activity);
        this.owner = owner;
    }

    public void actionPerformed(ActionEvent e) {
        //System.out.println("entering CDSAcitivtyConfigurationAction.actionPerformed(..)");
        //CDSConfigurationBean confBean = new CDSConfigurationBean();
    	CDSConfigurationBean confBean = getActivity().getConfiguration();
        CDSActivityConfigureDialog configurationDialog = new CDSActivityConfigureDialog(confBean);
        configurationDialog.setLocationRelativeTo(owner);
		configurationDialog.pack();
		configurationDialog.setModal(true);
		configurationDialog.setVisible(true);
        confBean.setCaGridName(configurationDialog.getCaGridName());
		confBean.setParty(configurationDialog.getParty());
		confBean.setDelegationLifetime(configurationDialog.getDelegationLifetime());
		confBean.setDelegationPathLength(configurationDialog.getDelegationPathLength());
		confBean.setIssuedCredentialLifetime(configurationDialog.getIssuedCredentialLifetime());
		confBean.setIssuedCredentialPathLength(configurationDialog.getIssuedCredentialPathLength());
		//save the configuration result
		Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();
		ActivityConfigurationDialog.configureActivity(owningDataflow, activity, confBean);

       
    }
}
    

