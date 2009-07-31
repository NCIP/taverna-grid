package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import java.rmi.RemoteException;

import org.apache.axis.types.URI.MalformedURIException;
import org.junit.Before;

import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client.NCBIBlastClientUtils;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

public class CommonTest {

	private static final FASTAProteinSequence PROT_SEQUENCE_ID = new FASTAProteinSequence(
			"uniprot:wap_rat");
	// 100 ms
	public static final int SHORT_TIMEOUT = 100;
	// 3 minutes
	public static final int LONG_TIMEOUT = 3 * 60 * 1000;

	public static final FASTAProteinSequence SIMPLE_PROT_SEQUENCE = new FASTAProteinSequence(
			"CQTNEECAQNDMCCPSSCGRSCKTPVNIE");

	protected NCBIBlastClient client;
	protected NCBIBlastClientUtils clientUtils;
	protected NCBIBLASTInputParameters params;
	protected NCBIBLASTInput input;

	@Before
	public void makeClient() throws MalformedURIException, RemoteException {
		client = new NCBIBlastClient(
				"http://127.0.0.1:8080/wsrf/services/cagrid/NCBIBlast");
		input = new NCBIBLASTInput();
		input.setProteinOrNucleotideSequenceRepresentation(PROT_SEQUENCE_ID);
		params = new NCBIBLASTInputParameters();
		params.setEmail("mannen@soiland-reyes.com");
		params.setDatabaseName("uniprot");
		params.setBlastProgram(BLASTProgram.BLASTP);
		input.setNCBIBLASTInputParameters(params);

		clientUtils = new NCBIBlastClientUtils(client);
		
	}

}