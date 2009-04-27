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

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CaGridActivityContextualView extends HTMLBasedActivityContextualView<CaGridActivityConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;

	public CaGridActivityContextualView(Activity<?> activity) {
		super(activity);
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
		String summary="<tr><td>WSDL</td><td>"+getConfigBean().getWsdl();
		summary+="</td></tr><tr><td>Operation</td><td>"+getConfigBean().getOperation()+"</td></tr>";
		//TODO uncomment these two lines when we switch back to a newer version of wsdl-activity
		//boolean securityConfigured=getConfigBean().getSecurityProfileString()!=null;
		//summary+="<tr><td>Secured?</td><td>"+Boolean.toString(securityConfigured)+"</td></tr>";
		summary+="</tr>";
		return summary;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
	}
	
}
