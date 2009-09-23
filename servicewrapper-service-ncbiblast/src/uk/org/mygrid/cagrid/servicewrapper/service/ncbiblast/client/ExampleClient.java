package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.cagrid.domain.common.MolecularSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.Alignment;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInputParameters;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceFragment;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.JobCallBack;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClientUtils;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;

public class ExampleClient {

	private String url = "http://cagrid.taverna.org.uk:8080/wsrf/services/cagrid/NCBIBlast";
	private static final int TIMEOUT_SECONDS = 60;
	
	public static void usage(){
		System.out.println(ExampleClient.class.getName() + " -url <service url>");
	}

	public static void main(String[] args) throws Exception {
		    System.out.println("Running NCBI Blast client");
		    ExampleClient exampleClient = new ExampleClient();
			if(!(args.length < 2) && args[0].equals("-url")){
					exampleClient.url = args[1];
			} else {
				usage();
				System.out.println("  -- Using default service at " + exampleClient.url);
			}
			exampleClient.main();
	}

	List<NCBIBlastOutput> output = new ArrayList<NCBIBlastOutput>();

	private void main() throws Exception {
		NCBIBlastClient client = new NCBIBlastClient(url);
		NCBIBlastInput input = new NCBIBlastInput();
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinGenomicIdentifier proteinId = new ProteinGenomicIdentifier();
		proteinId.setDataSourceName("uniprot");
		proteinId.setCrossReferenceId("wap_rat");
		sequenceRepresentation.setProteinId(proteinId);
		input.setSequenceRepresentation(sequenceRepresentation);
		NCBIBlastInputParameters params = new NCBIBlastInputParameters();
		params.setEmail("mannen@soiland-reyes.com");

		params.setQueryDatabase(new MolecularSequenceDatabase("", "uniprot"));
		params.setBlastProgram(BLASTProgram.BLASTP);
		input.setNcbiBLASTInputParameters(params);

		NCBIBlastClientUtils clientUtils = new NCBIBlastClientUtils(client);

		if (System.getProperty("GLOBUS_LOCATION") == null) {
			System.out.println("Calling NCBI Blast synchronously");
			System.out
					.println("  (Set -DGLOBUS_LOCATION=/Users/bob/cagrid/ws-core-4.0.3 to do asynchronous client calls)");
			NCBIBlastOutput ncbiBlastOut = clientUtils.ncbiBlastSync(input,
					TIMEOUT_SECONDS * 1000);
			printOutput(ncbiBlastOut);
		} else {
			System.out.println("Calling NCBI Blast asynchronously");
			clientUtils.ncbiBlastAsync(input,
					new JobCallBack<NCBIBlastOutput>() {

						public void jobError(Fault fault) {
							System.err.println("Fault: " + fault);
						}

						public void jobOutputReceived(NCBIBlastOutput jobOutput) {
							System.out.println("Job output received: "
									+ jobOutput);
							synchronized (output) {
								output.add(jobOutput);
								output.notifyAll();
							}
						}

						public void jobStatusChanged(JobStatus oldStatus,
								JobStatus newStatus) {
							System.out.println("Job status is " + newStatus);

						}
					});
		}

		if (System.getProperty("GLOBUS_LOCATION") != null) {

			synchronized (output) {
				output.wait(TIMEOUT_SECONDS * 1000);
				if (!output.isEmpty()) {
					NCBIBlastOutput blastOut = output.get(output.size() - 1);
					printOutput(blastOut);
				} else {
					System.err.println("Time out");
				}
			}
		}

	}

	private void printOutput(NCBIBlastOutput ncbiBlastOut) {
		SequenceSimilarity[] similarities = ncbiBlastOut
				.getSequenceSimilarities();
		System.out.println("Found " + similarities.length + " similarities");
		for (SequenceSimilarity similarity : similarities) {
			System.out.println("Similarity in "
					+ similarity.getSequenceId().getDataSourceName() + ":"
					+ similarity.getSequenceId().getCrossReferenceId()
					+ " (sequence length:" + similarity.getSequenceLength() + ")");
			Alignment[] alignments = similarity.getAlignments();
			System.out.println(" " + alignments.length + " alignments");
			for (Alignment align : alignments) {
				System.out.print("    Alignment score=" + align.getScore());
				System.out.print(" bits=" + align.getBits());
				System.out.println(" eValue=" + align.getEValue());
				SequenceFragment querySequenceFragment = align.getQuerySequenceFragment();
				System.out.print("      Q: " + querySequenceFragment.getSequence().getValue());
				System.out.println(" " + querySequenceFragment.getStart() + "-" + querySequenceFragment.getEnd());
				
				System.out.println("      P: " + align.getSequenceSimilarityPattern());
				
				SequenceFragment matchSequenceFragment = align.getMatchSequenceFragment();
				System.out.print("      M: " + matchSequenceFragment.getSequence().getValue());
				System.out.println(" " + matchSequenceFragment.getStart() + "-" + querySequenceFragment.getEnd());
				
			}
		}

	}
}
