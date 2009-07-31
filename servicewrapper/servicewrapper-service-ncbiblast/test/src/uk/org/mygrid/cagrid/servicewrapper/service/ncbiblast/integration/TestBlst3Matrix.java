package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.*;

import org.apache.axis.AxisFault;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;
import uk.org.mygrid.cagrid.valuedomains.Matrix;

/**
 * BLST3: matrix parameter
 * 
 * Default: BLOSUM62
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should produce error
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst3Matrix extends CommonTest {


	@Test(expected = AxisFault.class)
	public void failsInvalidMatrix() throws Exception {
		params.setMatrix(new Matrix("invalidMatrix"){});
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	
	@Test()
	public void emptyUsesDefault() throws Exception {
		System.out.println("Using default (BLOSUM62)");

		params.setMatrix(null);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		// TODO: How to check it actually used BLOSUM62? It seems to have an evalue of 1.0E-79
		// on first hit..
		assertEquals(1.0E-79 , similarities[0].getAlignments(0).getEValue(), 1.0E-82);
		
		// CQTNEECAQNDMCCPSSCGRSCKTPVNIE
	}
	
	@Test
	public void otherMatrix() throws Exception {
		System.out.println("Using PAM30");
		params.setMatrix(Matrix.PAM30);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		// At least it should be different from what we get from BLOSUM62!
		assertEquals(1.0E-128 , similarities[0].getAlignments(0).getEValue(), 1.0E-130);

	}


}
