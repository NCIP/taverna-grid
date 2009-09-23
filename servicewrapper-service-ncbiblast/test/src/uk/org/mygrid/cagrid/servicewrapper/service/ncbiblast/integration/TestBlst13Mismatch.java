package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
BLST13: mismatch parameter

Default: -3

Test empty/null: Should use default

Test invalid value: Should use maximum value -1


 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst13Mismatch extends CommonTest {

	@Test
	public void defaultMisMatch() throws Exception {
		setNucleotideParams();
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected mismatch, but: "
						+ cmdLine, cmdLine.contains(" -q -3 "));
	}

	@Test
	public void setMisMatch5() throws Exception {
		setNucleotideParams();
		params.setMismatch(BigInteger.valueOf(-5));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected mismatch, but: "
						+ cmdLine, cmdLine.contains(" -q -5 "));
	}

	@Test
	public void invalidMismatch() throws Exception {
		setNucleotideParams();
		// Must be a negative number
		params.setMismatch(BigInteger.valueOf(4));
		clientUtils.ncbiBlastSync(input, 3*LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected mismatch, but: "
						+ cmdLine, cmdLine.contains(" -q -1 "));
	}
}
