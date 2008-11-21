package net.sf.taverna.cagrid.servicewrapper;

/**
 * InterProScan input parameters in actual service's format.
 * <p>
 * According to
 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/interproscan#runInterProScan">InterProScan WSDL</a>.
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class InterProScanInput {

	/**
	 * InterProScan job parameters according to
	 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/interproscan#runInterProScan">InterProScan WSDL</a>.
	 * 
	 * @author Alex Nenadic
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public class InputParams {
		public String app;
		public boolean crc;
		public String seqtype;
		public boolean goterms;
		public boolean async;
		public String email;
	}

	/**
	 * InterProScan job data according to  
	 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/interproscan#runInterProScan">InterProScan WSDL</a>.
	 * @author Alex Nenadic
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public class Data {
		public String type;
		public String content;
	}

	public InputParams params;
	public Data data;
}
