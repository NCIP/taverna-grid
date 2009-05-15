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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package net.sf.taverna.t2.activities.cagrid;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.cagrid.wsdl.parser.UnknownOperationException;
import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;
import net.sf.taverna.cagrid.wsdl.soap.WSDLSOAPInvoker;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;

/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x
 * and overrides the getCall(EngineConfiguration config) method to enable
 * invocation of secure Web Services.
 * 
 * @author Stuart Owen
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class CaGridWSDLSOAPInvoker extends WSDLSOAPInvoker {

	private static final String REFERENCE_PROPERTIES = "ReferenceProperties";
	private static final String ENDPOINT_REFERENCE = "EndpointReference";
	private static Logger logger = Logger.getLogger(CaGridWSDLSOAPInvoker.class);
	private static final Namespace wsaNS = Namespace.getNamespace("wsa", "http://schemas.xmlsoap.org/ws/2004/03/addressing");

	private String wsrfEndpointReference = null;
	
	// Configuration bean carries security settings for the operation
	private CaGridActivityConfigurationBean configurationBean;

	public CaGridWSDLSOAPInvoker(WSDLParser parser, CaGridActivityConfigurationBean bean,
			List<String> outputNames, String wsrfEndpointReference) {
		super(parser, bean.getOperation(), outputNames);
		this.wsrfEndpointReference = wsrfEndpointReference;	
		this.configurationBean = bean;
	}

	@SuppressWarnings("unchecked")
	protected void addEndpointReferenceHeaders(
			List<SOAPHeaderElement> soapHeaders) {
		// Extract elements
		// Add WSA-stuff
		// Add elements

		Document wsrfDoc;
		try {
			wsrfDoc = parseWsrfEndpointReference(wsrfEndpointReference);
		} catch (JDOMException e) {
			logger.warn("Could not parse endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		} catch (IOException e) {
			logger.error("Could not read endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		}
		
		//Element endpointRefElem = wsrfDoc.getRootElement();
		Element endpointRefElem = wsrfDoc.getRootElement().getChild(ENDPOINT_REFERENCE, wsaNS);
		if (endpointRefElem == null) {
			logger.warn("Could not find " + ENDPOINT_REFERENCE);
			return;
		}
		Element refPropsElem = endpointRefElem.getChild(REFERENCE_PROPERTIES, wsaNS);
		if (refPropsElem == null) {
			logger.warn("Could not find " + REFERENCE_PROPERTIES);
			return;
		}
		
		List<Element> refProps = refPropsElem.getChildren();
		// Make a copy of the list as it would be modified by
		// prop.detach();
		for (Element prop : new ArrayList<Element>(refProps)) {
			DOMOutputter domOutputter = new DOMOutputter();
			SOAPHeaderElement soapElem;
			prop.detach();
			try {
				org.w3c.dom.Document domDoc = domOutputter.output(new Document(prop));
				soapElem = new SOAPHeaderElement(domDoc.getDocumentElement());			
			} catch (JDOMException e) {
				logger.warn("Could not translate wsrf element to DOM:\n" + prop, e);
				continue;
			}
			soapElem.setMustUnderstand(false);
			soapElem.setActor(null);
			soapHeaders.add(soapElem);
		}
		
		

//		soapHeaders.add(new SOAPHeaderElement((Element) wsrfDoc
	//			.getDocumentElement()));
	}


	/**
	 * Returns an Axis-based Call, initialised for the operation that needs to
	 * be invoked.
	 * 
	 * @param config
	 *            - Axis engine configuration containing settings for the
	 *            handlers that are required in order to make a call to a caGrid service.
	 * @return Call object initialised for the operation that needs to be
	 *         invoked.
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws MalformedURLException
	 */
	@Override
	protected Call getCall(EngineConfiguration config) throws ServiceException,
			UnknownOperationException, MalformedURLException {
		
		Call call = super.getCall(config);
		
		// Configure caGrid security properties for the operation, if any
		configureSecurity(call);

		return call;
	}
	
	/** 
	 * This method will configure the axis call with the 
	 * appropriate GSI security configuration parameters based on the security 
	 * metadata provided by the service.
	 */
	protected void configureSecurity(Call call) {
		
		if (configurationBean.getGSITransport() != null){
			call.setProperty(org.globus.wsrf.security.Constants.GSI_TRANSPORT, configurationBean.getGSITransport());
		}
		
		if (configurationBean.getGSISecureConversation() != null){
			call.setProperty(org.globus.wsrf.security.Constants.GSI_SEC_CONV, configurationBean.getGSISecureConversation());
		}
		
		if (configurationBean.getGSISecureMessage() != null){
			call.setProperty(org.globus.wsrf.security.Constants.GSI_SEC_MSG, configurationBean.getGSISecureMessage());
		}
		
		if (configurationBean.getGSIAnonymouos() != null){
			call.setProperty(org.globus.wsrf.security.Constants.GSI_ANONYMOUS, configurationBean.getGSIAnonymouos());
		}
		
		if (configurationBean.getGSICredential() != null){
			call.setProperty(org.globus.axis.gsi.GSIConstants.GSI_CREDENTIALS, configurationBean.getGSICredential());
		}
		
		if (configurationBean.getGSIAuthorisation() != null){
			call.setProperty(org.globus.wsrf.security.Constants.AUTHORIZATION, configurationBean.getGSIAuthorisation());
		}
		
		if (configurationBean.getGSIMode() != null){
			call.setProperty(org.globus.axis.gsi.GSIConstants.GSI_MODE, configurationBean.getGSIMode());
		}
	}
	
	@Override
	protected List<SOAPHeaderElement> makeSoapHeaders() {
		List<SOAPHeaderElement> soapHeaders = new ArrayList<SOAPHeaderElement>(
				super.makeSoapHeaders());
		if (wsrfEndpointReference != null && getParser().isWsrfService()) {
			addEndpointReferenceHeaders(soapHeaders);
		}
		return soapHeaders;
	}

	protected org.jdom.Document parseWsrfEndpointReference(
			String wsrfEndpointReference) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new StringReader(wsrfEndpointReference));
	}

}
