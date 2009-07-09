package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource;

import org.globus.wsrf.ResourceProperty;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.common.InterProScanJobConstants;


/** 
 * The implementation of this InterProScanJobResource type.
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanJobResource extends InterProScanJobResourceBase {


	public String getJobID() {
		ResourceProperty prop = getResourcePropertySet().get(InterProScanJobConstants.JOB_ID);
		return (String) prop.get(0);
	}

	public void setJobID(String jobID) {
		ResourceProperty prop = getResourcePropertySet().get(InterProScanJobConstants.JOB_ID);
		prop.set(0, jobID);
	}
	
}
