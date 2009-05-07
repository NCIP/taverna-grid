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
package net.sf.taverna.t2.activities.cagrid.actions;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;

/**
 * Panel with options to configure CaGridActivity.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class CaGridActivityConfigurePanel extends JPanel {

	private CaGridActivityConfigurationBean configurationBean;

	private JButton okButton;
	private JButton cancelButton;
	
	public CaGridActivityConfigurePanel(
			CaGridActivityConfigurationBean configurationBean) {

		this.configurationBean = configurationBean;
		initComponents();
		
	}

	private void initComponents() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		okButton=new JButton("OK");
		cancelButton=new JButton("Cancel");		
	}

	public void setOKAction(Action okAction) {
		okButton.setAction(okAction);
	}

	public void setCancelAction(Action cancelAction) {
		cancelButton.setAction(cancelAction);
	}

}
