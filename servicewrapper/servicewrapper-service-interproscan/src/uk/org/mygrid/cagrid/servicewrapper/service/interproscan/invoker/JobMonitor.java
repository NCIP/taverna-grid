/**
 * 
 */
package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResourceHome;

public class JobMonitor extends Thread {

	// Default period between checking a job
	private static final int DEFAULT_CHECK_PERIOD_SECONDS = 30;

	// Check again on failure
	private static final int FAILURE_CHECK_PERIOD_SECONDS = 15;

	// Delay between each check-status-call to server
	private static final long KIND_TO_SERVER_DELAY_MS = 3000;

	private static Logger logger = Logger.getLogger(JobMonitor.class);

	protected static Map<InterProScanJobResourceHome, JobMonitor> monitors = new HashMap<InterProScanJobResourceHome, JobMonitor>();

	public static void monitorJobs(InterProScanJobResourceHome jobResourceHome) {
		JobMonitor monitorThread = monitors.get(jobResourceHome);
		if (monitorThread == null || !monitorThread.isAlive()) {
			synchronized (monitors) {
				monitorThread = monitors.get(jobResourceHome);
				if (monitorThread == null) {
					monitorThread = new JobMonitor(jobResourceHome);
					monitorThread.setDaemon(true);
					monitors.put(jobResourceHome, monitorThread);
				}
				if (!monitorThread.isAlive()) {
					monitorThread.start();
				}
			}
		}
	}

	private final InterProScanJobResourceHome jobResourceHome;

	private InterProScanJobUtils jobUtils = new InterProScanJobUtils();

	private Map<ResourceKey, Calendar> nextChecks = new HashMap<ResourceKey, Calendar>();

	public JobMonitor(InterProScanJobResourceHome interProScanJobResourceHome) {
		super("Job monitor for " + interProScanJobResourceHome);
		this.jobResourceHome = interProScanJobResourceHome;
	}

	private Set<ResourceKey> finishedJobs = new HashSet<ResourceKey>();
	
	@Override
	public void run() {
		while (true) {
			for (ResourceKey jobKey : jobResourceHome.getResourceKeys()) {
				if (Thread.interrupted()) {
					// OK, let's kill our thread then..
					return;
				}
				if (finishedJobs.contains(jobKey)) {
					continue;
				}
				
				Calendar nextCheck = nextChecks.get(jobKey);
				if (nextCheck != null
						&& nextCheck.after(Calendar.getInstance())) {
					// Don't need to check this job yet
					continue;
				}
				
				InterProScanJobResource job;
				try {
					job = jobResourceHome.getResource(jobKey);
				} catch (ResourceException e) {
					logger.warn("Could not look up job resource for " + jobKey);
					continue;
				}
				if (job.getJobID() == null) {
					logger.info("Ignoring job with no jobID yet: " + jobKey);
					continue;
				}
				
				if (jobUtils.isFinished(job)) {
					// No point in updating it ever again
					finishedJobs.add(jobKey);
					continue;
				}

				nextCheck = Calendar.getInstance();
				try {
					checkStatus(job);
					nextCheck.add(Calendar.SECOND, DEFAULT_CHECK_PERIOD_SECONDS);
				} catch (RuntimeException ex) {
					nextCheck.add(Calendar.SECOND, FAILURE_CHECK_PERIOD_SECONDS);
				} catch (RemoteException e) {
					nextCheck.add(Calendar.SECOND, FAILURE_CHECK_PERIOD_SECONDS);
				}
				nextChecks.put(jobKey, nextCheck);
				try {
					Thread.sleep(KIND_TO_SERVER_DELAY_MS);
				} catch (InterruptedException e) {
					// OK, let's kill our thread then..
					return;
				}
			}
			// An extra sleep in case there were no jobs
			try {
				Thread.sleep(KIND_TO_SERVER_DELAY_MS);
			} catch (InterruptedException e) {
				// OK, let's kill our thread then..
				return;
			}
		}
	}

	protected void checkStatus(InterProScanJobResource job)
			throws RemoteException {
		logger.info("Checking status for " + job.getJobID());
		jobUtils.updateStatus(job);
		jobUtils.updateOutputs(job);
		jobUtils.updateFault(job);
	}
}