package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

public interface Invoker<Input, Output> {
	
	public String runJob(Input analyticalServiceInput) throws InvokerException ;

	public String checkStatus(String jobID) throws InvokerException;

	public Output poll(String jobID) throws InvokerException;
}
