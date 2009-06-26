package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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

	private static final String TYPE = "type";

	private static final String XMLSCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

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
		logger.debug("poll\n" + pollDoc);
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
			// Force async
			runDoc.getRunInterProScan().getParams().setAsync(true);

			// XmlBoolean xgetAsync =
			// runDoc.getRunInterProScan().getParams().xgetAsync();

			WSArrayofData content = runDoc.getRunInterProScan().addNewContent();

			// Determine array type
			QName arrayType = Data.type.getName();
			
			
			
			Element el = (Element) content.getDomNode();
			String prefix = findPrefix(arrayType.getNamespaceURI(), el.getOwnerDocument());
			content.setArrayType(prefix + ":" + arrayType.getLocalPart()
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

			RunInterProScanResponseDocument response = null;

			setRpcEncodedTypes(runDoc.getRunInterProScan());
			logger.debug("runInterProScan:\n" + runDoc);
			if (true) {
				response = interProScan.runInterProScan(runDoc);
			}
			logger.debug("Received run response:\n" + response);
			return response.getRunInterProScanResponse().getJobid();
		} catch (RemoteException e) {
			logger.warn("Could not invoke runInterProScan for "
					+ analyticalServiceInput, e);
			throw new InvokerException("Could not invoke runInterProScan", e);
		}
	}

	private void setRpcEncodedTypes(XmlObject xmlObject) {

		for (XmlObject child : xmlObject.selectPath("./*")) {
			setRpcEncodedTypes(child);
		}

		Element element = (Element) xmlObject.getDomNode();
		NamedNodeMap attributes = element.getAttributes();
		Document domDoc = (Document) xmlObject.getDomNode().getOwnerDocument();
		QName schemaName = xmlObject.schemaType().getName();
		if (schemaName == null) {
			return;
		}

		String xsiTypePrefix = findPrefix(schemaName.getNamespaceURI(), element
				.getOwnerDocument());

		String xsiType = xsiTypePrefix + ":" + schemaName.getLocalPart();
		Attr xsiTypeNode = domDoc.createAttributeNS(XMLSCHEMAINSTANCE, TYPE);
		xsiTypeNode.setValue(xsiType);
		attributes.setNamedItemNS(xsiTypeNode);
	}

	private Map<Document, Map<String, String>> documentNamespacePrefixes = new WeakHashMap<Document, Map<String, String>>();

	private long prefixCounter = 0;
	
	private String findPrefix(String namespaceURI, Document ownerDocument) {
		Map<String, String> prefixes;
		synchronized (documentNamespacePrefixes) {
			prefixes = documentNamespacePrefixes
					.get(ownerDocument);
			if (prefixes == null) {
				prefixes = new HashMap<String, String>();
				documentNamespacePrefixes.put(ownerDocument, prefixes);
			}
		}
		String prefix = prefixes.get(namespaceURI);
		if (prefix == null) {
			Element element = ownerDocument.getDocumentElement();
			String schemaNS = namespaceURI;
			synchronized(this) {
				prefix = "ns" + prefixCounter++;
			}
			element.setAttribute("xmlns:" + prefix, schemaNS);
			synchronized(prefixes) {
				prefixes.put(namespaceURI, prefix);
			}
		}
		return prefix;
	}

}
