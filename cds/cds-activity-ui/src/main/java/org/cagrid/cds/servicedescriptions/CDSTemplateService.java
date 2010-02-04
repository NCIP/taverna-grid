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
package org.cagrid.cds.servicedescriptions;

import javax.swing.Icon;

import org.cagrid.cds.*;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

public class CDSTemplateService extends AbstractTemplateService<CDSConfigurationBean>{
	
	private static final String NAME = "CDS Activity";

	@Override
	public Class<CDSActivity> getActivityClass() {
		return CDSActivity.class;
	}

	@Override
	public CDSConfigurationBean getActivityConfiguration() {
		CDSConfigurationBean transferConfigurationBean = new CDSConfigurationBean();
		//stringConstantConfigurationBean.setValue("Add your own value here");
		return transferConfigurationBean;
	}

	@Override
	public Icon getIcon() {
		return CDSActivityIcon.getCDSIcon();
	}

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return "credential delegation.";
	}
	
	public static ServiceDescription getServiceDescription() {
		CDSTemplateService scts = new CDSTemplateService();
		return scts.templateService;
	}
}
