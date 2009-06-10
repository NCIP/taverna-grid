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
 * Security properties of a caGrid service (operation) obtained from 
 * the getServiceSecurityMetadataMathod() and cached for each Taverna 
 * run. 
 * 
 * @author Alex Nenadic
 *
 */
public class CaGridActivitySecurityProperties {

	// GSI_TRANSPORT property, possible values SIGNATURE (shown as 'Integrity' in Introduce) 
	// and ENCRYPTION (shown as 'Privacy' in Introduce) 
	private Integer gsiTransport;
	private Boolean gsiAnonymouos;
	private Authorization gsiAuthorisation;
	// GSI_SEC_CONV property, possible values SIGNATURE and ENCRYPTION
	private Integer gsiSecureConversation;
	// GSI_SEC_MESG property, possible values SIGNATURE and ENCRYPTION
	private Integer gsiSecureMessage;
	private String gsiMode;
	
	private transient GSSCredential gsi_credential; // GSSCredential wraps the proxy used for context initiation, acceptance or both
	private transient GlobusCredential proxy; // proxy
	
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
