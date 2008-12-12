/******************************************************************************* 
 * Copyright (C) 2008 The University of Chicago
 * @author Wei Tan
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.T2Reference;


//classes to invoke the caGrid service to execute a given workflow
public class CaGridRun {
	//workflow instance identifier
	public int workflowid;
	//workflow inputs
	private Map<String, T2Reference> inputs;
	//time for execution
	private Date date;
	//xml string that represent the execution results -- may be multiple results
	public Map<String, String> outputMap;
	//the container to show multiple results
	public JPanel resultPanel;
	public String status;// initiated, executing, completed
	CaGridRun(){
		inputs =null;
		date = new Date();
		Random generator = new Random();
		workflowid = generator.nextInt();
		resultPanel = new JPanel();
		resultPanel.setName(String.valueOf(workflowid));
		status = "initiated";
		outputMap = null;
		
	}

}
