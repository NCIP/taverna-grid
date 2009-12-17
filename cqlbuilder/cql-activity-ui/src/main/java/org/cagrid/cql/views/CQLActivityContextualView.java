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
package org.cagrid.cql.views;

import java.awt.Frame;

import javax.swing.Action;

import org.cagrid.cql.CQLActivity;
import org.cagrid.cql.CQLConfigurationBean;
import org.cagrid.cql.actions.CQLActivityConfigurationAction;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CQLActivityContextualView extends HTMLBasedActivityContextualView<CQLConfigurationBean> {

    private static final long serialVersionUID = -553974544001808517L;

    public CQLActivityContextualView(Activity<?> activity) {
        super(activity);
    }

    @Override
    public String getViewTitle() {
        return "CQL Builder";
    }

    @Override
    protected String getRawTableRowsHtml() {
        //SavedQueryWindowState sq = getConfigBean().getSavedQuery();
        String targetServiceUrl = getConfigBean().getServicesUrl();
        String html = "<tr><td>Target service's url:</td><td>"+targetServiceUrl+"</td></tr>";       
        String valueStr = getConfigBean().getValue();
        valueStr = valueStr.replace("<", "&lt;");
        valueStr = valueStr.replace(">", "&gt;");
        valueStr = valueStr.replaceAll("\n", "<br/>");
        String html1 = "<tr><td>Value</td><td><pre>" + valueStr + "</pre></td></tr>";

        boolean isManual = getConfigBean().isIsQueryManual();
        String html2 = "<tr><td>isManual</td><td>" + (isManual ? "The query is manual query" : "The query is CQLBuilder query") + "</td></tr>";

        return html + html1 + html2;
    }

    @Override
    public Action getConfigureAction(Frame owner) {
        return new CQLActivityConfigurationAction((CQLActivity) getActivity(), owner);
    }
    @Override
	public int getPreferredPosition() {
		return 101;
	}
}


