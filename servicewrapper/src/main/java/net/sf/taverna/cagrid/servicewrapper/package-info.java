/**
 * caGrid service wrapping of the public analytical services (<a href=
 * "http://www.ebi.ac.uk/Tools/webservices/services/interproscan"
 * >InterProScan</a> and <a href="http://www.ebi.ac.uk/Tools/webservices/services/ncbiblast">
 * NCBI BLAST</a> provided by EBI.
 * 
 * <p>
 * The public caGrid service is represented by
 * {@link net.sf.taverna.cagrid.servicewrapper.WrappedService}.
 * <p>
 * The inputs and output of the caGrid service are to be defined by the caBIG silver-level compatible 
 * UML model to be provided separately as part of the caGrid-Taverna integration
 * project. The {@link net.sf.taverna.cagrid.servicewrapper.CaGridInput} and
 * {@link net.sf.taverna.cagrid.servicewrapper.CaGridOutput} beans are placeholders for the actual beans
 * to be auto-generated from the XMI files.
 * <p>
 * Internally, {@link net.sf.taverna.cagrid.servicewrapper.WrappedService} will use the
 * {@link net.sf.taverna.cagrid.servicewrapper.DataConverter} to convert the input data to the format
 * expected by the actual analytical service (placeholder {@link net.sf.taverna.cagrid.servicewrapper.AnalyticalServiceInput}),
 * use {@link net.sf.taverna.cagrid.servicewrapper.ServiceInvoker} to invoke the service, and convert the
 * output data (placeholder {@link net.sf.taverna.cagrid.servicewrapper.AnalyticalServiceOutput}) using the converter and
 * returning it to the caller of the caGrid service.
 */

package net.sf.taverna.cagrid.servicewrapper;