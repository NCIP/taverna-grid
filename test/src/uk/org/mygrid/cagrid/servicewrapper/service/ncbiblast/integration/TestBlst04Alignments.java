package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.axis.AxisFault;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

/**
 * BLST4: alignmentsToDisplay parameter
 * 
 * Default: 50
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should produce error
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst04Alignments extends CommonTest {



	private static final int INVALID_ALIGNMENTS = -2;
	private static final int DEFAULT_ALIGNMENTS = 50;
	private static final int TINY_ALIGNMENTS = 7;


	@Test
	public void invalidAlignmentUsesMinimum() throws Exception {
		params.setAlignmentsToOutput(BigInteger.valueOf(INVALID_ALIGNMENTS));
		input.setProteinOrNucleotideSequenceRepresentation(SIMPLE_PROT_SEQUENCE);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("Should not have got ", similarities.length > 0);
		assertEquals("Unexpected minimum number of alignments", 
				5, similarities.length);
	}
	
	
	@Test
	public void tinyAlignment() throws Exception {
		params.setAlignmentsToOutput(BigInteger.valueOf(TINY_ALIGNMENTS));
		input.setProteinOrNucleotideSequenceRepresentation(SIMPLE_PROT_SEQUENCE);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertEquals("Invalid number of similarities", TINY_ALIGNMENTS, similarities.length);
	}

	@Test
	public void emptyAlignmentUsesDefault() throws Exception {
		params.setAlignmentsToOutput(null);
		input.setProteinOrNucleotideSequenceRepresentation(SIMPLE_PROT_SEQUENCE);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		assertEquals("Invalid number of similarities", DEFAULT_ALIGNMENTS, similarities.length);
	}

}
