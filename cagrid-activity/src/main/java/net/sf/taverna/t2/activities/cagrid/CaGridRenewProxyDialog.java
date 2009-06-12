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
package net.sf.taverna.t2.activities.cagrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Dialog that asks user if he wished to renew their proxy certificate.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class CaGridRenewProxyDialog extends JDialog{

	private boolean renewProxy;
	private boolean renewProxyAskMeAgain;
	private String caGridName;
	
    public CaGridRenewProxyDialog(String caGridName)
    {
        super((Frame)null, caGridName + "proxy renewal", true);    
        this.caGridName = caGridName;
        initComponents();
    }

	private void initComponents() {
	      getContentPane().setLayout(new BorderLayout());

	      JLabel jlInstructions1 = new JLabel(
				"Your proxy for "
						+ caGridName
						+ " will expire in less than an hour.");
	      JLabel jlInstructions2 = new JLabel("Do you wish to renew it?");
	      jlInstructions1.setBorder(new EmptyBorder(5,5,0,5));
	      jlInstructions2.setBorder(new EmptyBorder(0,5,5,5));
	      JPanel jpInstructions = new JPanel();
	      jpInstructions.setLayout(new BoxLayout(jpInstructions, BoxLayout.Y_AXIS));
	      jpInstructions.setBorder(new CompoundBorder(
	                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
	      jpInstructions.add(jlInstructions1);
	      jpInstructions.add(jlInstructions2);
	      
	       JButton jbYes = new JButton("Yes");
	       jbYes.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	                renewProxy = true;
	                closeDialog();
	            }
	        });
	        JButton jbNo = new JButton("No");
	        jbNo.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	renewProxy = false;
	            	renewProxyAskMeAgain = true;
	                closeDialog();
	            }
	        });
	        JButton jbNoAndDoNotAskPressed = new JButton("No and do not ask again");
	        jbNoAndDoNotAskPressed.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	renewProxy = false;
	            	renewProxyAskMeAgain = false;
	                closeDialog();
	            }
	        });
	        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
	        jpButtons.add(jbYes);
	        jpButtons.add(jbNo);
	        jpButtons.add(jbNoAndDoNotAskPressed);
	      
	        jpInstructions.setMinimumSize(new Dimension(300,100));
	        
	        getContentPane().add(jpInstructions, BorderLayout.CENTER);
	        getContentPane().add(jpButtons, BorderLayout.SOUTH);
	        
	        setResizable(false);

	        getRootPane().setDefaultButton(jbYes);

	        pack();

	} 
	
    /**
     * Check if user wishes to renew proxy.
     * @return
     */
    public boolean renewProxy(){
    	return renewProxy;
    }
    
    /**
     * Check if user wishes to be asked again to renew proxy after he has declined once.
     * @return
     */
    public boolean renewProxyAskMeAgain(){
    	return renewProxyAskMeAgain;
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
