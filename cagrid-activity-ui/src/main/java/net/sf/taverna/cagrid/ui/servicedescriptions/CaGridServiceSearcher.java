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

//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;

import net.sf.taverna.cagrid.activity.CaGridActivity;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;

import org.apache.log4j.Logger;

public class CaGridServiceSearcher {

	private static Logger logger = Logger.getLogger(CaGridServiceSearcher.class);

	private String caGridName;
	private String indexServiceURL;
	private CaGridServiceQuery[] serviceQueryList;
	
	static {
		CaGridActivity.initializeSecurity();
	}
	
	public CaGridServiceSearcher(String caGridName, String indexServiceURL,
			CaGridServiceQuery[] serviceQueryList)
			throws Exception {
		
		this.caGridName = caGridName;
		this.indexServiceURL = indexServiceURL;
		this.serviceQueryList = serviceQueryList;
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {

	        try {
	    		logger.info("Starting caGrid service search for " + caGridName + " using Index Service: " + indexServiceURL);
				CaGridServiceQueryUtility.loadServices(caGridName, indexServiceURL, serviceQueryList, callBack);
	        }
			catch (Exception ex) {
				ex.printStackTrace();
				callBack.fail("An error occured when contacting the Index Service - could not load caGrid services.", ex);
				return;
	        }
		}
}

