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
package org.cagrid.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
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

/**
 * <p>
 * An Activity that holds a constant string value. It is automatically configured to have no input ports
 * and only one output port named <em>value</em>.<br>
 *
 * @author Stuart Owen
 *
 */
public class CaGridTransferActivity extends AbstractAsynchronousActivity<CaGridTransferConfigurationBean>{

	private static final Logger logger = Logger.getLogger(CaGridTransferActivity.class);
			
	private String value;
	private String fileTransferred;
	
	private CaGridTransferConfigurationBean config=null;
	
	@Override
	public void configure(CaGridTransferConfigurationBean conf)
			throws ActivityConfigurationException {
		this.config=conf;
		
		if (inputPorts.size() == 0) {
			addInput("workingDir", 0, "text/plain");
			addInput("epr", 0, "text/plain");
			addInput("fileNameToTransfer", 0, "text/plain");
			addInput("downloadOrUpload",0,"text/plain");
		}
		if (outputPorts.size() == 0) {
			addOutput("transferredFileName", 0, "text/plain");
		}
	}

	public String getStringValue() {
		return value;
	}
	
	@Override
	public CaGridTransferConfigurationBean getConfiguration() {
		return config;
	}
	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				try {
					String transferredFileName = "";
					
					String workingDir="";
					String fileNameToTransfer="";					
					String epr = "";
					String downloadOrUpload = "";
					
					Map<String,T2Reference> outputData = new HashMap<String, T2Reference>();
					//get input data
					for (String inputName : data.keySet()) {
						ActivityInputPort inputPort = getInputPort(inputName);
						Object input = referenceService.renderIdentifier(data
								.get(inputName), inputPort
								.getTranslatedElementClass(), callback
								.getContext());
						inputName = sanatisePortName(inputName);
						if(inputName.equals("fileNameToTransfer")){
							fileNameToTransfer = (String) input;
						}
						else if(inputName.equals("workingDir")){
							workingDir = (String) input;
						}
						else if(inputName.equals("epr")){
							epr = (String) input;
						}
						else{
							downloadOrUpload = (String)input;
						}
						System.out.println("input port name: "+ inputName+"\t value: "+  (String)input);
					}
					//TODO execute the transfer code
					System.out.println("Doing the caGrid transfer....");
					
					transferredFileName = fileNameToTransfer;
					//put output data
					T2Reference id = referenceService.register(transferredFileName, 0, true, callback.getContext());
					outputData.put("transferredFileName", id);
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					callback.fail(e.getMessage(),e);
				}
			}
			/**
			 * Removes any invalid characters from the port name.
			 * For example, xml-text would become xmltext.
			 * 
			 * 
			 * @param name
			 * @return
			 */
			private String sanatisePortName(String name) {
				String result=name;
				if (Pattern.matches("\\w++", name) == false) {
					result="";
					for (char c : name.toCharArray()) {
						if (Character.isLetterOrDigit(c) || c=='_') {
							result+=c;
						}
					}
				}
				return result;
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
