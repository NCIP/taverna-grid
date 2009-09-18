package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ebi.www.wsncbiblast.CheckStatusDocument;
import uk.ac.ebi.www.wsncbiblast.CheckStatusResponseDocument;
import uk.ac.ebi.www.wsncbiblast.Data;
import uk.ac.ebi.www.wsncbiblast.PollDocument;
import uk.ac.ebi.www.wsncbiblast.PollResponseDocument;
import uk.ac.ebi.www.wsncbiblast.RunNCBIBlastDocument;
import uk.ac.ebi.www.wsncbiblast.RunNCBIBlastResponseDocument;
import uk.ac.ebi.www.wsncbiblast.WSArrayofData;
import uk.ac.ebi.www.wswublast.GetDatabasesDocument;
import uk.ac.ebi.www.wswublast.GetDatabasesResponseDocument;
import uk.ac.ebi.www.wswublast.OutData;
import uk.ac.ebi.www.wswublast.WSArrayofoutData;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.SequenceDatabase.SequenceType;
import uk.org.mygrid.cagrid.servicewrapper.wsdl.ncbiblast.WSNCBIBlastServiceStub;
import uk.org.mygrid.cagrid.servicewrapper.wsdl.wublast.WSWUBlastServiceStub;

@SuppressWarnings("deprecation")
public class NCBIBlastInvokerImpl implements NCBIBlastInvoker {

	private static Logger logger = Logger.getLogger(NCBIBlastInvokerImpl.class);

	private WSNCBIBlastServiceStub ncbiBlast;

	private WSWUBlastServiceStub wuBlast;

	public NCBIBlastInvokerImpl() throws InvokerException {
		try {
			ncbiBlast = new WSNCBIBlastServiceStub();
			wuBlast = new WSWUBlastServiceStub();
			// To avoid 411 Error: Length Required
			ncbiBlast._getServiceClient().getOptions().setProperty(
					HTTPConstants.CHUNKED, false);
			wuBlast._getServiceClient().getOptions().setProperty(
					HTTPConstants.CHUNKED, false);
		} catch (AxisFault e) {
			logger.error("Could not initialize InterProScan service stub", e);
			throw new InvokerException(
					"Could not initialize InterProScan service stub", e);
		}
	}

	private static final String TYPE = "type";

	private static final String XMLSCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

	private static final String POLL_TYPE = "toolxml";

	private SAXBuilder saxBuilder = new SAXBuilder();

	public String checkStatus(String jobID) throws InvokerException {
		CheckStatusDocument statusDoc = CheckStatusDocument.Factory
				.newInstance();
		statusDoc.addNewCheckStatus().setJobid(jobID);
		CheckStatusResponseDocument checkStatus;
		logger.info("Checking status for " + jobID);
		logger.debug("checkStatus\n" + statusDoc);
		try {
			checkStatus = ncbiBlast.checkStatus(statusDoc);
		} catch (RemoteException e) {
			logger.warn("Can't check status for " + jobID, e);
			throw new InvokerException("Can't check status for " + jobID, e);
		}
		logger.debug("Received status for " + jobID + ": \n" + checkStatus);
		return checkStatus.getCheckStatusResponse().getStatus();
	}

	public org.jdom.Document poll(String jobID) throws InvokerException {
		PollDocument pollDoc = PollDocument.Factory.newInstance();
		pollDoc.addNewPoll().setJobid(jobID);
		pollDoc.getPoll().setType(POLL_TYPE);
		logger.info("Polling for " + jobID);
		logger.debug("poll\n" + pollDoc);
		try {

			PollResponseDocument poll = ncbiBlast.poll(pollDoc);
			logger.debug("Received poll response for " + jobID + ":\n" + poll);

			// FIXME: WSDL says <part name="output" type="xsd:base64Binary" />
			// but actual response is in <result> - need to extract XML
			// manually.
			String base64 = poll.getPollResponse().getDomNode().getFirstChild()
					.getFirstChild().getNodeValue();
			byte[] response = Base64.decode(base64);

			File outFile = File.createTempFile("ncbiblast", ".xml");
			FileUtils.writeByteArrayToFile(outFile, response);

			InputStream byteStream = new ByteArrayInputStream(response);
			org.jdom.Document doc = saxBuilder.build(byteStream);
			return doc;
		} catch (RemoteException e) {
			logger.warn("Can't poll for " + jobID, e);
			throw new InvokerException("Can't poll for " + jobID, e);
		} catch (JDOMException e) {
			logger.warn("Can't parse response for " + jobID, e);
			throw new InvokerException("Can't poll for " + jobID, e);
		} catch (IOException e) {
			logger.warn("Can't read response for " + jobID, e);
			throw new InvokerException("Can't poll for " + jobID, e);
		}
	}

	public String runJob(NCBIBlastInput analyticalServiceInput)
			throws InvokerException {
		try {
			RunNCBIBlastDocument runDoc = RunNCBIBlastDocument.Factory
					.newInstance();
			runDoc.addNewRunNCBIBlast().setParams(
					analyticalServiceInput.getParams());
			// Force async
			runDoc.getRunNCBIBlast().getParams().setAsync(true);

			// XmlBoolean xgetAsync =
			// runDoc.getRunInterProScan().getParams().xgetAsync();

			WSArrayofData content = runDoc.getRunNCBIBlast().addNewContent();

			// Determine array type
			QName arrayType = Data.type.getName();

			Element el = (Element) content.getDomNode();
			String prefix = findPrefix(arrayType.getNamespaceURI(), el
					.getOwnerDocument());
			content
					.setArrayType(prefix + ":" + arrayType.getLocalPart()
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

			RunNCBIBlastResponseDocument response = null;

			setRpcEncodedTypes(runDoc.getRunNCBIBlast());
			logger.debug("runInterProScan:\n" + runDoc);
			if (true) {
				response = ncbiBlast.runNCBIBlast(runDoc);
			}
			logger.debug("Received run response:\n" + response);
			return response.getRunNCBIBlastResponse().getJobid();
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
			prefixes = documentNamespacePrefixes.get(ownerDocument);
			if (prefixes == null) {
				prefixes = new HashMap<String, String>();
				documentNamespacePrefixes.put(ownerDocument, prefixes);
			}
		}
		String prefix = prefixes.get(namespaceURI);
		if (prefix == null) {
			Element element = ownerDocument.getDocumentElement();
			String schemaNS = namespaceURI;
			synchronized (this) {
				prefix = "ns" + prefixCounter++;
			}
			element.setAttribute("xmlns:" + prefix, schemaNS);
			synchronized (prefixes) {
				prefixes.put(namespaceURI, prefix);
			}
		}
		return prefix;
	}

	public List<SequenceDatabase> getDatabases() throws InvokerException {
		List<SequenceDatabase> databases = new ArrayList<SequenceDatabase>();
		GetDatabasesDocument getDatabasesDocument = GetDatabasesDocument.Factory
				.newInstance();
		getDatabasesDocument.addNewGetDatabases();
		GetDatabasesResponseDocument responseDocument;
		try {
			responseDocument = wuBlast.getDatabases(getDatabasesDocument);
		} catch (RemoteException e) {
			String msg = "Could not get databases";
			logger.warn(msg, e);
			throw new InvokerException(msg, e);
		}
		WSArrayofoutData dataArray = responseDocument.getGetDatabasesResponse()
				.getResult();
		for (XmlObject item : dataArray.selectChildren("", "item")) {
			OutData data;
			try {
				data = OutData.Factory.parse(item.copy().getDomNode());
			} catch (XmlException e) {
				String msg = "Could not parse database response";
				logger.warn(msg, e);
				throw new InvokerException(msg, e);
			}
			String name = data.getName();
			SequenceDatabase db = new SequenceDatabase();
			if (name == null || name.length() < 1) {
				logger.warn("Invalid database:\n" + data);
				continue;
			}
			db.setName(name);
			db.setDisplayName(data.getPrintName());
			String dataType = data.getDataType();
			if (dataType == null) {
				logger.warn("Invalid database:\n" + data);
				continue;
			}
			if (dataType.equalsIgnoreCase("protein")) {
				db.setSequenceType(SequenceType.protein);
			} else if (dataType.equalsIgnoreCase("nucleotide")) {
				db.setSequenceType(SequenceType.nucleotide);
			} else {
				logger.warn("Unknown data sequence type  " + dataType);
			}
			databases.add(db);
		}
		return databases;
	}

}
