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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.activities.cagrid.config.CaGridConfiguration;

/**
 * Dialog to specify type of caGrid and WSDL URL of the caGrid service to be added to service panel.
 * 
 * @author Alex Nenadic
 *
 */

@SuppressWarnings("serial")
public abstract class CaGridServiceFromWSDLDialog extends JDialog{

	// CaGrid name the user wishes to add service from (e.g. Training, Production , etc.) that can
	// be configured from the preferences panel
	private String[] caGridNames;
	private JComboBox caGridNamesComboBox;

	// Index Services (one per caGrid)
	private String[] indexServicesURLs;

	private JTextField caGridServiceWSDLTextField;

	public CaGridServiceFromWSDLDialog(){
		super((Frame) null, "Add caGrid service from a WSDL location", true, null); // create a modal dialog
		initComponents();
	}

	private void initComponents() {

		CaGridConfiguration configuration = CaGridConfiguration.getInstance();
		// Get default list of CaGridS - keys in the map contain CaGrid names and values contain 
		// various properties set for the CaGrid (Index Service URL, AuthN service URL, Dorian Service URL).
		HashSet<String> caGridNamesSet = new HashSet<String>(configuration.getDefaultPropertyMap().keySet());
		// Get all other CaGridS that may have been configured though preferences (may include 
		// the default ones as well if some of their values have been changed), 
		// but set will ignore duplicates so we are OK
		caGridNamesSet.addAll(configuration.getKeys());
		caGridNames = caGridNamesSet.toArray(new String[caGridNamesSet.size()]);
		caGridNamesComboBox = new JComboBox(caGridNames);
		
		// Get Index Service URLs for all caGrids
		indexServicesURLs = new String[caGridNames.length];
		for (int i = 0; i< caGridNames.length; i++){
			indexServicesURLs[i] = configuration.getPropertyStringList(caGridNames[i]).get(0);
		}
		
        JPanel servicePanel = new JPanel(new BorderLayout());
        servicePanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));

        // caGrid name combobox
        JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridPanel.add(new JLabel("Select grid"));
        caGridNamesComboBox = new JComboBox(caGridNames);
        gridPanel.add(caGridNamesComboBox);
        
        JPanel wsdlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wsdlPanel.add(new JLabel("Service WSDL's URL"));
        caGridServiceWSDLTextField = new JTextField(50);
        caGridServiceWSDLTextField.setMinimumSize(new Dimension(50, caGridServiceWSDLTextField.getPreferredSize().height));
        wsdlPanel.add(caGridServiceWSDLTextField);
        
        servicePanel.add(gridPanel, BorderLayout.NORTH);
        servicePanel.add(wsdlPanel, BorderLayout.SOUTH);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
								
				addRegistry(caGridServiceWSDLTextField.getText(),
						getCaGridName(),
						getIndexServiceURL());

				setVisible(false);
				dispose();				
			}
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();				
			}
        });
        
        // Panel with buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);        
        
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(servicePanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);

        // Set the default CaGrid to Production CaGrid
		caGridNamesComboBox.setSelectedItem(CaGridConfiguration.PRODUCTION_CAGRID_NAME);

        pack();
	}
	
	public String getWsdlURL(){
		return caGridServiceWSDLTextField.getText();
	}
	
	
	public String getIndexServiceURL(){
		return indexServicesURLs[caGridNamesComboBox.getSelectedIndex()];
	}
	
    /**
     * 
     * @return the selected CaGrid name
     */
    public String getCaGridName() {
        return (String)caGridNamesComboBox.getSelectedItem();
    }
	
	protected abstract void addRegistry(String wsdlURL, String caGridName, String indexServiceURL) ;
}
