package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
 * BLST9: extendGap parameter
 * 
 * Default: Depends on program and open gap (see
 * http://www.ebi.ac.uk/Tools/blastall/help.html#EXTENDGAP )
 * 
 * Test empty/null: Should use correct default
 * 
 * Test invalid value: Should produce error
 * 
 * 
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst09ExtendGap extends CommonTest {

	@Test
	public void defaultExtendGap() throws Exception {
		params.setOpenGap(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected extend gap, but: "
						+ cmdLine, cmdLine.contains(" -E 1 "));
	}

	@Test
	public void extendGap2() throws Exception {
		params.setExtendGap(BigInteger.valueOf(2));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected extend gap, but: "
						+ cmdLine, cmdLine.contains(" -E 2 "));
	}

	@Test
	public void invalidExtendGap() throws Exception {
		params.setExtendGap(BigInteger.valueOf(-20));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Should use the minimum of 0
		assertTrue(
				"Command Line did not contain right expected open gap, but: "
						+ cmdLine, cmdLine.contains(" -E 1 "));
	}
}
