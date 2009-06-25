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
package net.sf.taverna.t2.activities.cagrid.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.ietf.jgss.GSSCredential;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CaGridActivityItem extends AbstractActivityItem {

	private String use;
	private String url;
	private String style;
	private String operation;
	private String researchCenter;
	
	// Name of the caGrid the service belongs to
	private String caGridName;
	
	// Security settings for this operation of a caGrid service, if any, obtained by invoking
	// getServiceSecurityMetadata() on the service
	private String indexServiceURL; // URL of the Index Service used to discover this caGrid service (used as alias for username/password and proxy entries in the Taverna's keystore)
	private String authNServiceURL; // URL of the AuthN Service used or to be used to (re)authenticate the user
	private String dorianServiceURL; // URL of the Dorian Service used or to be used to (re)issue proxy
	private boolean isSecure = false;
	private Integer gsi_transport;
	private Boolean gsi_anonymouos;
	private Authorization authorisation;
	private Integer gsi_secure_conversation;
	private Integer gsi_secure_message;
	private String gsi_mode;
	private GSSCredential gsi_credential; // GSSCredential wraps the proxy used for context initiation, acceptance or both
	private GlobusCredential proxy; // proxy
	
	public void setCaGridName(String caGridName) {
		this.caGridName = caGridName;
	}

	public String getCaGridName() {
		return caGridName;
	}

	public String getResearchCenter() {
		return researchCenter;
	}

	public void setResearchCenter(String rc) {
		this.researchCenter = rc;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getType() {
		return "CaGrid Services";
	}

	@Override
	public String toString() {
		return operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}

	public boolean isSecure() {
		return isSecure;
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

	public void setAuthorisation(Authorization authorization) {
		this.authorisation = authorization;
	}

	public Authorization getAuthorisation() {
		return authorisation;
	}

	public void setGSICredentials(GSSCredential gsi_credentials) {
		this.gsi_credential = gsi_credentials;
	}

	public void setProxy(GlobusCredential proxy) {
		this.proxy = proxy;
	}

	public GlobusCredential getProxy() {
		return proxy;
	}

	public GSSCredential getGSICredentials() {
		return gsi_credential;
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

	public void setDefaultAuthNServiceURL(String authNServiceURL) {
		this.authNServiceURL = authNServiceURL;
	}

	public String getAuthNServiceURL() {
		return authNServiceURL;
	}

	public void setDefaultDorianServiceURL(String dorianServiceURL) {
		this.dorianServiceURL = dorianServiceURL;
	}

	public String getDorianServiceURL() {
		return dorianServiceURL;
	}

	public Icon getIcon() {
		return new ImageIcon(CaGridActivityItem.class.getResource("/cagrid.png"));
	}
	
	public Object getConfigBean() {
		CaGridActivityConfigurationBean bean = new CaGridActivityConfigurationBean();
		
		bean.setWsdl(getUrl());
		bean.setOperation(getOperation());
		
		bean.setIndexServiceURL(getIndexServiceURL());
		// Do not save AuthN and Dorian Servoce urls here- they will be read from properties file
		//bean.setAuthNServiceURL(getAuthNServiceURL());
		//bean.setDorianServiceURL(getDorianServiceURL());
				
		return bean;
	}
	
	public Activity<?> getUnconfiguredActivity() {
		return new CaGridActivity();
	}

}
