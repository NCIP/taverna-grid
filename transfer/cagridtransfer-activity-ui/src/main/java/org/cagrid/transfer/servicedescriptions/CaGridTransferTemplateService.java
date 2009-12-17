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
package org.cagrid.transfer.servicedescriptions;

import javax.swing.Icon;

import org.cagrid.transfer.*;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

public class CaGridTransferTemplateService extends AbstractTemplateService<CaGridTransferConfigurationBean>{
	
	private static final String NAME = "CaGrid Transfer Activity";

	@Override
	public Class<CaGridTransferActivity> getActivityClass() {
		return CaGridTransferActivity.class;
	}

	@Override
	public CaGridTransferConfigurationBean getActivityConfiguration() {
		CaGridTransferConfigurationBean transferConfigurationBean = new CaGridTransferConfigurationBean();
		//stringConstantConfigurationBean.setValue("Add your own value here");
		return transferConfigurationBean;
	}

	@Override
	public Icon getIcon() {
		return CaGridTransferActivityIcon.getCaGridTransferIcon();
	}

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return "file up/download using caGrid transfer.";
	}
	
	public static ServiceDescription getServiceDescription() {
		CaGridTransferTemplateService scts = new CaGridTransferTemplateService();
		return scts.templateService;
	}
}
