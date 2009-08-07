package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

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
public class TestBlst03Matrix extends CommonTest {


	@SuppressWarnings("serial")
	@Test(expected = AxisFault.class)
	public void failsInvalidMatrix() throws Exception {
		params.setMatrix(new Matrix("invalidMatrix"){});
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	
	@Test()
	public void emptyUsesDefault() throws Exception {
		params.setMatrix(null);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		
		String commandLine = getCommandLine();
		assertTrue("Wrong matrix on command line: " +commandLine, 
				commandLine.contains(" -M BLOSUM62 "));
		
	}
	
	@Test
	public void matrixPAM30() throws Exception {
		params.setMatrix(Matrix.PAM30);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);

		String commandLine = getCommandLine();
		assertTrue("Wrong matrix on command line: " +commandLine, 
				commandLine.contains(" -M PAM30 "));
	}


}
