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
package org.cagrid.cql;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;


/**
 * Configuration bean for setting up a StringConstantActivity.<br>
 * The only thing to be configured is the string value, since the ports are fixed.
 * 
 * @author Stuart Owen
 * @see CQLActivity
 */
public class CQLConfigurationBean extends ActivityPortsDefinitionBean {

    private String value;//stored cql query
    private boolean isQueryManual;
    private String servicesUrl;
    private int numberOfInput;

    public String getValue() {
        return value;
    }

    public void setValue(String cqlQuery) {
        this.value = cqlQuery;
    }

    public boolean isIsQueryManual() {
        return isQueryManual;
    }

    public void setIsQueryManual(boolean isQueryManual) {
        this.isQueryManual = isQueryManual;
    }

    public String getServicesUrl() {
        return servicesUrl;
    }

    public void setServicesUrl(String servicesUrl) {
        this.servicesUrl = servicesUrl;
    }
     public int getNumberOfInput() {
        return numberOfInput;
    }

    public void setNumberOfInput(int numberOfInput) {
        this.numberOfInput = numberOfInput;
    }
    
    //private SavedQueryWindowState savedQuery;
}
