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
package net.sf.taverna.cagrid.ui.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.cagrid.activity.CaGridActivityConfigurationBean;

/**
 * Panel with options to configure CaGridActivity.
 * 
 * @author Alex Nenadic
 * Modified by Wei Tan on 1/26/2010 to add CDS URL
 *
 */
@SuppressWarnings("serial")
public class CaGridActivityConfigureDialog extends JDialog {

	private String authNServiceURL;
	private String dorianServiceURL;
	private String cdsServiceURL;
	

	private JTextField jtfAuthNServiceURL;
	private JTextField jtfDorianServiceURL;
	private JTextField jtfCDSServiceURL;
	
	public CaGridActivityConfigureDialog(
			CaGridActivityConfigurationBean configurationBean) {

		super((Frame)null, "CaGrid Activity Configuration", true);
		// Create new string object so that we do not actually change something in the bean
		if (configurationBean.getAuthNServiceURL() != null)
			this.authNServiceURL = new String(configurationBean.getAuthNServiceURL());
		if (configurationBean.getDorianServiceURL() != null)
			this.dorianServiceURL = new String(configurationBean.getDorianServiceURL());
		if (configurationBean.getCDSURL() != null)
			this.cdsServiceURL = new String(configurationBean.getCDSURL());
		initComponents();
	}

	private void initComponents() {

		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		configurationPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
	
		// Instructions for users to know that setting to empty string actually deletes the property
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel instructionsPanel = new JPanel();
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
		JLabel instructions = new JLabel(
				"<html><body>Leaving Authentication or Dorian Service properties empty (or deleting them here)<br>" +
				"means that they will be picked up from preferences for this CaGrid.</body></html>");
		instructions.setBorder(new EmptyBorder(5,5,10,5));
		instructions.setFont(new Font(null, Font.PLAIN, 11));
		instructionsPanel.add(instructions);
		configurationPanel.add(instructionsPanel, c);
		
		c.gridwidth = 1;

		jtfAuthNServiceURL = new JTextField(35);
		jtfAuthNServiceURL.setText(authNServiceURL);
		
		jtfDorianServiceURL = new JTextField(35);
		jtfDorianServiceURL.setText(dorianServiceURL);
		
		jtfCDSServiceURL = new JTextField(35);
		jtfCDSServiceURL.setText(cdsServiceURL);
			
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlAuthNService = new JLabel("Authentication Service");
		jlAuthNService.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlAuthNService, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfAuthNServiceURL, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlDorianService = new JLabel("Dorian Service");
		jlDorianService.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlDorianService, c);
		
		c.gridx = 1;
		c.gridy = 2;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfDorianServiceURL, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlCDSService = new JLabel("CDS Service");
		jlAuthNService.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlCDSService, c);
		
		c.gridx = 1;
		c.gridy = 3;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfCDSServiceURL, c);
		
		// Buttons
		c.gridx = 0;
		c.gridy = 4;
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
		
	}

	public String getAuthNServiceURL() {
		return authNServiceURL;
	}

	public String getDorianServiceURL() {
		return dorianServiceURL;
	}
	public String getCDSServiceURL() {
		return cdsServiceURL;
	}
	
    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
    	authNServiceURL = jtfAuthNServiceURL.getText();
    	dorianServiceURL = jtfDorianServiceURL.getText();
        closeDialog();
    }

	/**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	authNServiceURL = null;
    	dorianServiceURL = null;
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
