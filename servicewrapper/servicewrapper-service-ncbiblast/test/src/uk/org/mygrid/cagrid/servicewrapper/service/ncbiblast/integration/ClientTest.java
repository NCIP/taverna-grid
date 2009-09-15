package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.cagrid.domain.common.MolecularSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInputParameters;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastOutput;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.JobCallBack;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClientUtils;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;

public class ClientTest {

	private static final int TIMEOUT_SECONDS = 60;

	public static void main(String[] args) throws Exception {
		new ClientTest().main();
	}

	List<NCBIBlastOutput> output = new ArrayList<NCBIBlastOutput>();

	private void main() throws Exception {
		NCBIBlastClient client = new NCBIBlastClient(
				"http://127.0.0.1:8080/wsrf/services/cagrid/NCBIBlast");
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
		
		NCBIBlastClientUtils clientUtils = new NCBIBlastClientUtils(
				client);
		
		
		System.out.println("synchronously");		
		NCBIBlastOutput ncbiBlastOut = clientUtils.ncbiBlastSync(input, TIMEOUT_SECONDS * 1000);
		System.out.println("Accession " + ncbiBlastOut.getSequenceSimilarities(0).getAccessionNumber());
		

		if (System.getProperty("GLOBUS_LOCATION") == null) {
			System.err.println("Set -DGLOBUS_LOCATION=/Users/bob/cagrid/ws-core-4.0.3 to do asynchronous client calls");
		} else {
			System.out.println("asynchronously");
			clientUtils.ncbiBlastAsync(input, new JobCallBack<NCBIBlastOutput>() {
			
				public void jobError(Fault fault) {
					System.err.println("Fault: " + fault);				
				}
				public void jobOutputReceived(NCBIBlastOutput jobOutput) {
					System.out.println("Job output received: " + jobOutput);
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
					NCBIBlastOutput blastOut = output.get(output
							.size() - 1);
					System.out.println("Accession " + blastOut.getSequenceSimilarities(0).getAccessionNumber());
				} else {
					System.err.println("Time out");
				}
			}
		}

	}
}
