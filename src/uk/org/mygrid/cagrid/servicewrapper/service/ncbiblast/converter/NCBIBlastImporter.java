package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

import uk.ac.ebi.schema.EBIApplicationResultDocument;
import uk.ac.ebi.schema.TAlignment;
import uk.ac.ebi.schema.THit;
import uk.ac.ebi.schema.TMatchSeq;
import uk.ac.ebi.schema.TQuerySeq;
import uk.ac.ebi.schema.TSSSR;
import uk.org.mygrid.cagrid.domain.common.Database;
import uk.org.mygrid.cagrid.domain.ncbiblast.Alignment;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceFragment;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

public class NCBIBlastImporter {

	public NCBIBLASTOutput importNCBIBlastOutput(Document data)
			throws XmlException, JDOMException {
		DOMOutputter domOutputter = new DOMOutputter();
		EBIApplicationResultDocument appResults = EBIApplicationResultDocument.Factory
				.parse(domOutputter.output(data));

		TSSSR searchResult = appResults.getEBIApplicationResult()
				.getSequenceSimilaritySearchResult();

		NCBIBLASTOutput ncbiblastOutput = new NCBIBLASTOutput();
		SequenceSimilarity[] sequenceSimilarities = importSequenceSimilarities(searchResult);
		ncbiblastOutput.setSequenceSimilarities(sequenceSimilarities);
		return ncbiblastOutput;
	}

	public SequenceSimilarity[] importSequenceSimilarities(TSSSR searchResult) {
		List<SequenceSimilarity> similarities = new ArrayList<SequenceSimilarity>();
		for (THit hit : searchResult.getHits().getHitArray()) {
			SequenceSimilarity sequenceSimilarity = new SequenceSimilarity();
			similarities.add(sequenceSimilarity);
			sequenceSimilarity.setId(hit.getId());
			sequenceSimilarity.setDatabase(new Database(hit.getDatabase()));
			sequenceSimilarity.setAccessionNumber(hit.getAc());
			sequenceSimilarity.setSequenceLength(BigInteger.valueOf(hit
					.getLength()));
			sequenceSimilarity.setDescription(hit.getDescription());

			sequenceSimilarity.setAlignments(importAlignments(
					hit.getAlignments().getAlignmentArray()));
			
		}
		return similarities.toArray(new SequenceSimilarity[0]);
	}

	public Alignment[] importAlignments(TAlignment[] alignmentArray) {
		List<Alignment> alignments = new ArrayList<Alignment>();
		for (TAlignment talignment : alignmentArray) {
			Alignment alignment = new Alignment();
			alignment.setBits(talignment.getBits());
			alignment.setScore(talignment.getScore());
			alignment.setEValue(talignment.getExpectation());
			alignment.setPositives(BigInteger.valueOf((long) talignment.getPositives()));			
			alignment.setIdentity(BigInteger.valueOf((long) talignment.getIdentity()));
			
			alignment.setQuerySequenceFragment(importQuerySequenceFragment(talignment.getQuerySeq()));		
			alignment.setMatchSequenceFragment(importMatchSequenceFragment(talignment.getMatchSeq()));
			alignment.setSequenceSimilarityPattern(talignment.getPattern());
			
			// TODO:  What about things like 			
//			talignment.getSmithWatermanScore();
//			talignment.getUngapped();
//			talignment.getZScore();
			
			alignments.add(alignment);
		}
		return alignments.toArray(new Alignment[0]);
	}

	public SequenceFragment importMatchSequenceFragment(TMatchSeq matchSeq) {
		SequenceFragment sequenceFragment = new SequenceFragment();
		sequenceFragment.setStart(BigInteger.valueOf(matchSeq.getStart()));
		sequenceFragment.setEnd(BigInteger.valueOf(matchSeq.getEnd()));
		sequenceFragment.setSequence(matchSeq.getStringValue());
		return sequenceFragment;
	}

	public SequenceFragment importQuerySequenceFragment(TQuerySeq querySeq) {
		SequenceFragment sequenceFragment = new SequenceFragment();
		sequenceFragment.setStart(BigInteger.valueOf(querySeq.getStart()));
		sequenceFragment.setEnd(BigInteger.valueOf(querySeq.getEnd()));
		sequenceFragment.setSequence(querySeq.getStringValue());
		return sequenceFragment;
	}

}
