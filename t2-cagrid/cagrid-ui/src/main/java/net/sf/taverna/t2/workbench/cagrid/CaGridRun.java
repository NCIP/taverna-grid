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
		
	}

}
