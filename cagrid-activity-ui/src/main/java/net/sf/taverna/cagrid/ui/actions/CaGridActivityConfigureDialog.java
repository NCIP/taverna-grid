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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
 *
 */
@SuppressWarnings("serial")
public class CaGridActivityConfigureDialog extends JDialog {

	private String authNServiceURL;
	private String dorianServiceURL;

	private JTextField jtfAuthNServiceURL;
	private JTextField jtfDorianServiceURL;
	
	public CaGridActivityConfigureDialog(
			CaGridActivityConfigurationBean configurationBean) {

		super((Frame)null, "CaGrid Activity Configuration", true);
		// Create new string object so that we do not actually change something in the bean
		if (configurationBean.getAuthNServiceURL() != null)
			this.authNServiceURL = new String(configurationBean.getAuthNServiceURL());
		if (configurationBean.getDorianServiceURL() != null)
			this.dorianServiceURL = new String(configurationBean.getDorianServiceURL());
		initComponents();
	}

	private void initComponents() {

		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		configurationPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
	
		// TODO put some instructions for users to know that setting to empty string actually deletes the property
		
		jtfAuthNServiceURL = new JTextField(35);
		jtfAuthNServiceURL.setText(authNServiceURL);
		
		jtfDorianServiceURL = new JTextField(35);
		jtfDorianServiceURL.setText(dorianServiceURL);
			
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Authentication Service"), c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfAuthNServiceURL, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		configurationPanel.add(new JLabel("Dorian Service"), c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfDorianServiceURL, c);
		
		// Buttons
		c.gridx = 0;
		c.gridy = 3;
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
