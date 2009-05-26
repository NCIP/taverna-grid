package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

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

	private WSInterProScanServiceStub interProScan;

	public InterProScanInvoker() throws InvokerException {
		try {
			interProScan = new WSInterProScanServiceStub();
			// To avoid 411 Error: Length Required
			interProScan._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false); 
			
		} catch (AxisFault e) {
			throw new InvokerException(
					"Could not initialize InterProScan service stub", e);
		}
	}

	public String checkStatus(String jobID) throws InvokerException {

		CheckStatusDocument statusDoc = CheckStatusDocument.Factory
				.newInstance();
		statusDoc.addNewCheckStatus().setJobid(jobID);
		CheckStatusResponseDocument checkStatus;
		try {
			checkStatus = interProScan.checkStatus(statusDoc);
		} catch (RemoteException e) {
			throw new InvokerException("Can't check status for " + jobID, e);
		}
		return checkStatus.getCheckStatusResponse().getStatus();
	}

	public byte[] poll(String jobID) throws InvokerException {
		PollDocument pollDoc = PollDocument.Factory.newInstance();
		pollDoc.addNewPoll().setJobid(jobID);
		try {
			PollResponseDocument poll = interProScan.poll(pollDoc);
			return poll.getPollResponse().getResult();
		} catch (RemoteException e) {
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
			// FIXME: Add analyticalServiceInput.getContent()
			for (Data data : analyticalServiceInput.getContent()) {
				XmlOptions xmlOptions = new XmlOptions();
				xmlOptions.setSaveOuter();
				System.out.println(data.xmlText(xmlOptions));
				DocumentFragment newDomNode = (DocumentFragment) data.newDomNode(xmlOptions );
				Node importNode = content.getDomNode().getOwnerDocument().importNode(newDomNode.getFirstChild(), true);
				content.getDomNode().appendChild(importNode);
			}

			System.out.println(runDoc);

	
			RunInterProScanResponseDocument response = interProScan
					.runInterProScan(runDoc);
			return response.getRunInterProScanResponse().getJobid();
			
			//throw new RuntimeException("");
		} catch (RemoteException e) {
			throw new InvokerException(e);
		}

	}

}
