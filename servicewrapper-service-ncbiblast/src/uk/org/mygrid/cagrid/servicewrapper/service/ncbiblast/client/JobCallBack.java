/**
 * 
 */
package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client;

import gov.nih.nci.cagrid.metadata.service.Fault;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;

public interface JobCallBack<JobOutputType> {
	public void jobStatusChanged(JobStatus oldStatus, JobStatus newStatus);
	
	public void jobOutputReceived(JobOutputType jobOutput);

	public void jobError(Fault fault);
	
}