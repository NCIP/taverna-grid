package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * BLST6: expectedThreshold parameter
 * 
 * Default: 10
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Use minimum value of 1e-200
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst06ExpectedThreshold extends CommonTest {

	@Test
	public void defaultExpectedThreshold() throws Exception {
		params.setExpectedThreshold(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected threshold, but: "
						+ cmdLine, cmdLine.contains(" -e 10 "));
	}

	@Test
	public void expectedThreshold1000() throws Exception {
		params.setExpectedThreshold(1000.0);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected threshold, but: "
						+ cmdLine, cmdLine.contains(" -e 1000.0 "));
	}

	@Test
	public void expectedThreshold5() throws Exception {
		params.setExpectedThreshold(5.0);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);

		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected threshold, but: "
						+ cmdLine, cmdLine.contains(" -e 5.0 "));
	}

	@Test
	public void expectedInvalid() throws Exception {
		params.setExpectedThreshold(-25.0);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Uses a *very* small -e
		assertTrue(
				"Command Line did not contain right expected threshold, but: "
						+ cmdLine, cmdLine.contains(" -e 1e-200 "));
	}

}
