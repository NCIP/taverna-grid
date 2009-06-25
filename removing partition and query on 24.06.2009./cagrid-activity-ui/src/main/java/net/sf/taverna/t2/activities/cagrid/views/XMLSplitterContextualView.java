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
package net.sf.taverna.t2.activities.cagrid.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.cagrid.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

/**
 * Contextual view for an XML splitter that can be added to split inputs/outputs of
 * CaGridActivity that contains complex data types.
 *
 */
@SuppressWarnings("serial")
public class XMLSplitterContextualView extends
		AbstractXMLSplitterContextualView<XMLSplitterConfigurationBean> {

	static Logger logger = Logger.getLogger(XMLSplitterContextualView.class);
	
	public XMLSplitterContextualView(
			Activity<XMLSplitterConfigurationBean> activity) {
		super(activity);
	}

	/**
	 * Gets the component from the {@link HTMLBasedActivityContextualView} and
	 * adds buttons to it allowing XML splitters to be added
	 */
	@Override
	public JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel buttonsPanel = new JPanel(new FlowLayout());

		addInputSplitter(mainFrame, buttonsPanel);
		addOutputSplitter(mainFrame, buttonsPanel);
		mainFrame.add(buttonsPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	@Override
	public String getBackgroundColour() {
		return "#ab92ea";
	}
	
	@Override
	public String getViewTitle() {
		return "XML splitter";
	}

	@Override
	protected String getRawTableRowsHtml() {
		return describePorts();
	}

}
