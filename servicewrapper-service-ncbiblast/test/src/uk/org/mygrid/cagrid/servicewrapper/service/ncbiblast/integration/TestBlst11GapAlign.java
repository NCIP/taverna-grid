package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

/**
 * BLST11: gapAlignment parameter
 * 
 * Default: true
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should produce error
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst11GapAlign extends CommonTest {

	@Test
	public void defaultGapAlign() throws Exception {
		params.setGapAlignment(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected gap align, but: "
						+ cmdLine, cmdLine.contains(" -gt "));
	}

	@Test
	public void gapAlignmentFalse() throws Exception {
		params.setGapAlignment(false);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected gap align, but: "
						+ cmdLine, cmdLine.contains(" -gf "));
	}

	@Ignore("Can't test with invalid boolean")
	@Test
	public void invalidGapAlignment() throws Exception {
	}
}
