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
package net.sf.taverna.cagrid.activity;

/**
 * A standard Java Bean that provides the details required to configure a CaGridActivity.
 *
 * @author Alex Nenadic
 * Modified by Wei Tan on 1/26/2010, adding CDS URL in CaGridActivity configuration
 */
public class CaGridActivityConfigurationBean {

	// WSDL url
    private String wsdl;
    // Operation/method on the service to be invoked
    private String operation;
    // SOAP style binding
    private String style;
    
    // Name of the caGrid the service belongs to, used to locate preferences for this caGrid such as
    // Index, Dorian and AuthN services. 
    private String caGridName;
    // Users can override the values from preferences for AuthN and Dorian services for this particular WSDL operation using this bean
	private String authNServiceURL = null; // URL of the AuthN Service used or to be used to (re)authenticate the user
	private String dorianServiceURL = null; // URL of the Dorian Service used or to be used to (re)issue proxy
	private String cdsServiceURL = null; // URL of the CDS (credential Delegation) Service used or to be used to (re)issue proxy
	/** Creates a new instance of CaGridActivityConfigurationBean */
    public CaGridActivityConfigurationBean() {
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

	public void setStyle(String style) {
		this.style = style;
	}

	public String getStyle() {
		return style;
	}
	
    public void setCaGridName(String caGridName) {
		this.caGridName = caGridName;
	}

	public String getCaGridName() {
		return caGridName;
	}

	public void setAuthNServiceURL(String authNServiceURL) {
		this.authNServiceURL = authNServiceURL;
	}

	public String getAuthNServiceURL() {
		return authNServiceURL;
	}

	public void setDorianServiceURL(String dorianServiceURL) {
		this.dorianServiceURL = dorianServiceURL;
	}

	public String getDorianServiceURL() {
		return dorianServiceURL;
	}

	public void setCDSURL(String cdsServiceURL) {
		this.cdsServiceURL = cdsServiceURL;
	}

	public String getCDSURL() {
		return cdsServiceURL;
	}

}
