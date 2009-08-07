package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import gov.nih.nci.cagrid.metadata.service.Fault;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.cagrid.domain.common.Database;
import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.JobStatus;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInputParameters;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.JobCallBack;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClientUtils;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

public class ClientTest {

	private static final int TIMEOUT_SECONDS = 60;

	public static void main(String[] args) throws Exception {
		new ClientTest().main();
	}

	List<NCBIBLASTOutput> output = new ArrayList<NCBIBLASTOutput>();

	private void main() throws Exception {
		NCBIBlastClient client = new NCBIBlastClient(
				"http://127.0.0.1:8080/wsrf/services/cagrid/NCBIBlast");
		NCBIBLASTInput input = new NCBIBLASTInput();
		input.setProteinOrNucleotideSequenceRepresentation(new FASTAProteinSequence(
				"uniprot:wap_rat"));
		NCBIBLASTInputParameters params = new NCBIBLASTInputParameters();
		params.setEmail("mannen@soiland-reyes.com");

		params.setDatabase(new Database("uniprot"));
		params.setBlastProgram(BLASTProgram.BLASTP);
		input.setNCBIBLASTInputParameters(params);
		
		NCBIBlastClientUtils clientUtils = new NCBIBlastClientUtils(
				client);
		
		
		System.out.println("synchronously");		
		NCBIBLASTOutput ncbiBlastOut = clientUtils.ncbiBlastSync(input, TIMEOUT_SECONDS * 1000);
		System.out.println("Accession " + ncbiBlastOut.getSequenceSimilarities(0).getAccessionNumber());
		

		if (System.getProperty("GLOBUS_LOCATION") != null) {
			// Set -DGLOBUS_LOCATION=/Users/bob/ws-core-4.0.3 to do 
			// asynchronous
			System.out.println("asynchronously");
			clientUtils.ncbiBlastAsync(input, new JobCallBack<NCBIBLASTOutput>() {
				public void jobStatusChanged(JobStatus oldValue, JobStatus newValue) {
					System.out.println("Job status is " + newValue);
				}
				public void jobOutputReceived(NCBIBLASTOutput jobOutput) {
					System.out.println("Job output received: " + jobOutput);
					synchronized (output) {
						output.add(jobOutput);
						output.notifyAll();
					}
				}
				public void jobError(Fault fault) {
					System.err.println("Fault: " + fault);				
				}
			});
		}
		
	
		
		if (System.getProperty("GLOBUS_LOCATION") != null) {

			synchronized (output) {
				output.wait(TIMEOUT_SECONDS * 1000);
				if (!output.isEmpty()) {
					NCBIBLASTOutput blastOut = output.get(output
							.size() - 1);
					System.out.println("Accession " + blastOut.getSequenceSimilarities(0).getAccessionNumber());
				} else {
					System.err.println("Time out");
				}
			}
		}

	}
}
