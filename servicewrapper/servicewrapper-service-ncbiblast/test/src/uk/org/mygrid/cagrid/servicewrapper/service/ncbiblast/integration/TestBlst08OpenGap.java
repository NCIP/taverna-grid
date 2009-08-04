package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Ignore;
import org.junit.Test;

/**
 * BLST8: openGap parameter
 * 
 * Default: Depends on program (BLASTN: 5, BLASTP/BLASTX: 11)
 * 
 * Test empty/null: Should use correct default
 * 
 * Test invalid value: Should produce error
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst08OpenGap extends CommonTest {

	@Test
	public void defaultOpenGap() throws Exception {
		params.setOpenGap(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected open gap, but: "
						+ cmdLine, cmdLine.contains(" -G 11 "));
	}

	@Test
	public void openGap15() throws Exception {
		params.setOpenGap(BigInteger.valueOf(8));
		params.setExtendGap(BigInteger.valueOf(1));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected open gap, but: "
						+ cmdLine, cmdLine.contains(" -G 8 "));
	}

	@Test
	public void invalidGap() throws Exception {
		params.setOpenGap(BigInteger.valueOf(-20));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		// Should use the minimum of 0
		assertTrue(
				"Command Line did not contain right expected open gap, but: "
						+ cmdLine, cmdLine.contains(" -G 0 "));
	}
}
