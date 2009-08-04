package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import org.apache.axis.AxisFault;
import org.junit.Test;

import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

/**
 * BLST1: program parameter
 * 
 * Default: No default value
 * 
 * Test empty/null: Should produce error
 * 
 * Test invalid value: Should produce error
 * 
 * Additional tests: Verify that for different values of parameter program
 * (BLASTN, BLASTP and BLASTX) the correct list of databases is returned from
 * getDatabases() method by manually cross-checking with EBI's web site.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst01Program extends CommonTest {

	@Test(expected = AxisFault.class)
	public void failsEmptyProgram() throws Exception {
		params.setBlastProgram(null);
		clientUtils.ncbiBlastSync(input, 100);
	}

	@Test(expected = AxisFault.class)
	public void failsInvalidProgram() throws Exception {
		params.setBlastProgram(new BLASTProgram("invalidProgram") {
		});
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsWrongProgram() throws Exception {
		// Our input is a protein id and can't be run against BLASTN
		params.setBlastProgram(BLASTProgram.BLASTN);
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

}
