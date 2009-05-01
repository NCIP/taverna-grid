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

import net.sf.taverna.t2.activities.cagrid.query.ServiceQuery;
import net.sf.taverna.t2.lang.beans.PropertyAnnotated;

/**
 * 
 * @author Alex Nenadic
 *
 */
public class CaGridServiceProviderConfig extends PropertyAnnotated {
	
	private String indexServiceURL;
	private ServiceQuery[] serviceQueryList; 
	private String defaultAuthNServiceURL;
	private String defaultDorianServiceURL;
	
	public CaGridServiceProviderConfig() {
	}
	
	public CaGridServiceProviderConfig(String indexServiceURL,
			ServiceQuery[] serviceQueryList, String authNServiceURL,
			String dorianServiceURL) {
		this.indexServiceURL = indexServiceURL;
		this.setServiceQueryList(serviceQueryList);
		this.setDefaultAuthNServiceURL(authNServiceURL);
		this.setDefaultDorianServiceURL(dorianServiceURL);
	}

	public String getIndexServiceURL() {
		return indexServiceURL;
	}

	public void setIndexServiceURL(String indexServiceURL) {
		this.indexServiceURL = indexServiceURL;
	}

	public void setServiceQueryList(ServiceQuery[] serviceQueryList) {
		this.serviceQueryList = serviceQueryList;
	}

	public ServiceQuery[] getServiceQueryList() {
		return serviceQueryList;
	}

	public void setDefaultAuthNServiceURL(String authNServiceURL) {
		this.defaultAuthNServiceURL = authNServiceURL;
	}

	public String getDefaultAuthNServiceURL() {
		return defaultAuthNServiceURL;
	}

	public void setDefaultDorianServiceURL(String dorianServiceURL) {
		this.defaultDorianServiceURL = dorianServiceURL;
	}

	public String getDefaultDorianServiceURL() {
		return defaultDorianServiceURL;
	}
	
}
