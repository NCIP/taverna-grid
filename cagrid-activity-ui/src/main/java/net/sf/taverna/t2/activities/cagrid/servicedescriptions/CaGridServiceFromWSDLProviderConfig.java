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

/**
 * 
 * @author Alex Nenadic
 *
 */
public class CaGridServiceFromWSDLProviderConfig {

	private String wsdlURL;
	private String indexServiceURL;
	private String defaultAuthNServiceURL;
	private String defaultDorianServiceURL;

	public CaGridServiceFromWSDLProviderConfig(String wsdlURL, String indexServiceURL,
			String authNServiceURL,
			String dorianServiceURL) {
		this.indexServiceURL = indexServiceURL;
		this.setDefaultAuthNServiceURL(authNServiceURL);
		this.setDefaultDorianServiceURL(dorianServiceURL);
	}

	public CaGridServiceFromWSDLProviderConfig() {
	}

	public String getIndexServiceURL() {
		return indexServiceURL;
	}

	public void setIndexServiceURL(String indexServiceURL) {
		this.indexServiceURL = indexServiceURL;
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

	public void setWsdlURL(String wsdlURL) {
		this.wsdlURL = wsdlURL;
	}

	public String getWsdlURL() {
		return wsdlURL;
	}


}
