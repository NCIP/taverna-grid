package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker.InterProScanJobUtils;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanJobImpl extends InterProScanJobImplBase {

	private InterProScanJobUtils jobUtils = new InterProScanJobUtils();

	public InterProScanJobImpl() throws RemoteException {
		super();
	}
	
  public gov.nih.nci.cagrid.metadata.service.Fault getError() throws RemoteException {
		InterProScanJobResource job = getJob();
		jobUtils.updateFault(job);
		return getJob().getFault();
	}

  public uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput getInputs() throws RemoteException {
		return getJob().getInterProScanInput();
	}
	
  public uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput getOutputs() throws RemoteException {
		InterProScanJobResource job = getJob();
		jobUtils.updateOutputs(job);
		return job.getInterProScanOutput();	
	}

  public uk.org.mygrid.cagrid.domain.common.JobStatus getStatus() throws RemoteException {
		InterProScanJobResource job = getJob();
		jobUtils.updateStatus(job);
		return job.getJobStatus();
	}
	
	protected InterProScanJobResource getJob() throws RemoteException {
		try {
			return getResourceHome().getAddressedResource();
		} catch (Exception e) {
			throw new RemoteException("Could not find job", e);
		}
	}

}
