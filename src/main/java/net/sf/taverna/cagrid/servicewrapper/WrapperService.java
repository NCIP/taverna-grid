package net.sf.taverna.cagrid.servicewrapper;

/**
 * 
 * Actual caGrid service to be exposed using gRavi or similar that
 * wraps an analytical service such as EBI's InterProScan or NCBI BLAST.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class WrapperService {

	private DataConverter converter;
	private ServiceInvoker invoker;

	/**
	 * Invoke the analytical service with given parameters.
	 * <p>
	 * This operation will block until execution is finished at the upstream
	 * service.
	 * 
	 * @param caGridInput
	 *            Input parameters in caGrid format according to UML models
	 * @return Result in caGrid format according to UML models
	 */
	public CaGridOutput invoke(CaGridInput caGridInput) {
		AnalyticalServiceInput inputData = converter.convertInputData(caGridInput);
		AnalyticalServiceOutput outputData = invoker.invoke(inputData);
		CaGridOutput caGridOutput = converter
				.convertOutputData(outputData);
		return caGridOutput;
	}
}
