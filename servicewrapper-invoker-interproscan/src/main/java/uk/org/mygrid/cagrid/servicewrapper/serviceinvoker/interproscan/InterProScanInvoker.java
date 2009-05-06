package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import java.rmi.RemoteException;
import java.util.regex.Pattern;

import org.apache.axis2.AxisFault;

import uk.ac.ebi.www.wsinterproscan.CheckStatusDocument;
import uk.ac.ebi.www.wsinterproscan.CheckStatusResponseDocument;
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
		} catch (AxisFault e) {
			throw new InvokerException("Could not initialize InterProScan service stub", e);
		}
	}

	public String checkStatus(String jobID) throws InvokerException {

		CheckStatusDocument statusDoc = CheckStatusDocument.Factory
				.newInstance();
		statusDoc.addNewCheckStatus().setJobid(jobID);
		CheckStatusResponseDocument checkStatus;
		try {
			checkStatus = interProScan
					.checkStatus(statusDoc);
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
			RunInterProScanDocument runDoc = RunInterProScanDocument.Factory.newInstance();
			runDoc.addNewRunInterProScan().setParams(analyticalServiceInput.getParams());
			WSArrayofData content = runDoc.getRunInterProScan().addNewContent();
			// FIXME: Add analyticalServiceInput.getContent() 
			System.out.println(content.xmlText());
			RunInterProScanResponseDocument response = interProScan.runInterProScan(runDoc);
			return response.getRunInterProScanResponse().getJobid();
		} catch (RemoteException e) {
			throw new InvokerException(e);
		}
	}

}
