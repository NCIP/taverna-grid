package net.sf.taverna.cagrid.servicewrapper;

/**
 * Invoke the actual InterProScan service using its data formats.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class ServiceInvoker {

	private static final int SLEEP = 500;
	private AnalyticalService service;

	/**
	 * Invoke {@link AnalyticalService} asynchronously.
	 * 
	 * @param inputData
	 *            InterProScan-formatted input
	 * @return InterProScan-formatted output
	 */
	public AnalyticalServiceOutput invoke(AnalyticalServiceInput inputData) {
		String jobId = service.runJob(inputData);
		// TODO: Handle error conditions as well
		while (!(service.getStatus(jobId).equals("DONE"))) {
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
			}
		}
		return service.poll(jobId);
	}
}
