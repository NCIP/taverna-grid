package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClientUtils;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.JobCallBack;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

public class ClientTest {

	private static final int TIMEOUT_SECONDS = 60;

	public static void main(String[] args) throws Exception {
		new ClientTest().main();
	}

	List<InterProScanOutput> output = new ArrayList<InterProScanOutput>();

	private void main() throws Exception {
		InterProScanClient interproscan = new InterProScanClient(
				"http://127.0.0.1:8080/wsrf/services/cagrid/InterProScan");
		InterProScanInput input = new InterProScanInput();
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinGenomicIdentifier proteinId = new ProteinGenomicIdentifier();
		proteinId.setDataSourceName("uniprot");
		proteinId.setCrossReferenceId("wap_rat");
		sequenceRepresentation.setProteinId(proteinId);
		input.setSequenceRepresentation(sequenceRepresentation);
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");

		// Using only PatternScan is a bit faster
		params.setSignatureMethods(new SignatureMethod[] { SignatureMethod.PatternScan });

		input.setInterProScanInputParameters(params);
		
		InterProScanClientUtils clientUtils = new InterProScanClientUtils(
				interproscan);
		
		System.out.println("synchronously");
		InterProScanOutput interProScanOut = clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 1000);
		System.out.println("Protein " + interProScanOut.getProteinSequence().getId());
		

		if (System.getProperty("GLOBUS_LOCATION") == null) {
			// Set -DGLOBUS_LOCATION=/Users/bob/ws-core-4.0.3 to do 
			// asynchronous
			return;
		}
		System.out.println("asynchronously");
		
		clientUtils.interProScanAsync(input, new JobCallBack<InterProScanOutput>() {
			public void jobOutputReceived(InterProScanOutput jobOutput) {
				System.out.println("Job output received: " + jobOutput);
				synchronized (output) {
					output.add(jobOutput);
					output.notifyAll();
				}
			}
			public void jobError(Fault fault) {
				System.err.println("Fault: " + fault);				
			}
			public void jobStatusChanged(JobStatus oldStatus,
					JobStatus newStatus) {
				System.out.println("Job status is " + newStatus);
			}
		});

		synchronized (output) {
			output.wait(TIMEOUT_SECONDS * 1000);
			if (!output.isEmpty()) {
				InterProScanOutput out = output.get(output
						.size() - 1);
				System.out.println("Protein " + out.getProteinSequence().getId());
			} else {
				System.err.println("Time out");
			}
		}

	}
}
