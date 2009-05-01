/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.cagrid;

import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import gov.nih.nci.cagrid.metadata.security.CommunicationMechanism;
import gov.nih.nci.cagrid.metadata.security.Operation;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadataOperations;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.globus.gsi.GlobusCredential;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.ietf.jgss.GSSCredential;
import org.xml.sax.SAXException;

import net.sf.taverna.t2.activities.cagrid.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.cagrid.OutputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.cagrid.T2WSDLSOAPInvoker;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.cagrid.wsdl.parser.TypeDescriptor;
import net.sf.taverna.cagrid.wsdl.parser.UnknownOperationException;
import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;


/**
 * An asynchronous Activity that can invoke caGrid WSDL based web-services.
 * <p>
 * The activity is configured according to the WSDL location and the operation.<br>
 * The ports are defined dynamically according to the WSDL specification, and in
 * addition an output<br>
 * port <em>attachmentList</em> is added to represent any attachments that are
 * returned by the web service.
 * </p>
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 */
public class CaGridActivity extends
AbstractAsynchronousActivity<CaGridActivityConfigurationBean> implements
InputPortTypeDescriptorActivity, OutputPortTypeDescriptorActivity {

	private static final String ENDPOINT_REFERENCE = "EndpointReference";
	private CaGridActivityConfigurationBean configurationBean;
	private WSDLParser parser;
	private Map<String, Integer> outputDepth = new HashMap<String, Integer>();
	private boolean isWsrfService = false;
	private String endpointReferenceInputPortName;
	
	// Security settings for this operation of a caGrid service, if any, obtained by invoking
	// getServiceSecurityMetadata() on the service
	private String indexServiceURL; // URL of the Index Service used to discover this caGrid service (used as alias for username/password and proxy entries in the Taverna's keystore)
	private String authNServiceURL; // URL of the AuthN Service used or to be used to (re)authenticate the user
	private String dorianServiceURL; // URL of the Dorian Service used or to be used to (re)issue proxy
	private Integer gsi_transport;
	private Boolean gsi_anonymouos;
	private Authorization authorisation;
	private Integer gsi_secure_conversation;
	private Integer gsi_secure_message;
	private String gsi_mode;
	private GSSCredential gsi_credential; // GSSCredential wraps the proxy used for context initiation, acceptance or both
	private GlobusCredential proxy; // proxy
	
	
	private static Logger logger = Logger.getLogger(CaGridActivity.class);
	
	public boolean isWsrfService() {
		return isWsrfService;
	}

	/**
	 * Configures the activity according to the information passed by the
	 * configuration bean.<br>
	 * During this process the WSDL is parsed to determine the input and output
	 * ports.
	 * 
	 * @param bean
	 *            the {@link CaGridActivityConfigurationBean} configuration bean
	 */
	@Override
	public void configure(CaGridActivityConfigurationBean bean)
			throws ActivityConfigurationException {
		if (this.configurationBean != null) {
			throw new IllegalStateException(
					"Reconfiguring CaGrid activity not yet implemented");
		}
		this.configurationBean = bean;
		try {
			parseWSDL();
			configurePorts();
			configureSecurity();
		} catch (Exception ex) {
			throw new ActivityConfigurationException(
					"Failed to configure CaGridActivity", ex);
		}
	}

	/**
	 * @return a {@link CaGridActivityConfigurationBean} representing the
	 *         CaGridActivity configuration
	 */
	@Override
	public CaGridActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity#
	 * getTypeDescriptorForInputPort(java.lang.String)
	 */
	public TypeDescriptor getTypeDescriptorForInputPort(String portName)
			throws UnknownOperationException, IOException {
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationInputParameters(configurationBean.getOperation());
		TypeDescriptor result = null;
		for (TypeDescriptor descriptor : inputDescriptors) {
			if (descriptor.getName().equals(portName)) {
				result = descriptor;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity#
	 * getTypeDescriptorsForInputPorts()
	 */
	public Map<String, TypeDescriptor> getTypeDescriptorsForInputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationInputParameters(configurationBean.getOperation());
		for (TypeDescriptor descriptor : inputDescriptors) {
			descriptors.put(descriptor.getName(), descriptor);
		}
		return descriptors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity#
	 * getTypeDescriptorForOutputPort(java.lang.String)
	 */
	public TypeDescriptor getTypeDescriptorForOutputPort(String portName)
			throws UnknownOperationException, IOException {
		TypeDescriptor result = null;
		List<TypeDescriptor> outputDescriptors = parser
				.getOperationOutputParameters(configurationBean.getOperation());
		for (TypeDescriptor descriptor : outputDescriptors) {
			if (descriptor.getName().equals(portName)) {
				result = descriptor;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity#
	 * getTypeDescriptorsForOutputPorts()
	 */
	public Map<String, TypeDescriptor> getTypeDescriptorsForOutputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationOutputParameters(configurationBean.getOperation());
		for (TypeDescriptor descriptor : inputDescriptors) {
			descriptors.put(descriptor.getName(), descriptor);
		}
		return descriptors;
	}

	protected void parseWSDL() throws ParserConfigurationException,
			WSDLException, IOException, SAXException, UnknownOperationException {
		parser = new WSDLParser(configurationBean.getWsdl());
	}

	protected void configurePorts() throws UnknownOperationException, IOException {
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationInputParameters(configurationBean.getOperation());
		List<TypeDescriptor> outputDescriptors = parser
				.getOperationOutputParameters(configurationBean.getOperation());
		for (TypeDescriptor descriptor : inputDescriptors) {
			addInput(descriptor.getName(), descriptor.getDepth(), true, null,
					String.class);
		}
		isWsrfService = parser.isWsrfService();
		if (isWsrfService) {
			// Make sure the port name is unique
			endpointReferenceInputPortName = ENDPOINT_REFERENCE;
			int counter = 0;
			while (Tools.getActivityInputPort(this,
					endpointReferenceInputPortName) != null) {
				endpointReferenceInputPortName = ENDPOINT_REFERENCE + counter++;
			}
			addInput(endpointReferenceInputPortName, 0, true, null,
					String.class);
		}

		for (TypeDescriptor descriptor : outputDescriptors) {
			addOutput(descriptor.getName(), descriptor.getDepth());
			outputDepth.put(descriptor.getName(), Integer.valueOf(descriptor
					.getDepth()));
		}

		// add output for attachment list
		addOutput("attachmentList", 1);
		outputDepth.put("attachmentList", Integer.valueOf(1));
	}

	
	public void configureSecurity()
			throws Exception {
		
		// Get security metadata for all operations/methods of this service
		// by invoking getServiceSecurityMetadata() method on the service
		ServiceSecurityClient ssc = null;
		try {
			ssc = new ServiceSecurityClient(parser.getWSDLLocation());
		} catch (MalformedURIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceSecurityMetadata securityMetadata = null;
		if (ssc != null) {
			try {
				securityMetadata = ssc.getServiceSecurityMetadata();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Get all secure OperationS of the service which security properties differ from the 
		// default security properties for the service itself and map them to their names.
		// Only operations which security properties are different from those of the service itself 
		// will be detected here - whether because they require more stringent or more loose security.
		Map<String, Operation> secureOperationsMap = new HashMap<String, Operation>();
		ServiceSecurityMetadataOperations ssmo = null; 
		if (securityMetadata != null){
			ssmo = securityMetadata.getOperations(); // all operations of the service requiring GSI security properties
		}
		if (ssmo != null) {
			Operation[] ops = ssmo.getOperation();
			if (ops != null) {
				logger.info("Discovered " + ops.length + " operation(s) of the service that require(s) Globus GSI security.");
				for (int i = 0; i < ops.length; i++) {
					//System.out.println("Secure operation name: " + ops[i].getName());
					//String lowerMethodName = ops[i].getName().substring(0, 1)
					//		.toLowerCase()
					//		+ ops[i].getName().substring(1);
					//secureOperationsMap.put(lowerMethodName, ops[i]);
					//System.out.println("Lowercase secure operation name: " + lowerMethodName);
					secureOperationsMap.put(ops[i].getName(), ops[i]);
				}
			}
		}
		
		CommunicationMechanism serviceDefaultCommunicationMechanism = securityMetadata.getDefaultCommunicationMechanism();
		CommunicationMechanism communicationMechanism = null;
		if (secureOperationsMap.containsKey(configurationBean.getOperation())) {
			Operation op = (Operation) secureOperationsMap.get(configurationBean.getOperation());
			communicationMechanism = op.getCommunicationMechanism(); // specific for this operation, may differ from service default
		} else {
			communicationMechanism = serviceDefaultCommunicationMechanism;
		}
		
		//indexServiceURL = configurationBean.getIndexServiceURL();
		//authNServiceURL = configurationBean.getAuthNServiceURL();
		//dorianServiceURL = configurationBean.getDorianServiceURL();
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {

				ReferenceService referenceService = callback.getContext()
						.getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
				Map<String, Object> invokerInputMap = new HashMap<String, Object>();

				try {
					String endpointReference = null;
					for (String key : data.keySet()) {
						Object renderIdentifier = referenceService
								.renderIdentifier(data.get(key), String.class,
										callback.getContext());
						if (isWsrfService()
								&& key.equals(endpointReferenceInputPortName)) {
							endpointReference = (String) renderIdentifier;
						} else {
							invokerInputMap.put(key, renderIdentifier);
						}
					}
					List<String> outputNames = new ArrayList<String>();
					for (OutputPort port : getOutputPorts()) {
						outputNames.add(port.getName());
					}

					T2WSDLSOAPInvoker invoker = new T2WSDLSOAPInvoker(parser,
							configurationBean.getOperation(), outputNames,
							endpointReference);
					CaGridActivityConfigurationBean bean = getConfiguration();
					EngineConfiguration wssEngineConfiguration = null;

					// Set security properties on the engine here
					
					/*if (bean.getSecurityProfileString() != null) {
						wssEngineConfiguration = new XMLStringProvider(bean
								.getSecurityProfileString());
					}*/

					Map<String, Object> invokerOutputMap = invoker.invoke(
							invokerInputMap, wssEngineConfiguration);

					for (String outputName : invokerOutputMap.keySet()) {
						Object value = invokerOutputMap.get(outputName);

						if (value != null) {
							Integer depth = outputDepth.get(outputName);
							if (depth != null) {
								outputData.put(outputName, referenceService
										.register(value, depth, true, callback
												.getContext()));
							} else {
								System.out
										.println("Depth not recorded for output:"
												+ outputName);
								// TODO what should the depth be in this case?
								outputData.put(outputName, referenceService
										.register(value, 0, true, callback
												.getContext()));
							}
						}
					}
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					logger.error("Error finding the input data for "
							+ getConfiguration().getOperation(), e);
					callback.fail("Unable to find input data", e);
					return;
				} catch (Exception e) {
					logger.error("Error invoking caGrid service "
							+ getConfiguration().getOperation(), e);
					callback.fail(
							"An error occurred invoking the CaGridActivity", e);
					return;
				}

			}

		});

	}

}
