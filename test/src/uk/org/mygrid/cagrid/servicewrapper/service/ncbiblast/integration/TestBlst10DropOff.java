package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
 * BLST10: dropoff parameter
 * 
 * Default: 0
 * 
 * Test empty/null: Should use default value
 * 
 * Test invalid value: Should use default value
 * 
 * 
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst10DropOff extends CommonTest {

	@Test
	public void defaultDropOff() throws Exception {
		params.setDropoff(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -X 0 "));
	}

	@Test
	public void dropOff14() throws Exception {
		params.setDropoff(BigInteger.valueOf(14));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -X 14 "));
	}

	@Test
	public void invalidDropOff() throws Exception {
		params.setDropoff(BigInteger.valueOf(-20));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Should use the minimum of 0
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -X 0 "));
	}
}
