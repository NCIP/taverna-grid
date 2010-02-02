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
package net.sf.taverna.cagrid.activity.config;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * Configuration for the caGrid activity.
 * 
 * @author Alex Nenadic
 */
public class CaGridConfiguration extends AbstractConfigurable {
	
	public static String uuid = "1df75ad0-491e-11de-8a39-0800200c9a66";
	
	// Default values
	public static String PRODUCTION_CAGRID_NAME =  "NCI Production CaGrid 1.3";
	public static String PRODUCTION_INDEX_SERVICE_URL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	public static String PRODUCTION_AUTHN_SERVICE_URL = "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian";
	public static String PRODUCTION_DORIAN_SERVICE_URL = "https://cagrid-dorian.nci.nih.gov:8443/wsrf/services/cagrid/Dorian";
	public static String PRODUCTION_PROXY_LIFETIME = "12"; // 12 hours is default and maximum	
	public static String PRODUCTION_CDS_SERVICE_URL = "https://cagrid-cds.nci.nih.gov:8443/wsrf/services/cagrid/CredentialDelegationService";
	
	public static String TRAINING_CAGRID_NAME =  "Training CaGrid 1.3";
	public static String TRAINING_INDEX_SERVICE_URL = "http://index.training.cagrid.org:8080/wsrf/services/DefaultIndexService";
	public static String TRAINING_AUTHN_SERVICE_URL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";
	public static String TRAINING_DORIAN_SERVICE_URL = "https://dorian.training.cagrid.org:8443/wsrf/services/cagrid/Dorian";
	public static String TRAINING_PROXY_LIFETIME = "12"; // 12 hours is default and maximum
	public static String TRAINING_CDS_SERVICE_URL = "https://cds.training.cagrid.org:8443/wsrf/services/cagrid/CredentialDelegationService";
	private Map<String, String> defaultPropertyMap;

	private static CaGridConfiguration instance;

	private static Logger logger = Logger.getLogger(CaGridConfiguration.class);
	
	public static CaGridConfiguration getInstance() {
		if (instance == null) {
			instance = new CaGridConfiguration();
		}
		return instance;
	}

	private CaGridConfiguration() {
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
							+ PRODUCTION_DORIAN_SERVICE_URL + ","
							+ PRODUCTION_PROXY_LIFETIME + ","
							+ PRODUCTION_CDS_SERVICE_URL);

			defaultPropertyMap.put(TRAINING_CAGRID_NAME,
					TRAINING_INDEX_SERVICE_URL + ","
							+ TRAINING_AUTHN_SERVICE_URL + ","
							+ TRAINING_DORIAN_SERVICE_URL + ","
							+ TRAINING_PROXY_LIFETIME + ","
							+ TRAINING_CDS_SERVICE_URL);	
		}			
	}

	public String getName() {
		return "CaGrid";
	}

	public String getUUID() {
		return uuid;
	}
	
	// Copied from AbstractConfigurable as could not update AbstractConfigurable due to
	// code freeze and needed this for cagrid plugin
	public static List<String> fromListText(String property) {
		List<String> result = new ArrayList<String>();
		if (property.length()>0) { //an empty string as assumed to be an empty list, rather than a list with 1 empty string in it!
			StringReader reader = new StringReader(property);
			CSVReader csvReader = new CSVReader(reader);
			try {
				for (String v : csvReader.readNext()) {
					result.add(v);
				}
			} catch (IOException e) {
				logger .error("Exception occurred parsing CSV properties:"+property,e);
			}
		}
		return result;
	}
	
	// Copied from AbstractConfigurable as could not update AbstractConfigurable due to
	// code freeze and needed this for cagrid plugin
	public static String toListText(List<String> values) {
		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer);
		csvWriter.writeNext(values.toArray(new String[]{}));
		return writer.getBuffer().toString().trim();
	}

	public String getDisplayName() {
		// TODO Auto-generated method stub
		return new String("CaGrid Configuration");
	}

	public String getFilePrefix() {
		// TODO Auto-generated method stub
		return new String("CaGrid Configuration");
	}

}
