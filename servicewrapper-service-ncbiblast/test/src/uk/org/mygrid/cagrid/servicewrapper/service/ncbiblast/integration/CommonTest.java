package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import java.math.BigInteger;
import java.rmi.RemoteException;

import org.apache.axis.types.URI.MalformedURIException;
import org.junit.After;
import org.junit.Before;

import uk.ac.ebi.ncbiblast.EBIApplicationResult;
import uk.org.mygrid.cagrid.domain.common.NucleicAcidSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.NucleicAcidSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.NucleicAcidSequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClientUtils;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.client.NCBIBlastJobClient;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

public class CommonTest {

	private static final String URL = "http://cagrid.taverna.org.uk:8080/wsrf/services/cagrid/NCBIBlast";
	protected ProteinGenomicIdentifier PROT_SEQUENCE_ID;
	protected ProteinSequence SIMPLE_PROT_SEQUENCE;
	protected NucleicAcidSequence FAKE_NUCLEOTIDE_SEQUENCE;
	protected NCBIBlastClient client;
	protected NCBIBlastClientUtils clientUtils;
	protected NCBIBlastInputParameters params;
	protected NCBIBlastInput input;
	protected ProteinSequenceDatabase PROTEIN_DATABASE;
	protected NucleicAcidSequenceDatabase NUCLEOTIDE_DATABASE;

	protected ProteinSequenceRepresentation protSeqRepId;
	protected NucleicAcidSequenceRepresentation nucleotideSeqRep;
	protected ProteinSequenceRepresentation protSeqRepSequence;
	

	@Before
	public void makeInputs() {
		
		PROTEIN_DATABASE = new ProteinSequenceDatabase();
		PROTEIN_DATABASE.setName("uniprot");
		NUCLEOTIDE_DATABASE = new NucleicAcidSequenceDatabase(); 
		NUCLEOTIDE_DATABASE.setName("em_rel_std_syn");
		
		PROT_SEQUENCE_ID = new ProteinGenomicIdentifier();
		PROT_SEQUENCE_ID.setDataSourceName("uniprot");
		PROT_SEQUENCE_ID.setCrossReferenceId("WAP_RAT");
		protSeqRepId = new ProteinSequenceRepresentation();
		protSeqRepId.setProteinId(PROT_SEQUENCE_ID);
		
		SIMPLE_PROT_SEQUENCE = new ProteinSequence();
		SIMPLE_PROT_SEQUENCE.setValue("CQTNEECAQNDMCCPSSCGRSCKTPVNIE");
		protSeqRepSequence = new ProteinSequenceRepresentation();
		protSeqRepSequence.setProteinSequence(SIMPLE_PROT_SEQUENCE);

		FAKE_NUCLEOTIDE_SEQUENCE = new NucleicAcidSequence();
		FAKE_NUCLEOTIDE_SEQUENCE.setValue("TAGGAGAGAGAGAGAGACCCCCCCCCCCCCCAGAGAGAGACAGGCAGCAGATTACGATGGTGGTGTGTGAC");
		nucleotideSeqRep = new NucleicAcidSequenceRepresentation();
		nucleotideSeqRep.setNucleicAcidSequence(FAKE_NUCLEOTIDE_SEQUENCE);
		
		input = new NCBIBlastInput();
		
		input.setSequenceRepresentation(protSeqRepId);

		params = new NCBIBlastInputParameters();
		params.setEmail("mannen@soiland-reyes.com");
		params.setQueryDatabase(PROTEIN_DATABASE);
		params.setBlastProgram(BLASTProgram.BLASTP);
		input.setNcbiBLASTInputParameters(params);
	}
	
	
	// 100 ms
	public static final int SHORT_TIMEOUT = 100;
	// 1 minute
	public static final int LONG_TIMEOUT = 1 * 60 * 1000;

	

	@Before
	public void makeClient() throws MalformedURIException, RemoteException {
		client = new NCBIBlastClient(
				URL);
		clientUtils = new NCBIBlastClientUtils(client);	
	}

	@After
	public void sleep() throws InterruptedException {
		// To avoid hammering the service
		Thread.sleep(500);
	}

	public String getCommandLine() throws RemoteException, Exception {
		NCBIBlastJobClient jobClient = clientUtils.getJobClientForInput(input);
		EBIApplicationResult originalOutput = clientUtils
				.getOriginalOutput(jobClient);
		if (originalOutput == null) {
			return "(no command line)";
		}
		String cmdLine = originalOutput.getHeader().getCommandLine()
				.getCommand();
		return cmdLine;
	}

	public void setNucleotideParams() {
		params.setBlastProgram(BLASTProgram.BLASTN);
		params.setQueryDatabase(NUCLEOTIDE_DATABASE);
		params.setAlignmentsToOutput(BigInteger.ONE);		
		input.setSequenceRepresentation(nucleotideSeqRep);
	}

}