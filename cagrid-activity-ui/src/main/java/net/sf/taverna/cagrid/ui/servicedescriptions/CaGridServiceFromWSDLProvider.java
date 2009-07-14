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

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;

import org.apache.log4j.Logger;

public class CaGridServiceFromWSDLProvider extends
		AbstractConfigurableServiceProvider<CaGridServiceFromWSDLProviderConfig>
		implements
		CustomizedConfigurePanelProvider<CaGridServiceFromWSDLProviderConfig> {

	private static final String CAGRID_SERVICE_FROM_WSDL = "caGrid service from WSDL";
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CaGridServiceFromWSDLProvider.class);

	public CaGridServiceFromWSDLProvider() {
		super(new CaGridServiceFromWSDLProviderConfig());
	}

	public String getName() {
		return CAGRID_SERVICE_FROM_WSDL;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		
		try {
			CaGridServiceFromWSDLSearcher searcher = new CaGridServiceFromWSDLSearcher(
					getConfiguration().getWsdlURL(),
					getConfiguration().getCaGridName(),
					getConfiguration().getIndexServiceURL());
			searcher.findServiceDescriptionsAsync(callBack);
			
		} catch (Exception ex) {
			callBack.fail("Could not add the caGrid service "
					+ getConfiguration().getIndexServiceURL(), ex);
			ex.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration().getWsdlURL();
	}

	public Icon getIcon() {
		return CaGridActivityIcon.cagridIcon;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		ArrayList<String> identifying = new ArrayList<String>();
		identifying.add(getConfiguration().getIndexServiceURL());
		identifying.add(getConfiguration().getWsdlURL());
		return identifying;
	}
	
	@SuppressWarnings("serial")
	public void createCustomizedConfigurePanel(
			final CustomizedConfigureCallBack<CaGridServiceFromWSDLProviderConfig> callBack) {
		
		CaGridServiceFromWSDLDialog caGridServiceFromWSDLDialogue = new CaGridServiceFromWSDLDialog() {
			@Override
			protected void addRegistry(String wsdlURL, 
					String caGridName,
					String indexServiceURL) {
				
				CaGridServiceFromWSDLProviderConfig providerConfig = new CaGridServiceFromWSDLProviderConfig(
						wsdlURL,
						caGridName,
						indexServiceURL);
				
				callBack.newProviderConfiguration(providerConfig);
			}
		};
		caGridServiceFromWSDLDialogue.setVisible(true);
	}


}
