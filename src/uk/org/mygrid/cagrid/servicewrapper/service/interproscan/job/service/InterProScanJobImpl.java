package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service;

import java.math.BigInteger;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.jdom.Document;

import uk.org.mygrid.cagrid.domain.common.JobStatus;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.Protein;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker.InvokerFactory;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanJobImpl extends InterProScanJobImplBase {

	private static Logger logger = Logger.getLogger(InterProScanJobImpl.class);

	private InterProScanInvoker invoker = InvokerFactory.getInvoker();

	public InterProScanJobImpl() throws RemoteException {
		super();
	}

	public uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput getInputs()
			throws RemoteException {
		try {
			return getResourceHome().getAddressedResource()
					.getInterProScanInput();
		} catch (Exception e) {
			logger.warn("Could not find input", e);
			throw new RemoteException("Could not find input", e);
		}
	}

	public uk.org.mygrid.cagrid.domain.common.JobStatus getStatus()
			throws RemoteException {
		String jobID;
		try {
			jobID = getResourceHome().getAddressedResource().getJobID();
		} catch (Exception e) {
			logger.warn("Unknown jobID", e);
			throw new RemoteException("Unknown jobID", e);
		}
		String status;
		try {
			status = invoker.checkStatus(jobID);
		} catch (InvokerException e) {
			logger.warn("Could not check status for " + jobID, e);
			throw new RemoteException("Could not check status for " + jobID, e);
		}
		logger.info("Status for " + jobID + " is " + status);
		JobStatus jobStatus;
		try {
			jobStatus = JobStatus.fromValue(status.toLowerCase());
		} catch (IllegalArgumentException ex) {
			logger.warn("Unknown status type for " + jobID  + ": " + status, ex);
			throw new RemoteException("Unknown status type " + status);		
		}
		try {
			getResourceHome().getAddressedResource().setJobStatus(jobStatus);
		} catch (Exception ex) {
			logger.warn("Can't set job status for " + jobID  + " to " + jobStatus, ex);
		}
		return jobStatus;
	}

	public uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput getOutputs()
			throws RemoteException {
		try {
			InterProScanOutput interProScanOutput = getResourceHome().getAddressedResource()
					.getInterProScanOutput();
			if (interProScanOutput == null && getStatus().equals(JobStatus.done)) {
				return fetchOutputs();
			}
			return interProScanOutput;
		} catch (Exception e) {
			logger.warn("Could not find output", e);
			throw new RemoteException("Could not find output", e);
		}
	}

	protected InterProScanOutput fetchOutputs() throws RemoteException {
		
		String jobID;
		try {
			jobID = getResourceHome().getAddressedResource().getJobID();
		} catch (Exception e) {
			logger.warn("Unknown jobID", e);
			throw new RemoteException("Unknown jobID", e);
		}
		Document data;
		try {
			data = invoker.poll(jobID);
		} catch (InvokerException e) {
			logger.warn("Can't poll for jobID " + jobID, e);
			throw new RemoteException("Can't poll for jobID " + jobID, e);
		}
		logger.info("Data returned for " + jobID + " is: \n" + data);
		
		InterProScanOutput output = convertInterProScanOutput(data);
		return output;
		
	}

	protected InterProScanOutput convertInterProScanOutput(Document data) {
		InterProScanOutput output = new InterProScanOutput();
		output.setProtein(new Protein("asdasd", "asdasd", BigInteger.valueOf(123)));
		return output;
	}

	public gov.nih.nci.cagrid.metadata.service.Fault getError()
			throws RemoteException {
		try {
			return getResourceHome().getAddressedResource().getFault();
		} catch (Exception e) {
			logger.warn("Could not find error", e);
			throw new RemoteException("Could not find error", e);
		}
	}

}
