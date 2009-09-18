package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.globus.wsrf.ResourceKey;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.domain.common.JobId;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter.InterProScanConverter;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker.InvokerFactory;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResourceHome;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

/**
 * InterProScan service implementation
 * <p>
 * Submits the job, puts the job ID in the job resource and return an endpoint
 * reference to the new job.
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanImpl extends InterProScanImplBase {

	private static final int DEFAULT_TERMINATION_DAYS = 14;

	static Logger logger = Logger.getLogger(InterProScanImpl.class);

	private InterProScanInvoker invoker = InvokerFactory.getInvoker();
	
	private InterProScanConverter converter = new InterProScanConverter();

	public InterProScanImpl() throws RemoteException {
		super();
	}

  public uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference interProScan(uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput interProScanInput) throws RemoteException {

		InterProScanInput input = converter.convertInterProScanInput(interProScanInput);
		
		final InterProScanJobResource resource;
		InterProScanJobReference jobResourceRef;
		try {
			InterProScanJobResourceHome resourceHome = getInterProScanJobResourceHome();
			ResourceKey resourceKey = resourceHome.createResource();
			resource = resourceHome.getResource(resourceKey);
			resource.setInterProScanInput(interProScanInput);
			jobResourceRef = resourceHome.getResourceReference(resourceKey);
			Calendar terminationTime = Calendar.getInstance();
			terminationTime.add(Calendar.DAY_OF_YEAR, DEFAULT_TERMINATION_DAYS);
			resource.setTerminationTime(terminationTime);
		} catch (Exception e) {
			logger.error("Could not create job resource", e);
			throw new RemoteException("Could not create job resource", e);
		}

		String jobID;
		try {
			jobID = invoker.runJob(input);
		} catch (InvokerException e1) {
			logger.warn("Can't submit job", e1);
			throw new RemoteException("Can't submit job", e1);
		}
		resource.setJobId(new JobId(jobID));
		logger.info("Submitted interproscan job " + jobID);
		return jobResourceRef;
	}

	

}
