/*********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.cagrid.ui.servicedescriptions;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.cagrid.activity.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CaGridServiceDescription extends
		ServiceDescription<CaGridActivityConfigurationBean> {

	private String use;
	private URI uri;
	private String style;
	private String operation;
	private String researchCenter;
	
	// Has this service been discovered as part of another 
	// caGrid service (i.e. is this a helper service used to query job resource?)
	private boolean isHelperService = false;
	private String helperServiceName;
	// For helper services - we need the URI of the original service
	// this was the helper service for in order to render the string that
	// will appear in the Service Panel
	private URI masterURI;

	// Name of the caGrid the service belongs to
	private String caGridName;
	// Index Service URL identifies the caGrid the service belongs to
	private String indexServiceURL;

	public Class<? extends Activity<CaGridActivityConfigurationBean>> getActivityClass() {
		return CaGridActivity.class;
	}

	public CaGridActivityConfigurationBean getActivityConfiguration() {
		CaGridActivityConfigurationBean bean = new CaGridActivityConfigurationBean();
		bean.setWsdl(getURI().toASCIIString());
		bean.setOperation(getOperation());
		bean.setStyle(style);
		
		// Set the CaGrid this service/operation belongs to so we
		// can fetch various other properties for this CaGrid from the preferences,
		// such as Index Service,  AuthN and Dorian services, etc. 
		bean.setCaGridName(caGridName);
		
		return bean;
	}
	
	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public URI getURI() {
		return uri;
	}

	public void setURI(URI url) {
		this.uri = url;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getType() {
		return "caGrid";
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

	public String getResearchCenter() {
		return researchCenter;
	}

	public void setResearchCenter(String rc) {
		this.researchCenter = rc;
	}

	public void setCaGridName(String caGridName) {
		this.caGridName = caGridName;
	}

	public String getCaGridName() {
		return caGridName;
	}
	
	public String getIndexServiceURL() {
		return indexServiceURL;
	}

	public void setIndexServiceURL(String indexServiceURL) {
		this.indexServiceURL = indexServiceURL;
	}

	public void setHelperService(boolean isHelperService) {
		this.isHelperService = isHelperService;
	}

	public boolean isHelperService() {
		return isHelperService;
	}

	public void setHelperServiceName(String helperServiceName) {
		this.helperServiceName = helperServiceName;
	}

	public String getHelperServiceName() {
		return helperServiceName;
	}
	
	public void setMasterURI(URI masterURI) {
		this.masterURI = masterURI;		
	}
	
	public URI getMasterURI() {
		return masterURI;
	}
	
	public Icon getIcon() {
		return CaGridActivityIcon.cagridIcon;
	}

	public String getName() {
		return getOperation();
	}
	
	@SuppressWarnings("unchecked")
	public List<? extends Comparable> getPath() {
		if ((researchCenter!= null) && (!researchCenter.equals(""))){
			if (isHelperService){
				return Arrays.asList(caGridName, researchCenter, "WSDL @ "+ getMasterURI(), helperServiceName);
			}
			else{
				return Arrays.asList(caGridName, researchCenter, "WSDL @ "+ getURI());
			}
		}
		else{
			if (isHelperService){
				return Arrays.asList(caGridName, "WSDL @ " + getMasterURI(), helperServiceName);
			}
			else{
				return Arrays.asList(caGridName, "WSDL @ " + getURI());
			}
		}

	}

	public boolean isTemplateService() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CaGridServiceDescription)) {
			return false;
		}
		CaGridServiceDescription other = (CaGridServiceDescription) obj;
		return getIdentifyingData().equals(other.getIdentifyingData());
	}

	protected List<Object> getIdentifyingData() {
		return Arrays.<Object> asList(getURI(), getOperation());
	}

}
