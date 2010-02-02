/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester   
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
package net.sf.taverna.cagrid.ui.views;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.cagrid.activity.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.cagrid.activity.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.cagrid.activity.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class XMLSplitterViewFactory implements ContextualViewFactory<Activity<XMLSplitterConfigurationBean>>{

	public boolean canHandle(Object object) {
		return object instanceof XMLInputSplitterActivity || object instanceof XMLOutputSplitterActivity;
	}

	public ContextualView getView(
			Activity<XMLSplitterConfigurationBean> activity) {
		return new XMLSplitterContextualView(activity);		
	}
	public List<ContextualView> getViews(
			Activity<XMLSplitterConfigurationBean> activity) {
		return Arrays.asList(new ContextualView[] {new XMLSplitterContextualView(activity)});
	}


}
