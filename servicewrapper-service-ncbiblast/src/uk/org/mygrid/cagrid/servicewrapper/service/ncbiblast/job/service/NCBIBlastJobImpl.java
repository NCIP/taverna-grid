package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.service;

import java.rmi.RemoteException;

import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker.NCBIBlastJobUtils;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.service.globus.resource.NCBIBlastJobResource;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class NCBIBlastJobImpl extends NCBIBlastJobImplBase {

	NCBIBlastJobUtils jobUtils = new NCBIBlastJobUtils();

	public NCBIBlastJobImpl() throws RemoteException {
		super();
	}

	public gov.nih.nci.cagrid.metadata.service.Fault getError()
			throws RemoteException {
		NCBIBlastJobResource job = getJob();
		jobUtils.updateFault(job);
		return getJob().getFault();
	}

	public uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput getInputs()
			throws RemoteException {
		return getJob().getNCBIBlastInput();
	}

	public uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput getOutputs()
			throws RemoteException {
		NCBIBlastJobResource job = getJob();
		jobUtils.updateOutputs(job);
		return job.getNCBIBlastOutput();
	}

	public uk.org.mygrid.cagrid.domain.common.JobStatus getStatus()
			throws RemoteException {
		NCBIBlastJobResource job = getJob();
		jobUtils.updateStatus(job);
		return job.getJobStatus();
	}

	protected NCBIBlastJobResource getJob() throws RemoteException {
		try {
			return getResourceHome().getAddressedResource();
		} catch (Exception e) {
			throw new RemoteException("Could not find job", e);
		}
	}

}
