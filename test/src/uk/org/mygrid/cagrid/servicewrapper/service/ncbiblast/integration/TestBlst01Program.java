package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertTrue;

import org.apache.axis.AxisFault;
import org.junit.Ignore;
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
@Ignore
public class TestBlst01Program extends CommonTest {

	@Test(expected = AxisFault.class)
	public void failsEmptyProgram() throws Exception {
		params.setBlastProgram(null);
		clientUtils.ncbiBlastSync(input, 100);
	}

	@SuppressWarnings("serial")
	@Test(expected = AxisFault.class)
	public void failsInvalidProgram() throws Exception {
		params.setBlastProgram(new BLASTProgram("invalidProgram") {
		});
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsWrongProgramBlastN() throws Exception {
		// Our input is a protein id and can't be run against BLASTN
		params.setBlastProgram(BLASTProgram.BLASTN);
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsWrongProgramBlastP() throws Exception {
		// Our input is a nucleotide sequence and can't be run against BLASTP
		input.setProteinOrNucleotideSequenceRepresentation(
				FAKE_NUCLEOTIDE_SEQUENCE);
		params.setBlastProgram(BLASTProgram.BLASTP);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String commandLine = getCommandLine();
		assertTrue("Wrong program on command line: " +commandLine, commandLine.contains(" -p blastp "));
	}


	@Test()
	public void blastP() throws Exception { 
		params.setBlastProgram(BLASTProgram.BLASTP);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String commandLine = getCommandLine();
		assertTrue("Wrong program on command line: " +commandLine, commandLine.contains(" -p blastp "));
	}

	@Test
	public void blastX() throws Exception {
		params.setBlastProgram(BLASTProgram.BLASTX);
		input.setProteinOrNucleotideSequenceRepresentation(
						FAKE_NUCLEOTIDE_SEQUENCE);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		String commandLine = getCommandLine();
		assertTrue("Wrong program on command line: " +commandLine, commandLine.contains(" -p blastx "));
	}


	@Test
	public void blastN() throws Exception {
		params.setBlastProgram(BLASTProgram.BLASTN);
		params.setDatabase(NUCLEOTIDE_DATABASE);
		input.setProteinOrNucleotideSequenceRepresentation(
						FAKE_NUCLEOTIDE_SEQUENCE);
		// Takes a bit longer to run
		clientUtils.ncbiBlastSync(input, 5*LONG_TIMEOUT);
		String commandLine = getCommandLine();
		assertTrue("Wrong program on command line: " +commandLine, commandLine.contains(" -p blastn "));
	}
}
