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
package net.sf.taverna.cagrid.activity.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * A dialog for configuring a new CaGrid installation.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class NewEditCaGridConfigurationDialog extends JDialog {
	
	private static final String PROXY_LIFETIME_NOT_SET = "Not set";
	
	private JTextField jtfCaGridName;
	private JTextField jtfIndexServiceURL;
	private JTextField jtfCDSServiceURL;
	private JTextField jtfAuthNServiceURL;
	private JTextField jtfDorianServiceURL;
	private JComboBox jcbfProxyLifetime;
	
	private String caGridName;
	private String indexServiceURL;
	private String cdsServiceURL;
	private String authNServiceURL;
	private String dorianServiceURL;
	private String proxyLifetime;
	
	// List of all CaGrid configuration names
	private ArrayList<String> caGridNames;

	
	public NewEditCaGridConfigurationDialog(){
		super();
		setTitle("Add new CaGrid configuration");
		setModal(true);

		this.caGridName = "";
		this.indexServiceURL = "";
		this.cdsServiceURL = "";
		this.authNServiceURL = "";
		this.dorianServiceURL = "";
		this.proxyLifetime = "";
		
		caGridNames = new ArrayList<String>(CaGridConfigurationPanel.caGridNames);
		
		initComponents();
	}

	public NewEditCaGridConfigurationDialog(String caGridName, String indexServiceURL,
			String cdsServiceURL, String authNServiceURL, String dorianServiceURL, String proxyLifetime) {
		
		super();
		setTitle("Edit CaGrid configuration");
		setModal(true);
		
		this.caGridName = caGridName;
		this.indexServiceURL = indexServiceURL;
		this.cdsServiceURL = cdsServiceURL;
		this.authNServiceURL = authNServiceURL;
		this.dorianServiceURL = dorianServiceURL;
		this.proxyLifetime = proxyLifetime;
		
		caGridNames = new ArrayList<String>(CaGridConfigurationPanel.caGridNames);
		// Remove the one currently being edited as we do not want to check for it
		// when checking for duplicate CaGrid configuration names
		caGridNames.remove(caGridName);
		
		initComponents();
	}

	private void initComponents() {
		
		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		configurationPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		jtfCaGridName = new JTextField();
		jtfCaGridName.setText(caGridName);
		// If editing Production or Training predefined CaGrid configurations then
		// user is not allowed to change their names
		if (caGridName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME)
				|| caGridName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME)){
			jtfCaGridName.setEditable(false);
		}
		
		jtfIndexServiceURL = new JTextField(35);
		jtfIndexServiceURL.setText(indexServiceURL);
		// If editing Production or Training predefined CaGrid configurations then
		// user is not allowed to change their Index Service URL
		if (caGridName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME)
				|| caGridName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME)){
			jtfIndexServiceURL.setEditable(false);
		}
		
		jtfCDSServiceURL = new JTextField(35);
		jtfCDSServiceURL.setText(cdsServiceURL);
		// Editing Production or Training predefined CaGrid configurations 
		if (caGridName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME)
				|| caGridName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME)){
			
		}
		
		jtfAuthNServiceURL = new JTextField(35);
		jtfAuthNServiceURL.setText(authNServiceURL);
		
		jtfDorianServiceURL = new JTextField(35);
		jtfDorianServiceURL.setText(dorianServiceURL);
		
		jcbfProxyLifetime = new JComboBox(new String[] {PROXY_LIFETIME_NOT_SET, "6h", "12h"});
		if (proxyLifetime.equals("")){
			jcbfProxyLifetime.setSelectedItem(PROXY_LIFETIME_NOT_SET);
		}
		else{
			jcbfProxyLifetime.setSelectedItem(proxyLifetime+"h");
		}
		
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 5;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("CaGrid configuration name"), c);
		c.gridx = 1;
		c.gridy = 0;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfCaGridName, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Index Service"), c);
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfIndexServiceURL, c);
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("CDS Service"), c);
		c.gridx = 1;
		c.gridy = 2;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfCDSServiceURL, c);
		c.gridx = 0;
		c.gridy = 3;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Authentication Service"), c);
		c.gridx = 1;
		c.gridy = 3;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfAuthNServiceURL, c);
		c.gridx = 0;
		c.gridy = 4;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Dorian Service"), c);
		c.gridx = 1;
		c.gridy = 4;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfDorianServiceURL, c);
		c.gridx = 0;
		c.gridy = 5;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Proxy lifetime"), c);
		c.gridx = 1;
		c.gridy = 5;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(jcbfProxyLifetime, c);
		
		// Button to update the values of current CaGrid installation
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cancelPressed();
			}
		});
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		configurationPanel.add(buttonsPanel, c);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(configurationPanel, BorderLayout.CENTER);
		
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });
        
        getRootPane().setDefaultButton(okButton);

        setPreferredSize(new Dimension(700, 280));
        
        pack();
	
	}

	public String getCaGridName() {
		return caGridName;
	}
	
	public String getIndexServiceURL() {
		return indexServiceURL;
	}

	public String getCDSServiceURL() {
		return cdsServiceURL;
	}

	public String getAuthNServiceURL() {
		return authNServiceURL;
	}

	public String getDorianServiceURL() {
		return dorianServiceURL;
	}

	public String getProxyLifetime() {
		return proxyLifetime;
	}

	/**
	 * Check if everything is entered correctly.
	 */
	   private boolean checkControls()
	    {    	
		   caGridName = jtfCaGridName.getText();
		 	if (caGridName.length() == 0){
	            JOptionPane.showMessageDialog(this,
	                "CaGrid configuration name cannot be empty", 
	                "Warning",
	                JOptionPane.WARNING_MESSAGE);            
	            return false;
	    	}
		 	// CaGrid installation name must be unique
		 	if (caGridNames.contains(caGridName)){
	            JOptionPane.showMessageDialog(this,
		                "CaGrid configuration name alredy in use", 
		                "Warning",
		                JOptionPane.WARNING_MESSAGE);            
		            return false;
		 	}
	   
	    	indexServiceURL = jtfIndexServiceURL.getText();
//	    	if (indexServiceURL.length() == 0){
//	            JOptionPane.showMessageDialog(this,
//	                "Index Service cannot be empty", 
//	                "Warning",
//	                JOptionPane.WARNING_MESSAGE);            
//	            return false;
//	    	}
	    	   	
	    	cdsServiceURL = jtfCDSServiceURL.getText();
	    	
	    	authNServiceURL= jtfAuthNServiceURL.getText();
	    	dorianServiceURL = jtfDorianServiceURL.getText();
	    	cdsServiceURL = jtfCDSServiceURL.getText();

	    	// If user configures AuthN Service then Dorian must be configured as well.
	    	// And vice versa.
	    	if ((authNServiceURL.length() == 0 && dorianServiceURL.length() > 0) ||
	    			(authNServiceURL.length() > 0 && dorianServiceURL.length() == 0)){
	            JOptionPane.showMessageDialog(this,
	                "Both Authentication and Dorian Services must be configured", 
	                "Warning",
	                JOptionPane.WARNING_MESSAGE);            
	            return false;
	    	}
    		String proxyLf = (String)jcbfProxyLifetime.getSelectedItem();
	    	if (authNServiceURL.length() == 0 && dorianServiceURL.length() == 0){
	    		if (proxyLf.equals(PROXY_LIFETIME_NOT_SET)){
		    		proxyLifetime = "";
	    		}
	    		else{
	    			JOptionPane.showMessageDialog(this,
	    					"You cannot configure proxy lifetime if Authentication or Dorian Service is empty", 
	    					"Warning",
	    					JOptionPane.WARNING_MESSAGE);   
	    			return false;
	    		}
	    	}
	    	else{
	    		if (proxyLf.equals(PROXY_LIFETIME_NOT_SET)){
		            JOptionPane.showMessageDialog(this,
			                "Proxy lifetime must be configured if Authentication and Dorian Services are configured", 
			                "Warning",
			                JOptionPane.WARNING_MESSAGE);            
			            return false;
	    		}
	    		else{
	    			proxyLifetime = proxyLf.substring(0, proxyLf.indexOf('h'));//remove character 'h' from the string
	    		}
	    	}
	   	
	    	return true;
	    }
	   
    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
        if (checkControls()) {
            closeDialog();
        }
    }

    /**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	caGridName = null;
    	indexServiceURL = null;
    	cdsServiceURL = null;
    	authNServiceURL = null;
    	dorianServiceURL = null;
    	proxyLifetime = null;
        closeDialog();
    }

    /**
     * Close the dialog.
     */
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
	
}
