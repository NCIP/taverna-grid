/*******************************************************************************
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
package net.sf.taverna.t2.workbench.cagrid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.cagrid.activity.config.CaGridConfiguration;



/**
 * Panel with options to configure CDActivity.
 * 
 * @author Wei Tan
 *
 */
@SuppressWarnings("serial")
public class CDSAndTransferConfDialog extends JDialog {
	
	private boolean needCDS;
	private boolean needUpload;

	private String caGridName;
	private String party;
	

	private int delegationLifetime;
	private int delegationPathLength;
	private int issuedCredentialLifetime;
	private int issuedCredentialPathLength;
	
	private String fileToUpload;
	public String getFileToUpload() {
		return fileToUpload;
	}

	public void setFileToUpload(String fileToUpload) {
		this.fileToUpload = fileToUpload;
	}


	private JTextField jtfFileName;
	
	private JTextField jtfParty;
	private JTextField jtfDelegationLifetime;
	private JTextField jtfDelegationPathLength;
	private JTextField jtfIssuedCredentialLifetime;
	private JTextField jtfIssuedCredentialPathLength;
	
	
	JComboBox caGridList;
	
	//TODO change it to 1.4
	
	public static String PRODUCTION_CAGRID_NAME =  "NCI Production CaGrid 1.3";
	public static String PARTY_STRING = "/O=caBIG/OU=caGrid/OU=Services/CN=cagrid-fqp.nci.nih.gov";
	
	
	public CDSAndTransferConfDialog(boolean needCDS, boolean needUpload) {
		
		super((Frame)null, "Configuration for Credential Delegation and caGrid Transfer (if any)", true);
		//are CDS and transfer needed?
		this.needCDS = needCDS;
		this.needUpload = needUpload;
		
		//get caGrid Name list from caGrid configuration
		CaGridConfiguration configuration = CaGridConfiguration.getInstance();
		HashSet<String> caGridNamesSet = new HashSet<String>(configuration.getDefaultPropertyMap().keySet());
		caGridNamesSet.addAll(configuration.getKeys());
		String[] caGridNamesStrings =  new String [caGridNamesSet.size()];
		caGridNamesStrings = (String[]) caGridNamesSet.toArray(caGridNamesStrings);
		caGridList = new JComboBox(caGridNamesStrings);
		caGridList.setSelectedItem(PRODUCTION_CAGRID_NAME);	
		initComponents();
	}

	public void setDelegationPathLength(int delegationPathLength) {
		this.delegationPathLength = delegationPathLength;
	}

	public int getDelegationPathLength() {
		return delegationPathLength;
	}

	private void initComponents() {
		
		delegationLifetime = 4;
		delegationPathLength = 1;
		issuedCredentialLifetime = 1;
		issuedCredentialPathLength = 0;

		JPanel configurationPanel = new JPanel();
		configurationPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		configurationPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
	
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel instructionsPanel = new JPanel();
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
		JLabel instructions = new JLabel(
				"<html><body>Configure the CDS and caTransfer Properties here</body></html>");
		instructions.setBorder(new EmptyBorder(5,5,10,5));
		instructions.setFont(new Font(null, Font.PLAIN, 11));
		instructionsPanel.add(instructions);
		configurationPanel.add(instructionsPanel, c);
		
		c.gridwidth = 1;
				
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlCaGrid = new JLabel("CaGrid Name");
		jlCaGrid.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlCaGrid, c);
		jlCaGrid.setEnabled(needCDS);
		
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(caGridList, c);
		caGridList.setEnabled(needCDS);

		
			
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlParty = new JLabel("Party Delegated To");
		jlParty.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlParty, c);
		jlParty.setEnabled(needCDS);
		
		jtfParty = new JTextField(40);
		jtfParty.setText(PARTY_STRING);
		c.gridx = 1;
		c.gridy = 2;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfParty, c);
		jtfParty.setEnabled(needCDS);
		
		jtfDelegationLifetime = new JTextField(35);
		jtfDelegationLifetime.setText(String.valueOf(delegationLifetime));
		
		c.gridx = 0;
		c.gridy = 3;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlDelegationLifetime = new JLabel("Delegation Lifetime (hr)");
		jlDelegationLifetime.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlDelegationLifetime, c);
		jlDelegationLifetime.setEnabled(needCDS);
		
		c.gridx = 1;
		c.gridy = 3;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfDelegationLifetime, c);
		jtfDelegationLifetime.setEnabled(needCDS);
		
		c.gridx = 0;
		c.gridy = 4;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlDelegationPathLength = new JLabel("Delegation PathLength");
		jlParty.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlDelegationPathLength, c);
		jlDelegationPathLength.setEnabled(needCDS);
		
		
		jtfDelegationPathLength = new JTextField(35);
		jtfDelegationPathLength.setText(String.valueOf(delegationPathLength));
		
		c.gridx = 1;
		c.gridy = 4;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfDelegationPathLength, c);
		jtfDelegationPathLength.setEnabled(needCDS);
		
		//issuedCredentialLifetime 
		
		jtfIssuedCredentialLifetime = new JTextField(35);
		jtfIssuedCredentialLifetime.setText(String.valueOf(issuedCredentialLifetime));
		c.gridx = 0;
		c.gridy = 5;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlIssuedCredentialLifetime = new JLabel("IssuedCredential Lifetime (hr)");
		jlParty.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlIssuedCredentialLifetime, c);
		jlIssuedCredentialLifetime.setEnabled(needCDS);
		
		
		c.gridx = 1;
		c.gridy = 5;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfIssuedCredentialLifetime, c);
		jtfIssuedCredentialLifetime.setEnabled(needCDS);
		
		//issuedCredentialPathLength 
		jtfIssuedCredentialPathLength = new JTextField(35);
		jtfIssuedCredentialPathLength.setText(String.valueOf(issuedCredentialPathLength));
		c.gridx = 0;
		c.gridy = 6;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		JLabel jlIssuedCredentialPathLength = new JLabel("IssuedCredential PathLength");
		jlParty.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(jlIssuedCredentialPathLength, c);
		jlIssuedCredentialPathLength.setEnabled(needCDS);
		
		c.gridx = 1;
		c.gridy = 6;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfIssuedCredentialPathLength, c);
		jtfIssuedCredentialPathLength.setEnabled(needCDS);
		
		//file upload 
		
		c.gridx = 0;
		c.gridy = 7;
		c.ipadx = 5;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		
		JButton openButton = new JButton("Upload a File...");
		
		openButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				openPressed();
			}
		});

		//JLabel jlFileUpload = new JLabel("File To Upload");
		openButton.setBorder(new EmptyBorder(5,5,5,5));
		configurationPanel.add(openButton, c);
		openButton.setEnabled(needUpload);
		
		jtfFileName = new JTextField(35);
		c.gridx = 1;
		c.gridy = 7;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		configurationPanel.add(jtfFileName, c);
		jtfFileName.setEnabled(needUpload);
		
		// Buttons
		c.gridx = 0;
		c.gridy = 8;
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

	
	 /**
     * Upload a File button pressed or otherwise activated.
     */
    private void openPressed()
    {
    	JFileChooser fc = new JFileChooser();
    	int returnVal = fc.showOpenDialog(CDSAndTransferConfDialog.this);

         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             //This is where a real application would open the file.
             fileToUpload = file.getPath();
             jtfFileName.setText(fileToUpload);
         }

    }
	
    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
    	caGridName = (String) caGridList.getSelectedItem();
    	party = jtfParty.getText();
    	delegationLifetime = Integer.valueOf(jtfDelegationLifetime.getText());
    	delegationPathLength = Integer.valueOf(jtfDelegationPathLength.getText());
    	issuedCredentialLifetime = Integer.valueOf(jtfIssuedCredentialLifetime.getText());
    	issuedCredentialPathLength = Integer.valueOf(jtfIssuedCredentialPathLength.getText());	
    	fileToUpload = jtfFileName.getText();
        closeDialog();
    }

	/**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	
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

	public void setIssuedCredentialLifetime(int issuedCredentialLifetime) {
		this.issuedCredentialLifetime = issuedCredentialLifetime;
	}

	public int getIssuedCredentialLifetime() {
		return issuedCredentialLifetime;
	}

	public void setIssuedCredentialPathLength(int issuedCredentialPathLength) {
		this.issuedCredentialPathLength = issuedCredentialPathLength;
	}

	public int getIssuedCredentialPathLength() {
		return issuedCredentialPathLength;
	}

	public void setCaGridName(String caGridName) {
		this.caGridName = caGridName;
	}

	public String getCaGridName() {
		return caGridName;
	}



	public int getDelegationLifetime() {
		return delegationLifetime;
	}



	public void setDelegationLifetime(int delegationLifetime) {
		this.delegationLifetime = delegationLifetime;
	}
	public String getParty() {
		return party;
	}


	public void setParty(String party) {
		this.party = party;
	}

}
