package net.sf.taverna.cagrid.servicewrapper;

/**
 * Placeholder for actual interface of InterProScan service which can be 
 * generated using axis/CXF or using internal classes of Taverna such as the
 * wsdl-generic module.
 * <p>
 * See <a href="http://www.ebi.ac.uk/Tools/webservices/services/interproscan#runInterProScan">WSDL</a>
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public interface InterProScanService {

	public String runInterProScan(InterProScanInput inputData);

	public String getStatus(String jobId);

	public InterProScanOutput polljobResponse(String jobId);

}
