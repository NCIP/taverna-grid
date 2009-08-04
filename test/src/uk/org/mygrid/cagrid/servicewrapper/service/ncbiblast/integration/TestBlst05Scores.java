package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;

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
public class TestBlst05Scores extends CommonTest {

	@Test
	public void defaultScores() throws Exception {
		params.setMaxScores(null);
		// Just to limit the outputs
		//params.setAlignmentsToOutput(BigInteger.valueOf(5));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected score, but: "
						+ cmdLine, cmdLine.contains(" -v 100 "));
	}
	
	@Test
	public void specificScore10() throws Exception {
		params.setMaxScores(BigInteger.valueOf(10));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected score, but: "
						+ cmdLine, cmdLine.contains(" -v 10 "));
	}
	
	@Test
	public void specificScore300() throws Exception {
		// Note: Nothing in output seems to change, so we'll just
		// inspect the used command line parameters
		params.setMaxScores(BigInteger.valueOf(300));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected score, but: "
						+ cmdLine, cmdLine.contains(" -v 300 "));
	}

	@Test
	public void invalidScore() throws Exception {
		params.setMaxScores(BigInteger.valueOf(-20));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Should use the minimum of 5
		assertTrue(
				"Command Line did not contain right expected score, but: "
						+ cmdLine, cmdLine.contains(" -v 5 "));
	}

		

	
}
