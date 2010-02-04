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
package org.cagrid.cds.views;

import java.awt.Frame;
import javax.swing.Action;

import org.cagrid.cds.actions.CDSActivityConfigurationAction;
import org.cagrid.cds.*;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CDSActivityContextualView extends HTMLBasedActivityContextualView<CDSConfigurationBean> {

    private static final long serialVersionUID = -553974544001808511L;

    public CDSActivityContextualView(Activity<?> activity) {
        super(activity);
    }

    @Override
    public String getViewTitle() {
        return "CDS Activity";
    }

    @Override
    protected String getRawTableRowsHtml() {
      
    	String caGridName = getConfigBean().getCaGridName();
    	
        String html1 = "<tr><td>CDS Activity</td></tr>";
        String html2 = "<tr><td>caGridName</td><td>"+caGridName +"</td></tr>";
        return html1+html2;
    }

    @Override
    public Action getConfigureAction(Frame owner) {
        return new CDSActivityConfigurationAction((CDSActivity) getActivity(), owner);
    }
    @Override
	public int getPreferredPosition() {
		return 100;
	}
}


