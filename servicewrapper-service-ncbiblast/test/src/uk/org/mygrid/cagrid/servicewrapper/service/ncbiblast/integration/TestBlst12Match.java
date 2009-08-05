package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Ignore;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.FASTANucleotideSequence;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

/**
 * BLST12: match parameter
 * 
 * Default: 2
 * 
 * Test empty/null: Should use default
 * 
 * Test invalid value: Should use minimum value 1
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst12Match extends CommonTest {


	@Test
	public void defaultMatch() throws Exception {
		// Need to use BLASTN to test match
		setNucleotideParams();
		clientUtils.ncbiBlastSync(input, 3*LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -r 2 "));
	}

	@Test
	public void match4() throws Exception {
		setNucleotideParams();
		params.setMatch(BigInteger.valueOf(4));
		clientUtils.ncbiBlastSync(input, 3*LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -r 4 "));
	}

	@Test
	public void invalidMatch() throws Exception {
		setNucleotideParams();
		params.setMatch(BigInteger.valueOf(-3));
		clientUtils.ncbiBlastSync(input, 3*LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected drop off, but: "
						+ cmdLine, cmdLine.contains(" -r 1 "));
	}
}
