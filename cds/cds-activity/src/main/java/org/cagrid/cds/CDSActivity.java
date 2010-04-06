/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * Copyright (C) 2009 The University of Chicago
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
package org.cagrid.cds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMUtil;

import gov.nih.nci.cagrid.opensaml.SAMLAssertion;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.cagrid.gaards.authentication.BasicAuthentication;
import org.cagrid.gaards.authentication.client.AuthenticationClient;
import org.cagrid.gaards.dorian.client.GridUserClient;
import org.cagrid.gaards.dorian.federation.CertificateLifetime;
import org.globus.gsi.GlobusCredential;
import org.cagrid.gaards.cds.client.ClientConstants;
import org.cagrid.gaards.cds.client.DelegationUserClient;
import org.cagrid.gaards.cds.common.IdentityDelegationPolicy;
import org.cagrid.gaards.cds.common.ProxyLifetime;
import org.cagrid.gaards.cds.common.Utils;
import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
import org.globus.wsrf.encoding.ObjectSerializer;

/**
 * <p>
 * An Activity that use http to transfer the files that are needed/produced by a caGrid service.
 * 
 *
 * @author Wei Tan
 *
 */
public class CDSActivity extends AbstractAsynchronousActivity<CDSConfigurationBean>{

	private static final Logger logger = Logger.getLogger(CDSActivity.class);
			
	
	private CDSConfigurationBean config=null;
	
	@Override
	public void configure(CDSConfigurationBean conf)
			throws ActivityConfigurationException {
		this.config=conf;
		if (outputPorts.size() == 0) {
			addOutput("EPR", 0, "text/plain");
		}
		
		
	}

	
	@Override
	public CDSConfigurationBean getConfiguration() {
		return config;
	}
	/*
	 * input port is no longer needed. properties are now in configuration bean
	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}
	*/

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				//String cdsURL = "https://cds.cvrgrid.cci.emory.edu:8443/wsrf/services/cagrid/CredentialDelegationService";
				try {
					
					//get users Globus credential in a certain caGrid, like NCI_Prod or CVRG
					//System.out.println("Get user's Globus Credential in "+ config.getCaGridName());
					File secConfigDirectory = CMUtil.getSecurityConfigurationDirectory();
					String path = secConfigDirectory.getAbsolutePath()+"/cagrid/trusted-certificates";
					System.setProperty("X509_CERT_DIR",path);					
					GlobusCredential proxy = CDSUtil.getGlobusCredential(config.getCaGridName());
					//TODO should be an input or configuration property
					//delegate to whom
					//delegate to myself
					//String party = "/O=caBIG/OU=caGrid/OU=LOA1/OU=Dorian/CN=wtan";
					//delegate to the autoqrs service
					//String party = "/O=CVRG/OU=caGrid/OU=LOA1/OU=Dorian/CN=taverna";
					//String party = "/O=CVRG/OU=LOA1/OU=Services/CN=cvrg02e.cvrgrid.org";
					//delegate to FQP
					//String party = "/O=caBIG/OU=caGrid/OU=Services/CN=cagrid-fqp.nci.nih.gov";
					System.out.println("Delegate Credential\n" +
							"caGrid:"+config.getCaGridName()+"\n"+
							"party:"+config.getParty()+"\n"+
							"delegationLifeTime:"+config.getDelegationLifetime()+"\n"+
							"delegationPathLength:"+config.getDelegationPathLength()+"\n"+
							"issuedCredentialLifeTime:"+config.getIssuedCredentialLifetime()+"\n"+
							"issuedCredentialPathLength:"+config.getIssuedCredentialPathLength()+"\n"
					);
					String epr = CDSUtil.delegateCredential(config.getCaGridName(),proxy, 
							config.getParty(),config.getDelegationLifetime(),config.getDelegationPathLength(),
							config.getIssuedCredentialLifetime(),config.getIssuedCredentialPathLength());					
					System.out.println(epr);
					System.out.println("Original Credential \n Identity: "+ proxy.getIdentity()+ 
							"\nSubject: "+ proxy.getSubject()+"\nIssuer: "+ proxy.getIssuer());
					
					//put output data
					Map<String,T2Reference> outputData = new HashMap<String, T2Reference>();
					T2Reference id = referenceService.register(epr, 0, true, callback.getContext());
					outputData.put("EPR", id);
					callback.receiveResult(outputData, new int[0]);
					} 
				catch (ReferenceServiceException e) {
					callback.fail(e.getMessage(),e);
				}
				catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
			}
				
		
		});
		
	}

	protected void addOutput(String portName, int portDepth, String type) {
		OutputPort port = EditsRegistry.getEdits().createActivityOutputPort(
				portName, portDepth, portDepth);
		MimeType mimeType = new MimeType();
		mimeType.setText(type);
		try {
			EditsRegistry.getEdits().getAddAnnotationChainEdit(port, mimeType).doEdit();
		} catch (EditException e) {
			logger.debug("Error adding MimeType annotation to port", e);
		}
		outputPorts.add(port);
	}
	protected void addInput(String portName, int portDepth, String type) {
		ActivityInputPort port = EditsRegistry.getEdits().createActivityInputPort(
				portName, portDepth, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(),String.class);
		MimeType mimeType = new MimeType();
		//should be "text/plain"
		mimeType.setText(type);
		try {
			EditsRegistry.getEdits().getAddAnnotationChainEdit(port, mimeType).doEdit();
		} catch (EditException e) {
			logger.debug("Error adding MimeType annotation to port", e);
		}
		inputPorts.add(port);
	}

}
