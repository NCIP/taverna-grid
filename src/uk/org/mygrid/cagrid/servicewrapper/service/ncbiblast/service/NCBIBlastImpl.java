package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.wsrf.ResourceKey;

import uk.org.mygrid.cagrid.domain.common.SequenceDatabase;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter.NCBIBlastConverter;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker.InvokerFactory;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.service.globus.resource.NCBIBlastJobResource;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.service.globus.resource.NCBIBlastJobResourceHome;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.stubs.types.NCBIBlastJobReference;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvoker;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class NCBIBlastImpl extends NCBIBlastImplBase {

	private static final int DEFAULT_TERMINATION_DAYS = 14;

	private static Logger logger = Logger.getLogger(NCBIBlastImpl.class);

	private NCBIBlastConverter converter = new NCBIBlastConverter();
	private NCBIBlastInvoker invoker = InvokerFactory.getInvoker();

	public NCBIBlastImpl() throws RemoteException {
		super();
	}

  public uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.stubs.types.NCBIBlastJobReference ncbiBlast(uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput nCBIBlastInput) throws RemoteException {
		NCBIBlastInput input = converter.convertNCBIBlastInput(nCBIBlastInput);

		final NCBIBlastJobResource resource;
		NCBIBlastJobReference jobResourceRef;
		try {
			NCBIBlastJobResourceHome resourceHome = getNCBIBlastJobResourceHome();
			ResourceKey resourceKey = resourceHome.createResource();
			resource = resourceHome.getResource(resourceKey);
			resource.setNCBIBlastInput(nCBIBlastInput);
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
		logger.info("Submitted interproscan job " + jobID);
		resource.setJobID(jobID);
		resource.store();
		return jobResourceRef;
	}

  public uk.org.mygrid.cagrid.domain.common.SequenceDatabase[] getDatabases() throws RemoteException {
		List<uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.SequenceDatabase> sequenceDBs;
		try {
			sequenceDBs = invoker.getDatabases();
		} catch (InvokerException e) {
			throw new RemoteException("Can't get databases from EBI", e);
		}
		List<SequenceDatabase> databases = converter.convertDatabases(sequenceDBs);
		return databases.toArray(new SequenceDatabase[databases.size()]);
	}

}
