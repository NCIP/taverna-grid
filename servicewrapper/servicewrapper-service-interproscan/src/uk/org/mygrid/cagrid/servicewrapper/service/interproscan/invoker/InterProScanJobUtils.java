package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.jdom.Document;

import uk.org.mygrid.cagrid.domain.common.JobStatus;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter.InterProScanConverter;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;

public class InterProScanJobUtils {

	private static Logger logger = Logger.getLogger(InterProScanJobUtils.class);

	private InterProScanInvoker invoker = InvokerFactory.getInvoker();

	private InterProScanConverter converter = new InterProScanConverter();

	public void updateStatus(InterProScanJobResource job)
			throws RemoteException {
		if (isFinished(job) && job.getInterProScanOutput() != null
				|| job.getFault() != null) {
			// No need to check status again, and the return data has been
			// fetched
			return;
		}
		String jobID = job.getJobID();
		if (jobID == null || jobID.equals("")) {
			// Too early, no job id set yet
			return;
		}

		String status;
		try {
			status = invoker.checkStatus(jobID);
		} catch (InvokerException e) {
			logger.warn("Could not check status for " + jobID, e);
			job.setFault(new Fault("Could not check status for " + jobID,
					"Can't check status"));
			throw new RemoteException("Could not check status for " + jobID, e);
		}
		logger.info("Status for " + jobID + " is " + status);
		JobStatus jobStatus;
		try {
			jobStatus = JobStatus.fromValue(status.toLowerCase());
		} catch (IllegalArgumentException ex) {
			job.setFault(new Fault("Unknown status type for " + jobID + ": "
					+ status, "Unknown status"));
			logger.warn("Unknown status type for " + jobID + ": " + status, ex);
			throw new RemoteException("Unknown status type " + status);
		}
		job.setJobStatus(jobStatus);
	}

	public void updateOutputs(InterProScanJobResource job)
			throws RemoteException {
		if (!job.getJobStatus().equals(JobStatus.done)
				|| job.getInterProScanOutput() != null) {
			// Too early/late
			return;
		}
		String jobID = job.getJobID();
		if (jobID == null || jobID.equals("")) {
			// Too early, no job id set yet
			return;
		}

		Document data;
		try {
			data = invoker.poll(jobID);
		} catch (InvokerException e) {
			job.setFault(new Fault("Can't poll for job ID " + jobID,
					"Can't poll"));
			logger.warn("Can't poll for jobID " + jobID, e);
			throw new RemoteException("Can't poll for jobID " + jobID, e);
		}
		logger.info("Data returned for " + jobID + " is: \n" + data);
		InterProScanOutput output = converter.convertInterProScanOutput(data);
		job.setInterProScanOutput(output);
	}

	public boolean isFinished(InterProScanJobResource job) {
		JobStatus jobStatus = job.getJobStatus();
		if (jobStatus == null) {
			return false;
		}
		if (jobStatus.equals(JobStatus.error)
				|| jobStatus.equals(JobStatus.not_found)) {
			return true;
		}
		if (jobStatus.equals(JobStatus.done)
				&& job.getInterProScanOutput() != null) {
			return true;
		}
		return false;
	}

	public void updateFault(InterProScanJobResource job) throws RemoteException {
		if (job.getInterProScanOutput() != null) {
			// No fault anymore
			job.setFault(null);
		}
	}

}
