/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * Copyright (C) 2009 The University of Chicago   
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
package org.cagrid.transfer.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.cagrid.transfer.*;
import org.cagrid.transfer.servicedescriptions.CaGridTransferActivityIcon;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaGridTransferActivityConfigurationAction extends ActivityConfigurationAction<CaGridTransferActivity, CaGridTransferConfigurationBean> {

    private static final long serialVersionUID = 2518716617809186975L;
    private final Frame owner;

    public CaGridTransferActivityConfigurationAction(CaGridTransferActivity activity, Frame owner) {
        super(activity);
        this.owner = owner;
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("entering CaGridTransferAcitivtyConfigurationAction.actionPerformed(..)");
        CaGridTransferConfigurationBean confBean = new CaGridTransferConfigurationBean();
        Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();
        Object[] functions = {"upload", "download"};
        boolean upload = getActivity().getConfiguration().getIsUpload();
        String function;
        if(upload==true){
        	function="upload";
        }
        else{
        	function = "download";
        }
        String s = (String)JOptionPane.showInputDialog(
                            null,
                            "Choose the function of this caGrid transfer activity:\n",
                            "Set the function of caGrid transfer: upload/download",
                            JOptionPane.PLAIN_MESSAGE,
                            CaGridTransferActivityIcon.getCaGridTransferIcon(),
                            functions,
                            function);

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
        	if(s.equals("upload")){
        		confBean.setIsUpload(true);
        	}
        	else{
        		confBean.setIsUpload(false);		
        	}        	
        	//configureActivity(confBean);
        	ActivityConfigurationDialog.configureActivity(owningDataflow, activity, confBean);
            return;
        }

        
        
    }
}
