package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.jdom.Document;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.ncbiblast.Alignment;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceFragment;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.DummyNCBIBlastInvoker;

public class TestNCBIBlastConverter {
	
	private Document original;
	private NCBIBlastConverter converter;
	private NCBIBLASTOutput converted;


	@Before
	public void prepare() throws Exception {		
		DummyNCBIBlastInvoker invoker = new DummyNCBIBlastInvoker();
		original = invoker.getDummyOutput();
		converter = new NCBIBlastConverter();
		converted = converter.convertNCBIBlastOutput(original);
	}
	
	@Test
	public void totalHits() throws Exception {
		assertEquals(50, converted.getSequenceSimilarities().length);
	}

	@Test
	public void hitAttributes() throws Exception {
		// <hit number="1" database="uniprot" id="WAP_RAT" ac="P01174" length="137" 
		// description="Whey acidic protein OS=Rattus norvegicus GN=Wap PE=1 SV=2">
		SequenceSimilarity hit1 = converted.getSequenceSimilarities()[0];
		assertEquals("Invalid database name", "uniprot", hit1.getDatabase().getName());
		assertEquals("Invalid identifier", "WAP_RAT", hit1.getId());
		assertEquals("Invalid accession number", "P01174", hit1.getAccessionNumber());
		assertEquals("Invalid sequence length", BigInteger.valueOf(137), hit1.getSequenceLength());
		assertEquals("Invalid description", "Whey acidic protein OS=Rattus norvegicus GN=Wap PE=1 SV=2",hit1.getDescription());
	}
	
	@Test
	public void alignment() throws Exception {

//		<hit number="2" database="uniprot" id="Q3UQ94_MOUSE" ac="Q3UQ94" length="140" description="Putative uncharacterized protein OS=Mus musculus GN=Wap PE=2 SV=1">
//		<alignments total="1">
//			<alignment number="1">
//				<score>465</score>
//				<bits>183</bits>
//				<expectation>4e-45</expectation>
//				<identity>66</identity>
//				<positives>75</positives>
//				<querySeq start="1" end="134">MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMCCPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAA-GPCPKDNPCSIDSDCSGTMKCCKNGCIMSCMDPEPKSPTVI</querySeq>
//				<pattern>MRC ISLVLGLLALEVALA+NL+E VFNSVQSM    S  E TECI CQTNEECAQN MCCP SCGR+ KTPVNI V KAG CPWN +QMI++ GPCP    CS D +CSG MKCC   C+M+C  P P+  ++I</pattern>
//				<matchSeq start="1" end="135">MRCLISLVLGLLALEVALAQNLEEQVFNSVQSMFPKASPIEGTECIICQTNEECAQNAMCCPGSCGRTRKTPVNIGVPKAGFCPWNLLQMISSTGPCPMKIECSSDRECSGNMKCCNVDCVMTCTPPVPEVWSII</matchSeq>
//			</alignment>
//		</alignments>
//	</hit>
		
		SequenceSimilarity hit2 = converted.getSequenceSimilarities(1);
		assertEquals("Invalid identifier", "Q3UQ94_MOUSE", hit2.getId());
		Alignment alignment = hit2.getAlignments(0);
		assertEquals("Invalid score", 465, alignment.getScore(), 0.1);
		assertEquals("Invalid bits", 183, alignment.getBits(), 0.1);
		assertEquals("Invalid expectation", 4e-45, alignment.getEValue(), 1e-43);
		assertEquals("Invalid identity", BigInteger.valueOf(66), alignment.getIdentity());
		assertEquals("Invalid positives", BigInteger.valueOf(75), alignment.getPositives());

		SequenceFragment querySequenceFragment = alignment.getQuerySequenceFragment();
		assertEquals(BigInteger.valueOf(1), querySequenceFragment.getStart());
		assertEquals(BigInteger.valueOf(134), querySequenceFragment.getEnd());
		assertEquals("MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMCCPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAA-GPCPKDNPCSIDSDCSGTMKCCKNGCIMSCMDPEPKSPTVI", querySequenceFragment.getSequence());

		assertEquals("MRC ISLVLGLLALEVALA+NL+E VFNSVQSM    S  E TECI CQTNEECAQN MCCP SCGR+ KTPVNI V KAG CPWN +QMI++ GPCP    CS D +CSG MKCC   C+M+C  P P+  ++I", alignment.getSequenceSimilarityPattern());
		
		SequenceFragment matchSequenceFragment = alignment.getMatchSequenceFragment();
		assertEquals(BigInteger.valueOf(1), matchSequenceFragment.getStart());
		assertEquals(BigInteger.valueOf(135), matchSequenceFragment.getEnd());
		assertEquals("MRCLISLVLGLLALEVALAQNLEEQVFNSVQSMFPKASPIEGTECIICQTNEECAQNAMCCPGSCGRTRKTPVNIGVPKAGFCPWNLLQMISSTGPCPMKIECSSDRECSGNMKCCNVDCVMTCTPPVPEVWSII", matchSequenceFragment.getSequence());
	}
	@Test
	
	public void multipleAlignments() throws Exception {

//		<hit number="8" database="uniprot" id="Q8JIP6_TRIHK" ac="Q8JIP6" length="305" description="Chorionic proteinase inhibitor OS=Tribolodon hakonensis GN=TribSPI PE=2 SV=1">
//		<alignments total="5">	
// ..
//			<alignment number="2">
//				<score>160</score>
//				<bits>66.2</bits>
// ..
//			<alignment number="5">
//				<score>97</score>
//				<bits>42.0</bits>
//				<expectation>0.017</expectation>
//				<identity>38</identity>
//				<positives>49</positives>
//				<querySeq start="81" end="133">GRCPWNPIQMIAAGPCPKDN--PCSIDSDCSGTMKCCKNGCIMSCMDPEPKSPTV</querySeq>
//				<pattern>G CP    ++   G C +     C+ DSDC+   KCC NGC + CM P    P V</pattern>
//				<matchSeq start="32" end="83">GVCPSRTYEL---GMCARIRFVSCADDSDCANNEKCCSNGCGLQCMAPVTVKPGV</matchSeq>
//			</alignment>
//		</alignments>
//	</hit>
		
		
		SequenceSimilarity hit8 = converted.getSequenceSimilarities(7);
		assertEquals("Invalid identifier", "Q8JIP6_TRIHK", hit8.getId());
		assertEquals("Unexpected number of alignments", 5, hit8.getAlignments().length);

		Alignment alignment2 = hit8.getAlignments(1);
		assertEquals("Invalid score", 160, alignment2.getScore(), 0.1);
		assertEquals("Invalid bits", 66.2, alignment2.getBits(), 0.1);

		
		Alignment alignment5 = hit8.getAlignments(4);
		assertEquals("Invalid score", 97, alignment5.getScore(), 0.1);
		assertEquals("Invalid bits", 42.0, alignment5.getBits(), 0.1);
	}

	
}
