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
package net.sf.taverna.cagrid.activity;

import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
import gov.nih.nci.cagrid.metadata.security.CommunicationMechanism;
import gov.nih.nci.cagrid.metadata.security.Operation;
import gov.nih.nci.cagrid.metadata.security.ProtectionLevelType;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadataOperations;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.swing.JOptionPane;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.utils.ClassUtils;
import org.apache.log4j.Logger;
import org.cagrid.gaards.saml.encoding.SAMLSerializerFactory;
import org.globus.axis.gsi.GSIConstants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.xml.sax.SAXException;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.cagrid.activity.CaGridWSDLSOAPInvoker;
import net.sf.taverna.cagrid.activity.InputPortTypeDescriptorActivity;
import net.sf.taverna.cagrid.activity.OutputPortTypeDescriptorActivity;
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
	
	private static Logger logger = Logger.getLogger(CaGridActivity.class);
	
	// Cache of security properties for operations we have obtained during the Taverna run
	// so we do not have to configure them over and over again. The keys in the map are wsdl
	// location of the service concatenated with the operation name. If an operation is not secure
	// we just put null in the map.
	public static HashMap<String, CaGridActivitySecurityProperties> securityPropertiesCache = new HashMap<String, CaGridActivitySecurityProperties>();

	static {
		initializeSecurity();
	}
	
	/**
	 * Configure Axis inside Globus to use caGrid classloader.
	 * <p>
	 * This is so that Axis inside Globus can find caGrid handlers 
	 * such as org.cagrid.gaards.saml.encoding.SAMLSerializerFactory
	 */
	protected static void initializeAxisClassLoader() {		
		ClassLoader classLoader = SAMLSerializerFactory.class.getClassLoader();
		if (ClassUtils.getDefaultClassLoader() != classLoader) {
			ClassUtils.setDefaultClassLoader(classLoader);			
		}
	}
	
	/**
	 * This static block is needed in case some of the caGrid services require
	 * https which is more than likely and needs to be executed before we start
	 * loading caGrid services or otherwise some of these services will fail.
	 * Some caGrid services requiring https have a weird CN in their server
	 * certificates - instead of CN=<HOSTNAME> they have CN="host/"+<HOSTNAME>,
	 * i.e. string "host/" prepended so we have to tell Java's SSL to accept
	 * these hostnames as well. This is not very good at is sets this hostname
	 * verifier across all https connections created in the JVM from now on, but
	 * solves the problem with such caGrid services.
	 * 
	 */
	protected static void setHostNameVerifier() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostName, SSLSession session) {
				String hostNameFromCertificate = null;
				try {
					hostNameFromCertificate = session.getPeerPrincipal()
							.getName().substring(3,
									session.getPeerPrincipal().getName()
											.indexOf(','));
				} catch (SSLPeerUnverifiedException e) {
					e.printStackTrace();
					return false;
				}
				logger.info("Hostname verifier: host from url: " + hostName + " vs. host from certificate: "+ hostNameFromCertificate);
				return (hostName.equals(hostNameFromCertificate) || ("host/"+hostName)
						.equals(hostNameFromCertificate));
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}


	
	
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
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), null, JOptionPane.ERROR_MESSAGE);
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

	/**
	 * Configures security properties of the operation, if any.
	 * 
	 * We made this method synchronised so no two CaGridActivities can configure security
	 * at the same time. This may a bit too harsh but is needed so that one CaGridAcrivity 
	 * can finish with with getting the proxy and saving it with Credential Manager so that the 
	 * next one can pick them up from Credential Manager. This is not needed when two CaGrid
	 * Activities are from different caGrids but anyway.
	 * 
	 * @throws Exception
	 */
	public static void configureSecurity(CaGridActivityConfigurationBean configBean)
			throws Exception {
		
		// If security properties for this operation are in the cache (even if they are null) 
		// then security has already been configured
		if (securityPropertiesCache.keySet().contains(configBean.getWsdl()+configBean.getOperation())){
			return;
		}
		
		CaGridActivitySecurityProperties secProperties = new CaGridActivitySecurityProperties();

		boolean https = configBean.getWsdl().toLowerCase().startsWith("https");		

		// Is this the special getServiceSecurityMetadata() operation
		if (configBean.getOperation().equals("getServiceSecurityMetadata")) {
			if (https) {
				secProperties.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
				secProperties.setGSIAnonymouos(Boolean.TRUE);
				secProperties.setGSIAuthorisation(NoAuthorization.getInstance());
			}
			return;
		}
		
		// Get security metadata for all operations/methods of this service
		// by invoking getServiceSecurityMetadata() method on the service
		ServiceSecurityClient ssc = null;
		try {
			ssc = new ServiceSecurityClient(configBean.getWsdl());
		} catch (MalformedURIException e) {
			logger
					.error("Error occured while configuring security properties of the operation "
							+ configBean.getOperation() + " of service " + configBean.getWsdl()
							+ " : an invalid URI specified for the WSDL location.");
			e.printStackTrace();
			throw e;
		} catch (RemoteException e) {
			logger
			.error("Error occured on the remote service while configuring security properties of the operation "
					+ configBean.getOperation() + " of service " + configBean.getWsdl());
			e.printStackTrace();
			throw e;
		}
		ServiceSecurityMetadata securityMetadata = null;
		if (ssc != null) {
			try {
				securityMetadata = ssc.getServiceSecurityMetadata();
			} catch (RemoteException e) {
				logger
				.error("Error occured on the remote service while getting security metadata for the operation "
						+ configBean.getOperation() + " of service " + configBean.getWsdl());
				e.printStackTrace();
				throw e;
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
				//logger.info("Discovered " + ops.length + " operation(s) of the service that require(s) Globus GSI security.");
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
		if (secureOperationsMap.containsKey(configBean.getOperation())) {
			Operation op = (Operation) secureOperationsMap.get(configBean.getOperation());
			communicationMechanism = op.getCommunicationMechanism(); // specific for this operation, may differ from service default
		} else {
			communicationMechanism = serviceDefaultCommunicationMechanism;
		}
				
		boolean anonymousPrefered = true;

		boolean anonymousAllowed = true;
		boolean authorizationAllowed = true;
		boolean delegationAllowed = true;
		boolean credentialsAllowed = true;
		
		Authorization authorization= null;
		
		String delegationMode = GSIConstants.GSI_MODE_NO_DELEG;
		
		if ((https) && (communicationMechanism.getGSITransport() != null)) {
			ProtectionLevelType level = communicationMechanism.getGSITransport().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					secProperties.setGSITransport(org.globus.wsrf.security.Constants.ENCRYPTION);
				} else {
					secProperties.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				secProperties.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
			}
			delegationAllowed = false;

		} else if (https) {
			secProperties.setGSITransport(org.globus.wsrf.security.Constants.SIGNATURE);
			delegationAllowed = false;
		} else if (communicationMechanism.getGSISecureConversation() != null) {
			ProtectionLevelType level = communicationMechanism.getGSISecureConversation().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					secProperties.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);

				} else {
					secProperties.setGSISecureConversation(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				secProperties.setGSISecureConversation(org.globus.wsrf.security.Constants.ENCRYPTION);
			}

		} else if (communicationMechanism.getGSISecureMessage() != null) {
			ProtectionLevelType level = communicationMechanism.getGSISecureMessage().getProtectionLevel();
			if (level != null) {
				if ((level.equals(ProtectionLevelType.privacy)) || (level.equals(ProtectionLevelType.either))) {
					secProperties.setGSISecureMessage(org.globus.wsrf.security.Constants.ENCRYPTION);
				} else {
					secProperties.setGSISecureMessage(org.globus.wsrf.security.Constants.SIGNATURE);
				}

			} else {
				secProperties.setGSISecureMessage(org.globus.wsrf.security.Constants.ENCRYPTION);
			}
			delegationAllowed = false;
			anonymousAllowed = false;
		} else {
			// Service is not secure (i.e. does not require https nor Globus GSI security)
			// We can exit.
			/*
			anonymousAllowed = false;
			authorizationAllowed = false;
			delegationAllowed = false;
			credentialsAllowed = false;
			*/
			securityPropertiesCache.put(configBean.getWsdl()+configBean.getOperation(), null);
			return;
		}

		if ((anonymousAllowed) && (communicationMechanism.isAnonymousPermitted()) && anonymousPrefered) {
			secProperties.setGSIAnonymouos(Boolean.TRUE);
			secProperties.setRequiresProxy(false);
		} else if (credentialsAllowed) {
			// Set that this operation requires proxy that will be fetched later by the
			// CaGridWSDLInvoker from the Credential Manager
			secProperties.setRequiresProxy(true);			
		}

		if (authorizationAllowed) {
			if (authorization == null) {
				secProperties.setGSIAuthorisation(NoAuthorization.getInstance());
			} else {
				secProperties.setGSIAuthorisation(authorization);
			}
		}
		if (delegationAllowed) {
			if (delegationMode != null) {
				secProperties.setGSIMode(delegationMode);
			}
		}	
		securityPropertiesCache.put(configBean.getWsdl()
				+ configBean.getOperation(), secProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {
				
				try{
					// Configure security if service requires it
					configureSecurity(getConfiguration());
				}
				catch(Exception ex){
					logger.error(
							"Error configuring security properties for the operation "
									+ getConfiguration().getOperation()
									+ " of the service "
									+ getConfiguration().getWsdl(), ex);
					callback.fail("Error configuring security properties for the operation "
							+ getConfiguration().getOperation()
							+ " of the service "
							+ getConfiguration().getWsdl(), ex);
					return;
				}

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

					CaGridWSDLSOAPInvoker invoker = new CaGridWSDLSOAPInvoker(parser,
							configurationBean, outputNames,
							endpointReference);
					
					EngineConfiguration engineConfiguration = null;
					// Load caGrid's context sensitive wsdd file
					InputStream resourceAsStream = getClass().getResourceAsStream("client-config.wsdd");
					if (resourceAsStream != null) {
						// We found it, so tell axis to configure an engine to use it
						engineConfiguration = new FileProvider(resourceAsStream);
						// Note that security parameters for invoking the operation that we have 
						// discovered previously will be set as part of the invoke() method when 
						// the axis call for the service gets created
					}
					else{
						// We are in trouble - caGrid services will not work properly
						logger.error("Could not load CaGrid's client-config.wsdd file.");
						callback.fail("Could not load CaGrid's client-config.wsdd file.");
						return;
					}
					
					Map<String, Object> invokerOutputMap;
					// We sync here on Security class as we do not want
					// anyone changing our BC Security Provider in the meantime
					synchronized (Security.class) {
						invokerOutputMap = invoker.invoke(
								invokerInputMap, engineConfiguration);
					}

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
					//e.printStackTrace();
					callback.fail(
							"An error occurred invoking the CaGridActivity", e);
					return;
				}

			}

		});

	}

	private static transient boolean securityInitialized = false;
	
	public static void initializeSecurity() {
		if (securityInitialized) {
			return;
		}
		synchronized(CaGridActivity.class){
			if (! securityInitialized) {
				initializeAxisClassLoader();
				setHostNameVerifier();
			}
		}
	}

}
