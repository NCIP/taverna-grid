package net.sf.taverna.cagrid.servicewrapper;

/**
 * Interface of an analytical service that can be generated using axis/CXF or 
 * using internal classes of Taverna such as the wsdl-generic module. It is 
 * implemented by classes implementing the actual analytical service - InterProScan
 * or NCBI BLAST. 
 * 
 * <p>
 * See <a href="http://www.ebi.ac.uk/Tools/webservices/wsdl/WSInterProScan.wsdl">InterProScan WSDL</a>.
 * 
 * <p>
 * See <a href="http://www.ebi.ac.uk/Tools/webservices/wsdl/WSNCBIBlast.wsdl">NCBI BLAST WSDL</a>.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public interface AnalyticalService {

	public static enum JOB_STATUS {DONE, ERROR, NOT_FOUND, PENDING, RUNNING};
	
	/**
	 * Run the analytical service job.
	 * @param input 
	 * @return The ID of the submitted job.
	 */
	public String runJob(AnalyticalServiceInput input);
	

	/**
	 * Check the status of the submitted job.
	 * @param jobId
	 * @return Status of the job.
	 */
	public String getStatus(String jobId);

	/**
	 * Poll the job results once status of the job becomes 'DONE'.
	 * @param jobId
	 * @return Results of the run analytical service.
	 */
	public AnalyticalServiceOutput poll(String jobId);

}
