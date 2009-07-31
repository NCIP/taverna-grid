package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

/**
 * BLST5: scores parameter
 * 
 * Default: 100
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should produce error
 * 
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst5Scores extends CommonTest {

	@Test
	public void defaultScores() throws Exception {
		params.setMaxScores(null);
		// Just to limit the outputs
		//params.setAlignmentsToOutput(BigInteger.valueOf(5));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		System.out.println("Num similarities " + similarities.length);
	}
	
	@Test
	public void specificScore10() throws Exception {
		params.setMaxScores(BigInteger.valueOf(10));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		System.out.println("Num similarities " + similarities.length);
	}
	
	@Test
	public void specificScore500() throws Exception {
		params.setMaxScores(BigInteger.valueOf(500));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		System.out.println("Num similarities " + similarities.length);
	}


	
}
