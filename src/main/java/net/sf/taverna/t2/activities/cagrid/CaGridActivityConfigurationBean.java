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

import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.ietf.jgss.GSSCredential;

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
    // SOAP style binding
    private String style;
    
	// Security settings for this operation of a caGrid service, if any, obtained by invoking
	// getServiceSecurityMetadata() on the service
    private boolean isSecure;
	private String indexServiceURL; // URL of the Index Service used to discover this caGrid service (used as alias for username/password and proxy entries in the Taverna's keystore)
	private String authNServiceURL; // URL of the AuthN Service used or to be used to (re)authenticate the user
	private String dorianServiceURL; // URL of the Dorian Service used or to be used to (re)issue proxy
	private Integer gsiTransport;
	private Boolean gsiAnonymouos;
	private Authorization gsiAuthorisation;
	private Integer gsiSecureConversation;
	private Integer gsiSecureMessage;
	private String gsiMode;
	// Do not serialise gsi_credential and proxy - we do not want these to appear in the saved wf file
	private transient GSSCredential gsi_credential; // GSSCredential wraps the proxy used for context initiation, acceptance or both
	private transient GlobusCredential proxy; // proxy
	
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

	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}

	public boolean isSecure() {
		return isSecure;
	}

	public void setGSITransport(Integer gsi_transport) {
		this.gsiTransport = gsi_transport;
	}

	public Integer getGSITransport() {
		return gsiTransport;
	}

	public void setGSIAnonymouos(Boolean gsi_anonymouos) {
		this.gsiAnonymouos = gsi_anonymouos;
	}

	public Boolean getGSIAnonymouos() {
		return gsiAnonymouos;
	}

	public void setGSIAuthorisation(Authorization authorisation) {
		this.gsiAuthorisation = authorisation;
	}

	public Authorization getGSIAuthorisation() {
		return gsiAuthorisation;
	}

	public void setGSISecureConversation(Integer gsi_sec_conv) {
		this.gsiSecureConversation = gsi_sec_conv;
	}

	public Integer getGSISecureConversation() {
		return gsiSecureConversation;
	}

	public void setGSISecureMessage(Integer gsi_sec_msg) {
		this.gsiSecureMessage = gsi_sec_msg;
	}

	public Integer getGSISecureMessage() {
		return gsiSecureMessage;
	}

	public void setGSIMode(String gsi_mode) {
		this.gsiMode = gsi_mode;
	}

	public String getGSIMode() {
		return gsiMode;
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

	public void setGSICredential(GSSCredential gsi_credential) {
		this.gsi_credential = gsi_credential;
	}

	public GSSCredential getGSICredential() {
		return gsi_credential;
	}

	public void setProxy(GlobusCredential proxy) {
		this.proxy = proxy;
	}

	public GlobusCredential getProxy() {
		return proxy;
	}


}
