package net.sf.taverna.cagrid.servicewrapper;

/**
 * Convert data between caGrid-style UML-backed data and the actual data format
 * used by the InterProScan service.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public interface InterProScanDataConverter {

	/**
	 * Convert caGrid-formatted input to InterProScan-formatted input for use
	 * with {@link InterProScanServiceInvoker#invoke(InterProScanInput)}.
	 * 
	 * @param cagridInputs
	 *            caGrid-formatted input
	 * @return InterProScan-formatted input
	 */
	public InterProScanInput convertInputData(
			CaGridInterProScanInput cagridInputs);

	/**
	 * Convert InterProScan-formatted outputs from
	 * {@link InterProScanServiceInvoker#invoke(InterProScanInput)} to
	 * caGrid-formatted output.
	 * 
	 * @param outputData
	 *            InterProScan-formatted output
	 * @return caGrid-formatted output
	 */
	public CaGridInterProScanOutput convertOutputData(
			InterProScanOutput outputData);

}
