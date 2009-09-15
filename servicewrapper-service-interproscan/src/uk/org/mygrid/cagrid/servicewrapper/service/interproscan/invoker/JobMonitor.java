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

import uk.org.mygrid.cagrid.valuedomains.JobStatus;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResourceHome;

/**
 * Monitor jobs by periodically checking their status, and if done, fetch their
 * result. This will update the job's resource properties.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class JobMonitor implements Runnable {

	/**
	 * Default period between checking a job
	 */
	private static final int DEFAULT_CHECK_PERIOD_SECONDS = 30;

	/**
	 * Check again on failure
	 */
	private static final int FAILURE_CHECK_PERIOD_SECONDS = 15;

	/**
	 * Delay between each check-status-call to server
	 */
	private static final long KIND_TO_SERVER_DELAY_MS = 3000;

	private static Logger logger = Logger.getLogger(JobMonitor.class);

	protected static Map<InterProScanJobResourceHome, JobMonitor> monitors = new HashMap<InterProScanJobResourceHome, JobMonitor>();

	public static JobMonitor getMonitorFor(
			InterProScanJobResourceHome jobResourceHome) {
		JobMonitor jobMonitor = monitors.get(jobResourceHome);
		if (jobMonitor == null) {
			synchronized (monitors) {
				// Check again now that we are synchronized
				jobMonitor = monitors.get(jobResourceHome);
				if (jobMonitor == null) {
					jobMonitor = new JobMonitor(jobResourceHome);
					monitors.put(jobResourceHome, jobMonitor);
				}
			}
		}
		return jobMonitor;
	}

	/**
	 * Finished (done/failed/unknown) jobs that we no longer need to check
	 */
	private Set<ResourceKey> finishedJobs = new HashSet<ResourceKey>();

	/**
	 * The resource home where we can look up the job resource keys
	 */
	private final InterProScanJobResourceHome jobResourceHome;

	/**
	 * Job utils for updating resource properties
	 */
	private InterProScanJobUtils jobUtils = new InterProScanJobUtils();

	/**
	 * Jobs that we have been asked to monitor through
	 * {@link #monitorJob(ResourceKey)}, jobs can be removed through
	 * {@link #stopMonitoring(ResourceKey)}.
	 */
	private Set<ResourceKey> monitoredJobs = new HashSet<ResourceKey>();

	/**
	 * When is a given job scheduled for a new status update?
	 */
	private Map<ResourceKey, Calendar> nextChecks = new HashMap<ResourceKey, Calendar>();

	/**
	 * The thread that is doing the monitoring
	 */
	private Thread monitorThread = null;

	/**
	 * Protected constructor, use
	 * {@link #getMonitorFor(InterProScanJobResourceHome)} instead.
	 * 
	 * @see #getMonitorFor(InterProScanJobResourceHome)
	 * @param interProScanJobResourceHome
	 */
	protected JobMonitor(InterProScanJobResourceHome interProScanJobResourceHome) {
		this.jobResourceHome = interProScanJobResourceHome;
		checkThread();
	}

	/**
	 * Check that there is a {@link #monitorThread} and that it's running.
	 */
	public void checkThread() {
		if (monitorThread == null || !monitorThread.isAlive()) {
			synchronized (this) {
				if (monitorThread == null || !monitorThread.isAlive()) {
					monitorThread = new Thread(this, "Job monitor");
					monitorThread.start();
				}
			}
		}
	}

	/**
	 * Add job (referenced by ResourceKey) to be monitored. The job resource
	 * property must reside on the {@link InterProScanJobResourceHome} this
	 * JobMonitor was constructed with.
	 * 
	 * @param key ResourceKey for an {@link InterProScanJobResource}
	 */
	public void monitorJob(ResourceKey key) {
		synchronized (monitoredJobs) {
			monitoredJobs.add(key);
		}
		checkThread();
	}

	/**
	 * Process monitored jobs. If the job has not yet been updated, or it's
	 * scheduled check is due, update its job status. If the job status is
	 * {@link JobStatus#done} - also update its output document.
	 */
	public void run() {
		while (true) {
			Set<ResourceKey> jobs;
			synchronized (monitoredJobs) {
				jobs = new HashSet<ResourceKey>(monitoredJobs);
				jobs.removeAll(finishedJobs);
			}
			for (ResourceKey jobKey : jobs) {
				logger.debug("Monitoring " + jobKey);
				if (Thread.interrupted()) {
					logger.warn("Interrupted job monitor thread, aborting");
					return;
				}
				Calendar nextCheck = nextChecks.get(jobKey);
				if (nextCheck != null
						&& nextCheck.after(Calendar.getInstance())) {
					// Don't need to check this job yet
					continue;
				}

				InterProScanJobResource job;
				try {
					// TODO: Is this safe from a different thread?
					job = jobResourceHome.getResource(jobKey);
				} catch (ResourceException e) {
					logger.warn("Could not look up job resource for " + jobKey);
					continue;
				}
				if (job.getJob() == null || job.getJob().getStatus() == null) {
					logger.info("Ignoring job with no jobID yet: " + jobKey);
					continue;
				}

				if (jobUtils.isFinished(job)) {
					// No point in updating it ever again
					synchronized (monitoredJobs) {
						finishedJobs.add(jobKey);
					}
					continue;
				}

				nextCheck = Calendar.getInstance();
				try {
					checkStatus(job);
					nextCheck
							.add(Calendar.SECOND, DEFAULT_CHECK_PERIOD_SECONDS);
				} catch (RuntimeException ex) {
					nextCheck
							.add(Calendar.SECOND, FAILURE_CHECK_PERIOD_SECONDS);
					logger.warn("Could not check status for " + job, ex);
				} catch (RemoteException e) {
					logger.warn("Could not check status for " + job, e);
					nextCheck.add(Calendar.SECOND, FAILURE_CHECK_PERIOD_SECONDS);
				}
				nextChecks.put(jobKey, nextCheck);
				try {
					Thread.sleep(KIND_TO_SERVER_DELAY_MS);
				} catch (InterruptedException e) {
					logger.warn("Interrupted job monitor thread, aborting");
					return;
				}
			}
			// An extra sleep in case there were no jobs
			try {
				Thread.sleep(KIND_TO_SERVER_DELAY_MS);
			} catch (InterruptedException e) {
				logger.warn("Interrupted job monitor thread, aborting");
				return;
			}
		}
	}

	/**
	 * Stop monitoring a job (typically because it has been deleted from the ResourceHome).
	 * 
	 * @param key
	 */
	public void stopMonitoring(ResourceKey key) {
		synchronized (monitoredJobs) {
			monitoredJobs.remove(key);
			finishedJobs.remove(key);
		}
	}

	protected void checkStatus(InterProScanJobResource job)
			throws RemoteException {
		logger.info("Checking status for " + job.getJobId().getValue());
		jobUtils.updateStatus(job);
		jobUtils.updateOutputs(job);
		jobUtils.updateFault(job);
	}
}