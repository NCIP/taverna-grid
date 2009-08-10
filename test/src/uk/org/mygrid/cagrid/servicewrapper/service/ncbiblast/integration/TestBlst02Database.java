package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.AxisFault;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.SequenceDatabase;
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
		params.setDatabase(new SequenceDatabase());
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsInvalidDatabase() throws Exception {
		params.setDatabase(new SequenceDatabase("invalidDatabase", null));
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void failsEmptyDatabase() throws Exception {
		params.setDatabase(new SequenceDatabase("", null));
		clientUtils.ncbiBlastSync(input, SHORT_TIMEOUT);
	}

	@Test
	public void usesSpecificDatabase() throws Exception {
		String uniprot = "uniprot";
		SequenceDatabase uniprotDb = new ProteinSequenceDatabase();
		uniprotDb.setDatabaseId(uniprot);
		params.setDatabase(uniprotDb);
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);

		String commandLine = getCommandLine();
		// A bit fragile test, the database is just a path on the command line
		assertTrue("Wrong database on command line: " + commandLine,
				commandLine.contains("/blastdb/" + uniprot + " "));

		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		for (SequenceSimilarity similarity : similarities) {
			assertEquals("Different database name", uniprotDb.getDatabaseId(), similarity
					.getDatabase().getDatabaseId());
		}
	}

	@Test
	public void getDatabases() throws RemoteException {
		SequenceDatabase[] databases = client.getDatabases();
		assertTrue("Expected more than 2 databases", databases.length > 2);

		List<String> databaseNames = new ArrayList<String>();
		for (SequenceDatabase db : databases) {
			databaseNames.add(db.getDatabaseId());
			if (db.getDatabaseId().equals("uniprot")) {
				assertTrue("Database uniprot is not a ProteinSequenceDatabase",
						db instanceof ProteinSequenceDatabase);
				assertFalse("Database uniprot is a NucleotideSequenceDatabase",
						db instanceof NucleotideSequenceDatabase);
			} else if (db.getDatabaseId().equals("em_rel_env")) {
				assertTrue(
						"Database em_rel_env is not a NucleotideSequenceDatabase",
						db instanceof NucleotideSequenceDatabase);
				assertFalse("Database em_rel_env is a ProteinSequenceDatabase",
						db instanceof ProteinSequenceDatabase);
			}
		}

		assertTrue("Did not contain protein db uniprot", databaseNames
				.contains("uniprot"));
		assertTrue("Did not contain protein db swissprot", databaseNames
				.contains("swissprot"));
		assertTrue("Did not contain nucleotide db em_rel_env", databaseNames
				.contains("em_rel_env"));

	}

}
