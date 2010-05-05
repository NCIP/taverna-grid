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

//import java.awt.BorderLayout;
import gov.nih.nci.cagrid.workflow.factory.client.TavernaWorkflowServiceClient;
import java.rmi.RemoteException;
import java.util.Date;
//import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//import javax.swing.JTextArea;
import java.io.File;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.globus.gsi.GlobusCredential;

import workflowmanagementfactoryservice.WorkflowStatusType;
import net.sf.taverna.t2.reference.T2Reference;


//classes to invoke the caGrid service to execute a given workflow
public class CaGridRun {
	//workflow instance identifier, also used to persist EPR
	public long workflowid;
	public String workflowLocalName;
	//workflow inputs
	private Map<String, T2Reference> inputs;
	//properties like security and transfer
	public WFProperties wfp;
	public GlobusCredential proxy;
	//started time
	public Date date;
	//xml string that represent the execution results -- may be multiple results
	public Map<String, String> outputMap;
	//the container to show multiple results
	public JPanel resultPanel;
	
	EndpointReferenceType workflowEPR;
	WorkflowStatusType workflowStatusElement; //status
	String url;
	
	CaGridRun(String url, String workflowName){
		/*
		try {
			TavernaWorkflowServiceClient client = new TavernaWorkflowServiceClient(url);
		} catch (MalformedURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		this.url = url;
		workflowLocalName = workflowName;
		inputs =null;
		date = new Date();
		workflowid = date.getTime();
		resultPanel = new JPanel();
		resultPanel.setName(String.valueOf(workflowid));
		workflowStatusElement = WorkflowStatusType.Active;
		outputMap = null;
		workflowEPR =  null;
		proxy = null;
	}
	public EndpointReferenceType readEPRFromFile(){
		return null;
		
	}
	public boolean writeEPRToFile(){
		if(workflowEPR==null){
			return false;
		}
		else {
			try {
				TavernaWorkflowServiceClient.writeEprToFile(workflowEPR, String.valueOf(workflowid));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;			
		}	
	}
	public boolean updateStatus(){
		try {
			workflowStatusElement = TavernaWorkflowServiceClient.getStatus(workflowEPR);
		} catch (MalformedURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	public String toString() {
		return workflowLocalName+"@"+date.toString();
	}

}
