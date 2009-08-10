package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.SequenceDatabase;
import uk.org.mygrid.cagrid.domain.ncbiblast.Alignment;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

/**
 * BLST15: Compare wrapped and original services
 * 
 * Set up test workflows/scripts (such as myExperiment workflow no. 201) to use
 * several combinations of the above parameters. Run these on the NCBI BLAST
 * service directly and through the NCBI BLAST service wrapper and verify that
 * the returned hits are the same in both cases.
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst15Comparison extends CommonTest {
	@Test
	public void findPatterns() throws Exception {
		params.setDatabase(new SequenceDatabase("uniprot", null));
		NCBIBLASTOutput out = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		SequenceSimilarity[] similarities = out.getSequenceSimilarities();
		assertTrue("No similarities found", similarities.length > 0);
		System.out.println("Patterns found:");
		for (SequenceSimilarity similarity : similarities) {
			for (Alignment alignment : similarity.getAlignments()) {
				System.out.println(alignment.getSequenceSimilarityPattern());
			}
		}
	}
}
