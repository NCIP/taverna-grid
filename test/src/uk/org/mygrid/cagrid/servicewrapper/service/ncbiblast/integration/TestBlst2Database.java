package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.*;

import org.apache.axis.AxisFault;
import org.junit.Test;

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
public class TestBlst2Database extends CommonTest {

	@Test(expected = AxisFault.class)
	public void failsEmptyProgram() throws Exception {
		params.setDatabaseName(null);
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsInvalidProgram() throws Exception {
		params.setDatabaseName("invalidDatabase");
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test
	public void usesSpecificDatabase() throws Exception {
		String databaseName = "uniprot";
		params.setDatabaseName(databaseName);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		for (SequenceSimilarity similarity : similarities) {
			assertEquals("Different database name", databaseName, similarity.getDatabaseName());
			System.out.println(similarity.getAlignments(0).getQuerySequenceFragment().getSequence());
		}
	}
	
	// TODO: Create and test getDatabases()

	
	
}
