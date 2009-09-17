package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import org.junit.Before;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClientUtils;

public class SequenceTools {

	protected static final String URL = "http://localhost:8080/wsrf/services/cagrid/InterProScan";
	protected static final String EMAIL = "mannen@soiland-reyes.com";
	protected static final int TIMEOUT_SECONDS = 60;


	protected InterProScanClientUtils clientUtils  = null;

	@Before
	public void init(){
		InterProScanClient interproscan = null;
		clientUtils  = null;
		try {
			interproscan = new InterProScanClient(URL);
			clientUtils = new InterProScanClientUtils(interproscan);
		} catch (Exception e) {
			System.exit(1);
		}
	}
	
	
	
	protected ProteinSequenceRepresentation makeSequenceRepr() {
		return makeSequenceRepr("uniprot", "wap_rat");
	}

	protected ProteinSequenceRepresentation makeSequenceRepr(String dataSourceName, String crossReferenceId) {
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinGenomicIdentifier proteinId = new ProteinGenomicIdentifier();
		proteinId.setDataSourceName(dataSourceName);
		proteinId.setCrossReferenceId(crossReferenceId);
		sequenceRepresentation.setProteinId(proteinId);
		return sequenceRepresentation;
	}

	protected ProteinSequenceRepresentation makeProteinSequence(String fasta) {
		ProteinSequenceRepresentation proteinSequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinSequence proteinSequence = new ProteinSequence();
		proteinSequence.setValueInFastaFormat(fasta);
		proteinSequenceRepresentation.setProteinSequence(proteinSequence);
		return proteinSequenceRepresentation;
	}

}