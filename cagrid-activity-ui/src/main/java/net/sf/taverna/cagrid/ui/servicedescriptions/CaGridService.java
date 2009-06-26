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
package net.sf.taverna.cagrid.ui.servicedescriptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Wei Tan
 */
public class CaGridService {
	
	private String wsdlLocation;	
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
