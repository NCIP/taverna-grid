
/*
 * Copyright (C) 2008 The University of Chicago 
 *
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: CaGridService.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/04 18:36:09 $
 *               by   $Author: tanw $
 * Created on 01-Dec-2007
 *****************************************************************/
package net.sf.taverna.t2.activities.cagrid.query;

import java.util.ArrayList;
import java.util.List;

public class CaGridService {
	
	private String wsdlLocation;
	//TODO: add more service metadata to enable sorting
	
	private String serviceName;
	private List<String> operations = new ArrayList<String>();
	private String researchCenterName;
	
	public void setResearchCenterName(String name){
		researchCenterName = name;
	}
	public String getResearchCenterName(){
		return researchCenterName;
	}
	
	
	public CaGridService(String location, String name){
		this.wsdlLocation = location;
		this.serviceName = name;
		researchCenterName = "";
	}
	
	
	public boolean addOperation(String s) {
		return operations.add(s);
	}

	public String getServiceName() {
		return serviceName;
	}
	public String getServiceWSDLLocation(){
		return wsdlLocation;
	}

	public List<String> getOperations() {
		return operations;
	}
	
}
