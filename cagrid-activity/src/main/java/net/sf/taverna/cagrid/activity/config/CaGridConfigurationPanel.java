/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester 
 * Copyright (C) 2010 The University of Chicago   
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

/**
 * CaGrid preferences panel where users can define new caGrid configurations 
 * and edit details of existing. 
 * 
 * Default caGrid configurations include Training and Production CaGrid. For 
 * these two, it is not possible to change the Index Service, but AuthN, Dorian
 * and CDS services can be modified.
 * 
 * @author Alex Nenadic
 * @author Wei Tan
 *
 */
@SuppressWarnings("serial")
public class CaGridConfigurationPanel extends JPanel{

	private CaGridConfiguration configuration;

	// List of all defined caGrid configurations - includes two default ones (Production and Training CaGrid)
	public static ArrayList<String> caGridNames;
	// CaGrid configurations' details
	private ArrayList<String> indexServicesURLs;
	private ArrayList<String> authNServicesURLs;
	private ArrayList<String> dorianServicesURLs;
	private ArrayList<String> proxyLifetimes;
	private ArrayList<String> credentialDelegationServicesURLs;
		
	private JComboBox caGridNamesComboBox;
	private DefaultComboBoxModel caGridNamesComboBoxModel;

	private JTextField jtfIndexServiceURL;
	private JTextField jtfAuthNServiceURL;
	private JTextField jtfDorianServiceURL;
	private JTextField jtfCredentialDelegationServiceURL;
	private JTextField jtfProxyLifetime;

	private JButton addButton;
	private JButton removeButton;
	private JButton restoreDefaultsButton;

	public CaGridConfigurationPanel() {
		super();
		
		configuration = CaGridConfiguration.getInstance();
		// Get default list of CaGridS - keys in the map contain CaGrid names and values contain 
		// various properties set for the CaGrid (Index Service URL, AuthN service URL, Dorian Service URL, 
		// proxy lifetime and CDS URL).
		HashSet<String> caGridNamesSet = new HashSet<String>(configuration.getDefaultPropertyMap().keySet());
		// Get all other caGridS that may have been configured though preferences (may include 
		// the default ones as well if some of their values have been changed), 
		// but set will ignore duplicates so we are OK
		caGridNamesSet.addAll(configuration.getKeys());
		caGridNames = new ArrayList<String>(caGridNamesSet);//caGridNamesSet.toArray(new String[caGridNamesSet.size()]);
		
		// Get Index Service URLs for all CaGrids
		indexServicesURLs = new ArrayList<String>();
		// Get AuthN Service URLs for all CaGrids
		authNServicesURLs = new ArrayList<String>();
		// Get Dorian Service URLs for all CaGrids
		dorianServicesURLs = new ArrayList<String>();
		// Get default proxy lifetime for all CaGrids
		proxyLifetimes = new ArrayList<String>();
		// Get CDS Service URLs for all CaGrids, if defined
		credentialDelegationServicesURLs = new ArrayList<String>();
		for (int i = 0; i < caGridNames.size(); i++){
			List<String> propertyStringList = configuration.getPropertyStringList(caGridNames.get(i));
			
			indexServicesURLs.add(propertyStringList.get(0));
			authNServicesURLs.add(propertyStringList.get(1));
			dorianServicesURLs.add(propertyStringList.get(2));
			proxyLifetimes.add(propertyStringList.get(3));
			credentialDelegationServicesURLs.add(propertyStringList.get(4));
		}

		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());	
		
		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new CompoundBorder(new EmptyBorder(5,0,0,0), new EtchedBorder(EtchedBorder.LOWERED)));
		configurationPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 5;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("CaGrid configuration"), c);
		c.gridx = 1;
		c.gridy = 0;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.NONE;
		caGridNamesComboBox = new JComboBox();
		caGridNamesComboBoxModel = new DefaultComboBoxModel(new Vector<String>(caGridNames));
		caGridNamesComboBox.setModel(caGridNamesComboBoxModel);
		configurationPanel.add(caGridNamesComboBox, c);

		jtfIndexServiceURL = new JTextField();
		jtfIndexServiceURL.setEditable(false);
		jtfCredentialDelegationServiceURL = new JTextField();
		jtfCredentialDelegationServiceURL.setEditable(false);
		jtfAuthNServiceURL = new JTextField();
		jtfAuthNServiceURL.setEditable(false);
		jtfDorianServiceURL = new JTextField();
		jtfDorianServiceURL.setEditable(false);
		jtfProxyLifetime = new JTextField();
		jtfProxyLifetime.setEditable(false);
		jtfProxyLifetime.setMinimumSize(new Dimension(50,jtfProxyLifetime.getPreferredSize().height));
		jtfProxyLifetime.setMaximumSize(new Dimension(50,jtfProxyLifetime.getPreferredSize().height));
		jtfProxyLifetime.setPreferredSize(new Dimension(50,jtfProxyLifetime.getPreferredSize().height));
		
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
		configurationPanel.add(jtfCredentialDelegationServiceURL, c);
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
		configurationPanel.add(jtfProxyLifetime, c);
		
		// Button to update the values of current CaGrid installation
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JPanel updateButtonPanel= new JPanel (new FlowLayout(FlowLayout.CENTER));
		JButton updateButton = new JButton("Edit");
		updateButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {	
				int index =  caGridNamesComboBox.getSelectedIndex();
				NewEditCaGridConfigurationDialog editCaGridConfigurationDialog = new NewEditCaGridConfigurationDialog(
						(String) caGridNamesComboBox.getSelectedItem(),
						indexServicesURLs.get(index), credentialDelegationServicesURLs.get(index), 
						authNServicesURLs.get(index),dorianServicesURLs.get(index), 
						proxyLifetimes.get(index));
				editCaGridConfigurationDialog.setLocationRelativeTo(null);
				editCaGridConfigurationDialog.setVisible(true);
				
				String caGridConfigurationName = editCaGridConfigurationDialog.getCaGridName();
				if (caGridConfigurationName == null){ //user cancelled
					return;
				}				

				// Update the configuration
				updateConfiguration(caGridConfigurationName, editCaGridConfigurationDialog.getIndexServiceURL(),
						editCaGridConfigurationDialog.getCDSServiceURL(), editCaGridConfigurationDialog.getAuthNServiceURL(),
						editCaGridConfigurationDialog.getDorianServiceURL(), editCaGridConfigurationDialog.getProxyLifetime());			
			}			
		});
		updateButtonPanel.add(updateButton);
		
		// Button to restore default values for Training and Production CaGrid
		restoreDefaultsButton = new JButton("Restore defaults");
		restoreDefaultsButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {	
				String caGridConfigurationName =  (String)caGridNamesComboBox.getSelectedItem();
				
				if (caGridConfigurationName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME) ||
						caGridConfigurationName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME)){
					
					ConfigurationManager manager = ConfigurationManager.getInstance();
					String defaultConfiguration = configuration.getDefaultProperty(caGridConfigurationName);
					List<String> defaultConfigurationList = CaGridConfiguration.fromListText(defaultConfiguration);
					configuration.setPropertyStringList(caGridConfigurationName, defaultConfigurationList);
					try {
						manager.store(configuration);
					} catch (Exception ex) {
			            JOptionPane.showMessageDialog(null,
				                "Failed to remove configuration " + caGridConfigurationName, 
				                "Error",
				                JOptionPane.ERROR_MESSAGE);            
				            return;
				    }
					// Update the current configuration with the default values
					
					// Current configuration
					int index = caGridNamesComboBox.getSelectedIndex();
					
					indexServicesURLs.set(index, defaultConfigurationList.get(0));
					
					authNServicesURLs.set(index, defaultConfigurationList.get(1));
					
					dorianServicesURLs.set(index, defaultConfigurationList.get(2));
					
					proxyLifetimes.set(index, defaultConfigurationList.get(3));
					
					credentialDelegationServicesURLs.set(index, defaultConfigurationList.get(4));

					loadConfigurationDetails();
				}
			}			
		});
		updateButtonPanel.add(restoreDefaultsButton);

		configurationPanel.add(updateButtonPanel, c);
		
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addButton = new JButton("New");
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				NewEditCaGridConfigurationDialog newCaGridConfigurationDialog = new NewEditCaGridConfigurationDialog();
				newCaGridConfigurationDialog.setLocationRelativeTo(null);
				newCaGridConfigurationDialog.setVisible(true);
				
				String newCaGridConfigurationName = newCaGridConfigurationDialog.getCaGridName();
				if (newCaGridConfigurationName == null){ //user cancelled
					return;
				}
				
				addConfiguration(newCaGridConfigurationName, newCaGridConfigurationDialog.getIndexServiceURL(),
						newCaGridConfigurationDialog.getCDSServiceURL(), newCaGridConfigurationDialog.getAuthNServiceURL(),
						newCaGridConfigurationDialog.getDorianServiceURL(), newCaGridConfigurationDialog.getProxyLifetime());
			}
			
		});
		
		removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Production and Training CaGrid cannot be deleted - 
				// we have disabled the button when they are selected
				// but check here nevertheless.
				if(!caGridNamesComboBox.getSelectedItem().equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME) ||
						!caGridNamesComboBox.getSelectedItem().equals(CaGridConfiguration.TRAINING_CAGRID_NAME) ){
					configuration.deleteProperty((String) caGridNamesComboBox.getSelectedItem());
				}
				
	           int iSelected = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to remove configuration "
								+ ((String) caGridNamesComboBox
										.getSelectedItem()) + "?", "Warning",
										JOptionPane.YES_NO_OPTION);            
	            if (iSelected == JOptionPane.YES_OPTION) {
		            removeConfiguration();
	            }
			}
			
		});
		buttonsPanel.add(addButton);
		buttonsPanel.add(removeButton);
			
		// Set action for when caGrid name is selected to populate the 
		// details for that caGrid
		caGridNamesComboBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {	
				loadConfigurationDetails();
				String caGridName = (String)caGridNamesComboBox.getSelectedItem();
				if (caGridName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME) ||
						caGridName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME)){
					restoreDefaultsButton.setEnabled(true);
				}
				else{
					restoreDefaultsButton.setEnabled(false);
				}
			}
			
		});
		// Fire up the action for populating of the corresponding fields
		caGridNamesComboBox.setSelectedItem(CaGridConfiguration.PRODUCTION_CAGRID_NAME);
		

		JTextArea caGridText = new JTextArea(
				"View, add, edit or remove CaGrid configrations. A CaGrid configuration is a set of properties common to " +
				"all services belonging to a single CaGrid. Production and Training CaGrid configurations cannot be deleted.");
		caGridText.setLineWrap(true);
		caGridText.setWrapStyleWord(true);
		caGridText.setEditable(false);
		caGridText.setBorder(new EmptyBorder(10, 10, 10, 10));

		add(caGridText, BorderLayout.NORTH);
		add(configurationPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);
		
	}
	
	/**
	 *  Delete currently selected CaGrid configuration.
	 */
	protected void removeConfiguration() {
		
		indexServicesURLs.remove(caGridNamesComboBox.getSelectedIndex());
		
		authNServicesURLs.remove(caGridNamesComboBox.getSelectedIndex());
		
		dorianServicesURLs.remove(caGridNamesComboBox.getSelectedIndex());
		
		proxyLifetimes.remove(caGridNamesComboBox.getSelectedIndex());
		
		credentialDelegationServicesURLs.remove(caGridNamesComboBox.getSelectedIndex());
		
		// Delete from configuration
		String selectedCaGridConfigurationName = (String) caGridNamesComboBox.getSelectedItem();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		configuration.deleteProperty(selectedCaGridConfigurationName);
		try {
			manager.store(configuration);
		} catch (Exception e) {
            JOptionPane.showMessageDialog(this,
	                "Failed to remove CaGrid configuration", 
	                "Error",
	                JOptionPane.ERROR_MESSAGE);            
	            return;
	    }

		// Update CaGrid names
		caGridNames.remove(selectedCaGridConfigurationName);
		caGridNamesComboBoxModel.removeElement(selectedCaGridConfigurationName);
		// Reload current configuration's details
		loadConfigurationDetails();		
	}

	/**
	 * Add new CaGrid configuration.
	 */
	protected void addConfiguration(String caGridConfigurationName, String indexServiceURL,
			String credentialDelegationServiceURL, String authNServiceURL,
			String dorianServiceURL, String proxyLifetime) {

		List<String> configurationList = new ArrayList<String>();
		configurationList.add(indexServiceURL);
		indexServicesURLs.add(indexServiceURL);
		
		configurationList.add(authNServiceURL);
		authNServicesURLs.add(authNServiceURL);
		
		configurationList.add(dorianServiceURL);
		dorianServicesURLs.add(dorianServiceURL);
		
		configurationList.add(proxyLifetime);
		proxyLifetimes.add(proxyLifetime);
		
		configurationList.add(credentialDelegationServiceURL);
		credentialDelegationServicesURLs.add(credentialDelegationServiceURL);
		
		// Add the new configuration
		ConfigurationManager manager = ConfigurationManager.getInstance();
		configuration.setPropertyStringList(caGridConfigurationName, configurationList);
		try {
			manager.store(configuration);
		} catch (Exception e) {
            JOptionPane.showMessageDialog(this,
	                "Failed to add CaGrid configuration", 
	                "Error",
	                JOptionPane.ERROR_MESSAGE);            
	        return;
		}
		
		// Update CaGrid names
		caGridNames.add(caGridConfigurationName);
		caGridNamesComboBoxModel.addElement(caGridConfigurationName);
		caGridNamesComboBox.setSelectedItem(caGridConfigurationName);
		
		// Reload current configuration's details
		loadConfigurationDetails();
	}
	
	/**
	 * Update current CaGrid configuration.
	 */
	protected void updateConfiguration(String caGridConfigurationName, String indexServiceURL,
			String credentialDelegationServiceURL, String authNServiceURL,
			String dorianServiceURL, String proxyLifetime) {

		// Has CaGrid configuration's name been updated? If yes, we have to remove 
		// the previous one and add a new one
		if (!caGridConfigurationName.equals((String)caGridNamesComboBox.getSelectedItem())){
			removeConfiguration();
			addConfiguration(caGridConfigurationName, indexServiceURL, credentialDelegationServiceURL, 
					authNServiceURL, dorianServiceURL, proxyLifetime);
			return;
		}
		// Otherwise we just update the current one
		
		// Current configuration
		int index = caGridNamesComboBox.getSelectedIndex();
		
		List<String> configurationList = new ArrayList<String>();
		configurationList.add(indexServiceURL);
		indexServicesURLs.set(index, indexServiceURL);
		
		configurationList.add(authNServiceURL);
		authNServicesURLs.set(index, authNServiceURL);
		
		configurationList.add(dorianServiceURL);
		dorianServicesURLs.set(index, dorianServiceURL);
		
		configurationList.add(proxyLifetime);
		proxyLifetimes.set(index, proxyLifetime);
		
		configurationList.add(credentialDelegationServiceURL);
		credentialDelegationServicesURLs.set(index, credentialDelegationServiceURL);
		
		// Update the current configuration
		ConfigurationManager manager = ConfigurationManager.getInstance();
		configuration.setPropertyStringList(caGridConfigurationName, configurationList);
		try {
			manager.store(configuration);
		} catch (Exception e) {
            JOptionPane.showMessageDialog(this,
	                "Failed to update CaGrid configuration", 
	                "Error",
	                JOptionPane.ERROR_MESSAGE);            
	        return;
		}
		
		// Reload current configuration's details
		loadConfigurationDetails();
	}

	private void loadConfigurationDetails(){
		
		int index = caGridNamesComboBox.getSelectedIndex();
		
		jtfIndexServiceURL.setText(indexServicesURLs.get(index));
		jtfCredentialDelegationServiceURL.setText(credentialDelegationServicesURLs.get(index));
		jtfAuthNServiceURL.setText(authNServicesURLs.get(index));
		jtfDorianServiceURL.setText(dorianServicesURLs.get(index));
		if (proxyLifetimes.get(index).equals("")){
			jtfProxyLifetime.setText("");
		}
		else{
			jtfProxyLifetime.setText(proxyLifetimes.get(index)+"h");
		}
		
		// Default CaGrid configurations (Training and Production CaGrid) 
		// cannot be deleted. Restore defaults button only works for
		// Training and Production CaGrid
		String caGridName = (String)caGridNamesComboBox.getSelectedItem();

		if(caGridName.equals(CaGridConfiguration.PRODUCTION_CAGRID_NAME) ||
				caGridName.equals(CaGridConfiguration.TRAINING_CAGRID_NAME) ){
			removeButton.setEnabled(false);
			restoreDefaultsButton.setEnabled(true);
		}
		else{
			removeButton.setEnabled(true);
			restoreDefaultsButton.setEnabled(false);
		}
	}

}
