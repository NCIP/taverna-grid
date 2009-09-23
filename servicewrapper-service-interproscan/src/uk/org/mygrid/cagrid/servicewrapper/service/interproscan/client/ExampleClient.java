package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.Classification;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomain;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainIdentifier;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainMatch;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;

public class ExampleClient {

	public String url = "http://cagrid.taverna.org.uk:8080/wsrf/services/cagrid/InterProScan";
	private static final int TIMEOUT_SECONDS = 120;

	public static void usage() {
		System.out.println(ExampleClient.class.getName()
				+ " -url <service url>");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Running InterProScan client");
		ExampleClient exampleClient = new ExampleClient();
		if (!(args.length < 2) && args[0].equals("-url")) {
			exampleClient.url = args[1];
		} else {
			usage();
			System.out.println("  -- Using default service at "
					+ exampleClient.url);
		}
		exampleClient.main();
	}

	List<InterProScanOutput> output = new ArrayList<InterProScanOutput>();

	private void main() throws Exception {
		InterProScanClient interproscan = new InterProScanClient(url);
		InterProScanInput input = makeInput();

		InterProScanClientUtils clientUtils = new InterProScanClientUtils(
				interproscan);

		if (System.getProperty("GLOBUS_LOCATION") == null) {
			System.out.println("Calling interproscan synchronously");
			System.out
					.println("  (Set -DGLOBUS_LOCATION=/Users/bob/ws-core-4.0.3 to use asynchronous calls)");
			InterProScanOutput interProScanOut = clientUtils.interProScanSync(
					input, TIMEOUT_SECONDS * 1000);
			printOutputs(interProScanOut);
			return;
		}

		System.out.println("Calling interproscan asynchronously");
		clientUtils.interProScanAsync(input,
				new JobCallBack<InterProScanOutput>() {
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
				InterProScanOutput out = output.get(output.size() - 1);
				printOutputs(out);
			} else {
				System.err.println("Time out");
			}
		}

	}

	private InterProScanInput makeInput() {
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
		// params.setSignatureMethods(new SignatureMethod[] {
		// SignatureMethod.PatternScan });

		input.setInterProScanInputParameters(params);
		return input;
	}

	protected void printOutputs(InterProScanOutput interProScanOut) {
		System.out.println("Searched for protein "
				+ interProScanOut.getProteinSequence().getId());
		ProteinDomainMatch[] matches = interProScanOut
				.getProteinDomainMatches();
		System.out.println("Found " + matches.length + " matches");
		for (ProteinDomainMatch match : matches) {
			System.out.println("Match " + match.getId() + " '"
					+ match.getName() + "'");
			Classification[] classifications = match.getClassifications();
			if (classifications != null && classifications.length > 0) {
				System.out.println(" Classifications: ");
				for (Classification classification : classifications) {
					System.out.println(classification.getId() + " "
							+ classification.getCategory() + " "
							+ classification.getClassificationType() + " "
							+ classification.getDescription());
				}
			}
			ProteinDomain[] proteinDomains = match
					.getProteinDomainPerDatabaseMatches();
			System.out.println(" Found " + proteinDomains.length
					+ " protein domains");
			for (ProteinDomain domain : proteinDomains) {
				ProteinDomainIdentifier domainId = domain.getProteinDomainId();
				System.out.println("   " + domainId.getDataSourceName() + " "
						+ domainId.getCrossReferenceId() + ": "
						+ domain.getName());
			}
		}
	}
}
