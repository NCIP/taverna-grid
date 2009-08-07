package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.*;

import org.apache.axis.AxisFault;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.Database;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

/**
 * BLST2: databaseName parameter Default: No default value
 * 
 * Test empty/null: Should produce error
 * 
 * Test invalid value: Should produce error
 * 
 * Additional tests: Test that all results (database hits) returned are found
 * only in the database passed as an input parameter and that database belongs
 * to the list of databases for the value of the program parameter.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst02Database extends CommonTest {

	@Test(expected = AxisFault.class)
	public void failsNullDatabase() throws Exception {
		params.setDatabase(null);
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);	
	}

	@Test(expected = AxisFault.class)
	public void failsInnerNullDatabase() throws Exception {
		params.setDatabase(new Database());
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}
	
	@Test(expected = AxisFault.class)
	public void failsInvalidDatabase() throws Exception {
		params.setDatabase(new Database("invalidDatabase"));
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}
	

	@Test(expected = AxisFault.class)
	public void failsEmptyDatabase() throws Exception {
		params.setDatabase(new Database(""));
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test
	public void usesSpecificDatabase() throws Exception {
		String uniprot = "uniprot";
		Database uniprotDb = new Database(uniprot);
		params.setDatabase(uniprotDb);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		
		String commandLine = getCommandLine();
		// A bit fragile test, the database is just a path on the command line
		assertTrue("Wrong database on command line: " +commandLine, 
				commandLine.contains("/blastdb/" + uniprot +" "));
		
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		for (SequenceSimilarity similarity : similarities) {
			assertEquals("Different database name", uniprotDb, similarity.getDatabase());
		}
	}
	
	// TODO: Create and test getDatabases()

	
	
}
