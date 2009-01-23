package net.sf.taverna.cagrid.servicewrapper;

/**
 * Convert data between caGrid-style data (conforming to the UML model) and the actual data format
 * used by the analytical service.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public interface DataConverter {

	/**
	 * Convert caGrid-formatted input to analytical service-formatted input for use
	 * with {@link ServiceInvoker#invoke(AnalyticalServiceInput)}.
	 * 
	 * @param cagridInput
	 *            caGrid-formatted input
	 * @return Analytical service-formatted input
	 */
	public AnalyticalServiceInput convertInputData(
			CaGridInput cagridInput);

	/**
	 * Convert analytical service-formatted output from
	 * {@link ServiceInvoker#invoke(AnalyticalServiceInput)} to
	 * caGrid-formatted output.
	 * 
	 * @param analyticalServiceOutput
	 *            analytical service-formatted output
	 * @return caGrid-formatted output
	 */
	public CaGridOutput convertOutputData(
			AnalyticalServiceOutput analyticalServiceOutput);

}
