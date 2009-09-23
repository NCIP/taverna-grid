package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
 * BLST5: scores parameter
 * 
 * Default: 100
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should use the minimum value 5
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
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue("Command Line did not contain right expected score, but: "
				+ cmdLine, cmdLine.contains(" -v 100 "));
	}

	@Test
	public void specificScore10() throws Exception {
		params.setMaxScores(BigInteger.valueOf(10));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue("Command Line did not contain right expected score, but: "
				+ cmdLine, cmdLine.contains(" -v 10 "));
	}

	@Test
	public void specificScore300() throws Exception {
		// Note: Nothing in output seems to change, so we'll just
		// inspect the used command line parameters
		params.setMaxScores(BigInteger.valueOf(300));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue("Command Line did not contain right expected score, but: "
				+ cmdLine, cmdLine.contains(" -v 300 "));
	}

	@Test
	public void invalidScore() throws Exception {
		params.setMaxScores(BigInteger.valueOf(-20));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Should use the minimum of 5
		assertTrue("Command Line did not contain right expected score, but: "
				+ cmdLine, cmdLine.contains(" -v 5 "));
	}

}
