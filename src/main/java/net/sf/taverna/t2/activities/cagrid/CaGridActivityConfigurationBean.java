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

import org.globus.wsrf.impl.security.authorization.Authorization;

/**
 * A standard Java Bean that provides the details required to configure a CaGridActivity.
 *
 * @author Alex Nenadic
 */
public class CaGridActivityConfigurationBean {

	// WSDL url
    private String wsdl;
    
    // Operation/method on the service to be invoked
    private String operation;
    
	// Security settings for this operation of a caGrid service, if any, obtained by invoking
	// getServiceSecurityMetadata() on the service
	private String indexServiceURL; // URL of the Index Service used to discover this caGrid service (used as alias for username/password and proxy entries in the Taverna's keystore)
	private String authNServiceURL; // URL of the AuthN Service used or to be used to (re)authenticate the user
	private String dorianServiceURL; // URL of the Dorian Service used or to be used to (re)issue proxy
	private Integer gsi_transport;
	private Boolean gsi_anonymouos;
	private Authorization authorisation;
	private Integer gsi_secure_conversation;
	private Integer gsi_secure_message;
	private String gsi_mode;
	
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

	public void setGSITransport(Integer gsi_transport) {
		this.gsi_transport = gsi_transport;
	}

	public Integer getGSITransport() {
		return gsi_transport;
	}

	public void setGSIAnonymouos(Boolean gsi_anonymouos) {
		this.gsi_anonymouos = gsi_anonymouos;
	}

	public Boolean getGSIAnonymouos() {
		return gsi_anonymouos;
	}

	public void setAuthorisation(Authorization authorisation) {
		this.authorisation = authorisation;
	}

	public Authorization getAuthorisation() {
		return authorisation;
	}

	public void setGSISecureConversation(Integer gsi_sec_conv) {
		this.gsi_secure_conversation = gsi_sec_conv;
	}

	public Integer getGSISecureConversation() {
		return gsi_secure_conversation;
	}

	public void setGSISecureMessage(Integer gsi_sec_msg) {
		this.gsi_secure_message = gsi_sec_msg;
	}

	public Integer getGSISecureMessage() {
		return gsi_secure_message;
	}

	public void setGSIMode(String gsi_mode) {
		this.gsi_mode = gsi_mode;
	}

	public String getGSIMode() {
		return gsi_mode;
	}

	public void setIndexServiceURL(String indexServiceURL) {
		this.indexServiceURL = indexServiceURL;
	}

	public String getIndexServiceURL() {
		return indexServiceURL;
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


}
