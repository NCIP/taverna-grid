package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

/**
 * BLST7: filter parameter
 * 
 * Default: false
 * 
 * Test empty/null: Should use the default value
 * 
 * Test invalid value: Should produce error
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst07Filter extends CommonTest {

	@Test
	public void defaultFilter() throws Exception {
		params.setFilter(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected filter, but: "
						+ cmdLine, cmdLine.contains(" -F F "));
	}

	@Test
	public void filterTrue() throws Exception {
		params.setFilter(true);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String cmdLine = getCommandLine();
		assertTrue(
				"Command Line did not contain right expected filter, but: "
						+ cmdLine, cmdLine.contains(" -F T "));
	}

	@Ignore("Not possible to test with invalid boolean")
	@Test
	public void invalidFilter() throws Exception {
	}
	

}
