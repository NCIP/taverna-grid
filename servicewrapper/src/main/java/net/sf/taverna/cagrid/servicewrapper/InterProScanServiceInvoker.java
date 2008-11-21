package net.sf.taverna.cagrid.servicewrapper;

/**
 * Invoke the actual InterProScan service using its data formats.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class InterProScanServiceInvoker {

	private static final int SLEEP = 500;
	private InterProScanService service;

	/**
	 * Invoke {@link InterProScanService} asynchronously.
	 * 
	 * @param inputData
	 *            InterProScan-formatted input
	 * @return InterProScan-formatted output
	 */
	public InterProScanOutput invoke(InterProScanInput inputData) {
		String jobId = service.runInterProScan(inputData);
		// TODO: Handle error conditions as well
		while (!(service.getStatus(jobId).equals("DONE"))) {
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
			}
		}
		return service.polljobResponse(jobId);
	}
}
