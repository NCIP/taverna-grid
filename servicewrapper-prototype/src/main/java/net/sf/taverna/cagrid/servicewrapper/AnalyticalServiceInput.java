package net.sf.taverna.cagrid.servicewrapper;

/**
 * Analytical service's input - consists of input parameters for the job and the
 * actual input data.
 * <p>
 * For InterProScan service input, see
 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/interproscan">here</a>.
 * <p>
 * For NCBI BLAST service input, see
 * <a href="http://www.ebi.ac.uk/Tools/webservices/services/ncbiblast">here</a>.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AnalyticalServiceInput {
	
	protected AnalyticalServiceInputParams inputParams;
	
	protected AnalyticalServiceInputData inputData;
	
	public void setInputParams(AnalyticalServiceInputParams inputParams) {
		this.inputParams = inputParams;
	}
	
	public AnalyticalServiceInputParams getInputParams() {
		return inputParams;
	}

	public void setInputData(AnalyticalServiceInputData inputData) {
		this.inputData = inputData;
	}

	public AnalyticalServiceInputData getInputData() {
		return inputData;
	}

}
