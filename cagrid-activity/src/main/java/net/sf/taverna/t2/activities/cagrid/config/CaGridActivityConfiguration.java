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
package net.sf.taverna.t2.activities.cagrid.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * Configuration for the caGrid activity.
 * 
 * @author Alex Nenadic
 */
public class CaGridActivityConfiguration extends AbstractConfigurable {

	public static String PRODUCTION_CAGRID_NAME =  "Production CaGrid";
	public static String PRODUCTION_INDEX_SERVICE_URL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	public static String PRODUCTION_AUTHN_SERVICE_URL = "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian";
	public static String PRODUCTION_DORIAN_SERVICE_URL = "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian";
	
	public static String TRAINING_CAGRID_NAME =  "Training CaGrid";
	public static String TRAINING_INDEX_SERVICE_URL = "http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService";
	public static String TRAINING_AUTHN_SERVICE_URL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";
	public static String TRAINING_DORIAN_SERVICE_URL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";

	private Map<String, String> defaultPropertyMap;

	private static CaGridActivityConfiguration instance;
	
	public static CaGridActivityConfiguration getInstance() {
		if (instance == null) {
			instance = new CaGridActivityConfiguration();
		}
		return instance;
	}

	private CaGridActivityConfiguration() {
		super();
		initaliseDefaults();
	}

	public String getCategory() {
		return "cagrid";
	}

	public Map<String, String> getDefaultPropertyMap() {
		initaliseDefaults();
		return defaultPropertyMap;
	}

	private void initaliseDefaults() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			
			defaultPropertyMap.put(PRODUCTION_CAGRID_NAME,
					PRODUCTION_INDEX_SERVICE_URL + ","
							+ PRODUCTION_AUTHN_SERVICE_URL + ","
							+ PRODUCTION_DORIAN_SERVICE_URL);

			defaultPropertyMap.put(TRAINING_CAGRID_NAME,
					TRAINING_INDEX_SERVICE_URL + ","
							+ TRAINING_AUTHN_SERVICE_URL + ","
							+ TRAINING_DORIAN_SERVICE_URL);	
		}			
	}

	public String getName() {
		return "CaGrid";
	}

	public String getUUID() {
		return "1df75ad0-491e-11de-8a39-0800200c9a66";
	}

}
