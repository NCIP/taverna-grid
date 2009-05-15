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
package net.sf.taverna.t2.activities.cagrid.servicedescriptions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Dialog to specify type of caGrid and WSDL URL of the caGrid service to be added to service panel.
 * 
 * @author Alex Nenadic
 *
 */

@SuppressWarnings("serial")
public abstract class CaGridServiceFromWSDLDialog extends JDialog{

	// CaGrid type the user wishes to add service from (e.g. Training, Production)
	private String[] caGridType = {"Training caGrid", 
			"Production caGrid"};
	
	// Index Services
	private String[] indexServicesURLs = { "http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", 
			"http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService"};

	// Map of Authentication Services corresponding to each of the Index Services 
	// (should be a list of Authentication Services (and not just one) for each Index Service really)
	private Map<String,String> authenticationServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};
	
	// Map of Dorian Services corresponding to each of the Index Services
	private Map<String,String> dorianServicesMap = new HashMap<String, String>(){
	    {
	        put("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService", "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian");
	        put("http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService", "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian");
	    }
	};	

	public JComboBox caGridTypeComboBox;

	private JTextField caGridServiceWSDLTextField;

	public CaGridServiceFromWSDLDialog(){
		super((Frame) null, "Add caGrid service from a WSDL location", true, null); // create a modal dialog
		initComponents();
	}

	private void initComponents() {
        
        JPanel servicePanel = new JPanel(new GridBagLayout());
        servicePanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5,5,5,5);
		c.weightx = 0.5;
		c.weighty = 0.5;
        // caGrid Type combobox
        servicePanel.add(new JLabel("Select grid"), c);
        
		c.gridx = 1;
		c.gridy = 0;
        caGridTypeComboBox = new JComboBox(caGridType);
        servicePanel.add(caGridTypeComboBox, c);
        
		c.gridx = 0;
		c.gridy = 1;
        servicePanel.add(new JLabel("Service WSDL's URL"), c);
        
		c.gridx = 1;
		c.gridy = 1;
        caGridServiceWSDLTextField = new JTextField(50);
        caGridServiceWSDLTextField.setMinimumSize(new Dimension(50, caGridServiceWSDLTextField.getPreferredSize().height));
        servicePanel.add(caGridServiceWSDLTextField, c);
        
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(servicePanel);
        
	}
	
	public String getWsdlURL(){
		return caGridServiceWSDLTextField.getText();
	}
	
	
	public String getIndexServiceURL(){
		return indexServicesURLs[caGridTypeComboBox.getSelectedIndex()];
	}

	public String AuthenticationServiceURL(){
		return authenticationServicesMap.get(indexServicesURLs[caGridTypeComboBox.getSelectedIndex()]);
	}
	
	public String DorianServiceURL(){
		return dorianServicesMap.get(indexServicesURLs[caGridTypeComboBox.getSelectedIndex()]);
	}
	
	protected abstract void addRegistry(String wsdlURL, String indexServiceURL,
			String authNServiceURL, String dorianServiceURL) ;
}
