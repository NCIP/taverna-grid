package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ebi.www.wsinterproscan.CheckStatusDocument;
import uk.ac.ebi.www.wsinterproscan.CheckStatusResponseDocument;
import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.PollDocument;
import uk.ac.ebi.www.wsinterproscan.PollResponseDocument;
import uk.ac.ebi.www.wsinterproscan.RunInterProScanDocument;
import uk.ac.ebi.www.wsinterproscan.RunInterProScanResponseDocument;
import uk.ac.ebi.www.wsinterproscan.WSArrayofData;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.Invoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.wsdl.interproscan.WSInterProScanServiceStub;

public class InterProScanInvoker implements Invoker<InterProScanInput, byte[]> {

	private static final String POLL_TYPE = "toolxml";

	private static Logger logger = Logger.getLogger(InterProScanInvoker.class);

	private WSInterProScanServiceStub interProScan;

	public InterProScanInvoker() throws InvokerException {
		try {
			interProScan = new WSInterProScanServiceStub();
			// To avoid 411 Error: Length Required
			interProScan._getServiceClient().getOptions().setProperty(
					HTTPConstants.CHUNKED, false);

		} catch (AxisFault e) {
			logger.error("Could not initialize InterProScan service stub", e);
			throw new InvokerException(
					"Could not initialize InterProScan service stub", e);
		}
	}

	public String checkStatus(String jobID) throws InvokerException {
		CheckStatusDocument statusDoc = CheckStatusDocument.Factory
				.newInstance();
		statusDoc.addNewCheckStatus().setJobid(jobID);
		CheckStatusResponseDocument checkStatus;
		logger.info("Checking status for " + jobID);
		logger.debug("checkStatus\n" + statusDoc);
		try {
			checkStatus = interProScan.checkStatus(statusDoc);
		} catch (RemoteException e) {
			logger.warn("Can't check status for " + jobID, e);
			throw new InvokerException("Can't check status for " + jobID, e);
		}
		logger.debug("Received status for " + jobID + ": \n" + checkStatus);
		return checkStatus.getCheckStatusResponse().getStatus();
	}

	public byte[] poll(String jobID) throws InvokerException {
		PollDocument pollDoc = PollDocument.Factory.newInstance();
		pollDoc.addNewPoll().setJobid(jobID);
		pollDoc.getPoll().setType(POLL_TYPE);
		logger.info("Polling for " + jobID);
		logger.debug("poll\n" +  pollDoc);
		try {
			PollResponseDocument poll = interProScan.poll(pollDoc);
			logger.debug("Received poll response for " + jobID + ":\n" + poll);
			return poll.getPollResponse().getResult();
		} catch (RemoteException e) {
			logger.warn("Can't poll for " + jobID, e);
			throw new InvokerException("Can't poll for " + jobID, e);
		}
	}

	public String runJob(InterProScanInput analyticalServiceInput)
			throws InvokerException {
		try {
			RunInterProScanDocument runDoc = RunInterProScanDocument.Factory
					.newInstance();
			runDoc.addNewRunInterProScan().setParams(
					analyticalServiceInput.getParams());
			WSArrayofData content = runDoc.getRunInterProScan().addNewContent();

			// Determine array type
			QName arrayType = Data.type.getName();
			String wsinPrefix = "wsin95e016c8";
			Element el = (Element) content.getDomNode();
			el.setAttribute("xmlns:" + wsinPrefix, arrayType.getNamespaceURI());
			content.setArrayType(wsinPrefix + ":" + arrayType.getLocalPart()
					+ "[]");

			// Add analyticalServiceInput.getContent()
			for (Data data : analyticalServiceInput.getContent()) {
				XmlOptions xmlOptions = new XmlOptions();
				xmlOptions.setSaveOuter();
				DocumentFragment dataNode = (DocumentFragment) data
						.newDomNode(xmlOptions);

				Element dataElem = content.getDomNode().getOwnerDocument()
						.createElementNS(arrayType.getNamespaceURI(),
								arrayType.getLocalPart());
				content.getDomNode().appendChild(dataElem);
				NodeList childNodes = dataNode.getFirstChild().getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node child = childNodes.item(i);
					Node importNode = dataElem.getOwnerDocument().importNode(
							child, true);
					dataElem.appendChild(importNode);
				}
			}

			logger.info("Running interpro scan for " + analyticalServiceInput);
			logger.debug("runInterProScan:\n" + runDoc);

			RunInterProScanResponseDocument response = interProScan
					.runInterProScan(runDoc);
			logger.debug("Received run response:\n" + response);
			return response.getRunInterProScanResponse().getJobid();
		} catch (RemoteException e) {
			logger.warn("Could not invoke runInterProScan for "
					+ analyticalServiceInput, e);
			throw new InvokerException("Could not invoke runInterProScan", e);
		}
	}

}
