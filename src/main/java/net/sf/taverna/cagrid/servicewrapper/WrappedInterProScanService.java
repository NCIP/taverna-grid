package net.sf.taverna.cagrid.servicewrapper;

/**
 * 
 * Actual caGrid service to be exposed using gRavi or similar.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class WrappedInterProScanService {

	private InterProScanDataConverter converter;
	private InterProScanServiceInvoker invoker;

	/**
	 * Invoke InterProScan service with given parameters.
	 * <p>
	 * This operation will block until execution is finished at the upstream
	 * service.
	 * 
	 * @param caGridInput
	 *            Input parameters in caGrid format according to UML models
	 * @return Result in caGrid format according to UML models
	 */
	public CaGridInterProScanOutput invoke(CaGridInterProScanInput caGridInput) {
		InterProScanInput inputData = converter.convertInputData(caGridInput);
		InterProScanOutput outputData = invoker.invoke(inputData);
		CaGridInterProScanOutput caGridOutput = converter
				.convertOutputData(outputData);
		return caGridOutput;
	}
}
