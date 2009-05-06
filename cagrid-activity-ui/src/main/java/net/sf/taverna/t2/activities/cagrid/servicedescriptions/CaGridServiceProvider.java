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

import javax.swing.Icon;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.cagrid.query.CaGridServiceSearcher;
import net.sf.taverna.t2.activities.cagrid.query.ServiceQuery;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;

//import org.apache.log4j.Logger;

/**
 * 
 * @author Alex Nenadic
 *
 */
public class CaGridServiceProvider extends
		AbstractConfigurableServiceProvider<CaGridServiceProviderConfig>
		implements
		CustomizedConfigurePanelProvider<CaGridServiceProviderConfig> {

	//private static Logger logger = Logger.getLogger(CaGridServiceProvider.class);

	private static final String CAGRID_SERVICE = "caGrid service";

	public static final Icon cagridIcon = new ImageIcon(
			CaGridServiceDescription.class.getResource("/cagrid.png"));

	public CaGridServiceProvider() {
		super(new CaGridServiceProviderConfig());
	}

	public String getName() {
		return CAGRID_SERVICE;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
	
		try {
			CaGridServiceSearcher searcher = new CaGridServiceSearcher(
					getConfiguration().getIndexServiceURL(),
					getConfiguration().getServiceQueryList(), 
					getConfiguration().getDefaultAuthNServiceURL(),
					getConfiguration().getDefaultDorianServiceURL());
			searcher.findServiceDescriptionsAsync(callBack);
			
		} catch (Exception ex) {
			callBack.fail("Could not fetch caGrid services from the Index Service: "
					+ getConfiguration().getIndexServiceURL(), ex);
			ex.printStackTrace();
		}

	}

	@Override
	public String toString() {
		return getName();
	}

	public Icon getIcon() {
		return cagridIcon;
	}

	@SuppressWarnings("serial")
	public void createCustomizedConfigurePanel(
			final CustomizedConfigureCallBack<CaGridServiceProviderConfig> callBack) {
		
		CaGridServicesSearchDialog caGridServicesQueryDialogue = new CaGridServicesSearchDialog() {
			@Override
			protected void addRegistry(String indexServiceURL,
					ServiceQuery[] serviceQueryList, String authNServiceURL,
					String dorianServiceURL) {
				
				CaGridServiceProviderConfig providerConfig = new CaGridServiceProviderConfig(
						indexServiceURL, serviceQueryList, authNServiceURL,
						dorianServiceURL);
				
				callBack.newProviderConfiguration(providerConfig);
			}
		};
		caGridServicesQueryDialogue.setVisible(true);
	}

}
