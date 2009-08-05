package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.axis.AxisFault;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.FASTANucleotideSequence;
import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.SequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;

/**
 * BLST14: protein or nucleotide sequence parameter or its 'database:identifier'
 * 
 * Default: No default
 * 
 * Test empty/null: Should produce error
 * 
 * Test invalid value: Should produce error
 * 
 * Additional tests: Pass the same protein/nucleotide both as a sequence and as
 * an identifier in a protein/nucleotide database and verify that the returned
 * hits are identical. For the the test protein we will use the whey acidic
 * protein found in rats, whose database identifier is uniprot:wap_rat and FASTA
 * sequence is:
 * 
 * From
 * http://www.ebi.ac.uk/Tools/webservices/rest/dbfetch/uniprot/WAP_RAT/fasta
 * 
 * >sp|P01174|WAP_RAT Whey acidic protein;
 * MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMC
 * CPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAAGPCPKDNPCSIDSDCSGTMKCCKNGCI
 * MSCMDPEPKSPTVISFQ
 * 
 * 
 * For the test nucleotide, we can use INSR insulin receptor in humans, whose
 * database identifier is (???) and partial FASTA sequence is:
 * 
 * >gi|210032107|ref|NG_008852.1| Homo sapiens insulin receptor (INSR) on
 * chromosome 19
 * CCTGCTGTTGACCTGAAGACTTAGCAATAACATAAACAGCCGATTAACAGGCATTTTGTATGTTACAAGT
 * ATTACATACTGTATTTCTTTCTTTCTTACTTTATTATTATTATTATTTTGAGATAGAGTCTTACTGCATC
 * ACCTAGGCTGGAGTGCGCTAGTGGCATGATCTTGGCTCACTGCAACCTCCGCCTGTTAGGTTCAAGTAAT
 * TCTCATGCCTCAGCCCTCCAGAGTAGCTGGAATTACAAGCACACCACGCCTGGCTAATTTTTGTATTTTT
 * AGTAAATATGGGGTTTTGTAATGTTGGCCAGGCTGGTCTCGAACTCCTGACCTCAAGTGATCCGCCTCCC
 * TCTGTCTTGCAAAGTGCTGGGATTACAGGAGTGAACCACTGTGCCCAGCCTCTACACACTACAGTCTTAG
 * AATAAAGCAAGTTAGAGAAGAGAAAATGTTATTTAAAAAATCACAGCTGGGCATGGTGTCATGTGGCTG (..)
 * 
 * See the full sequence <a href=
 * "http://www.ncbi.nlm.nih.gov/nuccore/210032107?report=fasta&log$=seqview"
 * >here</a>.
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestBlst14Sequence extends CommonTest {

	String NUCLEOTIDE_FASTA = ""
			+ ">embl|M10051|M10051 Human insulin receptor mRNA, complete cds. "
			+ "ggggggctgcgcggccgggtcggtgcgcacacgagaaggacgcgcggcccccagcgctct"
			+ "tgggggccgcctcggagcatgacccccgcgggccagcgccgcgcgcctgatccgaggaga"
			+ "ccccgcgctcccgcagccatgggcaccgggggccggcggggggcggcggccgcgccgctg"
			+ "ctggtggcggtggccgcgctgctactgggcgccgcgggccacctgtaccccggagaggtg"
			+ "tgtcccggcatggatatccggaacaacctcactaggttgcatgagctggagaattgctct"
			+ "gtcatcgaaggacacttgcagatactcttgatgttcaaaacgaggcccgaagatttccga"
			+ "gacctcagtttccccaaactcatcatgatcactgattacttgctgctcttccgggtctat"
			+ "gggctcgagagcctgaaggacctgttccccaacctcacggtcatccggggatcacgactg"
			+ "ttctttaactacgcgctggtcatcttcgagatggttcacctcaaggaactcggcctctac"
			+ "aacctgatgaacatcacccggggttctgtccgcatcgagaagaacaatgagctctgttac"
			+ "ttggccactatcgactggtcccgtatcctggattccgtggaggataatcacatcgtgttg"
			+ "aacaaagatgacaacgaggagtgtggagacatctgtccgggtaccgcgaagggcaagacc"
			+ "aactgccccgccaccgtcatcaacgggcagtttgtcgaacgatgttggactcatagtcac"
			+ "tgccagaaagtttgcccgaccatctgtaagtcacacggctgcaccgccgaaggcctctgt"
			+ "tgccacagcgagtgcctgggcaactgttctcagcccgacgaccccaccaagtgcgtggcc"
			+ "tgccgcaacttctacctggacggcaggtgtgtggagacctgcccgcccccgtactaccac"
			+ "ttccaggactggcgctgtgtgaacttcagcttctgccaggacctgcaccacaaatgcaag"
			+ "aactcgcggaggcagggctgccaccaatacgtcattcacaacaacaagtgcatccctgag"
			+ "tgtccctccgggtacacgatgaattccagcaacttgctgtgcaccccatgcctgggtccc"
			+ "tgtcccaaggtgtgccacctcctagaaggcgagaagaccatcgactcggtgacgtctgcc"
			+ "caggagctccgaggatgcaccgtcatcaacgggagtctgatcatcaacattcgaggaggc"
			+ "aacaatctggcagctgagctagaagccaacctcggcctcattgaagaaatttcagggtat"
			+ "ctaaaaatccgccgatcctacgctctggtgtcactttccttcttccggaagttacgtctg"
			+ "attcgaggagagaccttggaaattgggaactactccttctatgccttggacaaccagaac"
			+ "ctaaggcagctctgggactggagcaaacacaacctcaccaccactcaggggaaactcttc"
			+ "ttccactataaccccaaactctgcttgtcagaaatccacaagatggaagaagtttcagga"
			+ "accaaggggcgccaggagagaaacgacattgccctgaagaccaatggggacaaggcatcc"
			+ "tgtgaaaatgagttacttaaattttcttacattcggacatcttttgacaagatcttgctg"
			+ "agatgggagccgtactggccccccgacttccgagacctcttggggttcatgctgttctac"
			+ "aaagaggccccttatcagaatgtgacggagttcgatgggcaggatgcgtgtggttccaac"
			+ "agttggacggtggtagacattgacccacccctgaggtccaacgaccccaaatcacagaac"
			+ "cacccagggtggctgatgcggggtctcaagccctggacccagtatgccatctttgtgaag"
			+ "accctggtcaccttttcggatgaacgccggacctatggggccaagagtgacatcatttat"
			+ "gtccagacagatgccaccaacccctctgtgcccctggatccaatctcagtgtctaactca"
			+ "tcatcccagattattctgaagtggaaaccaccctccgaccccaatggcaacatcacccac"
			+ "tacctggttttctgggagaggcaggcggaagacagtgagctgttcgagctggattattgc"
			+ "ctcaaagggctgaagctgccctcgaggacctggtctccaccattcgagtctgaagattct"
			+ "cagaagcacaaccagagtgagtatgaggattcggccggcgaatgctgctcctgtccaaag"
			+ "acagactctcagatcctgaaggagctggaggagtcctcgtttaggaagacgtttgaggat"
			+ "tacctgcacaacgtggttttcgtccccagaaaaacctcttcaggcactggtgccgaggac"
			+ "cctaggccatctcggaaacgcaggtcccttggcgatgttgggaatgtgacggtggccgtg"
			+ "cccacggtggcagctttccccaacacttcctcgaccagcgtgcccacgagtccggaggag"
			+ "cacaggccttttgagaaggtggtgaacaaggagtcgctggtcatctccggcttgcgacac"
			+ "ttcacgggctatcgcatcgagctgcaggcttgcaaccaggacacccctgaggaacggtgc"
			+ "agtgtggcagcctacgtcagtgcgaggaccatgcctgaagccaaggctgatgacattgtt"
			+ "ggccctgtgacgcatgaaatctttgagaacaacgtcgtccacttgatgtggcaggagccg"
			+ "aaggagcccaatggtctgatcgtgctgtatgaagtgagttatcggcgatatggtgatgag"
			+ "gagctgcatctctgcgtctcccgcaagcacttcgctctggaacggggctgcaggctgcgt"
			+ "gggctgtcaccggggaactacagcgtgcgaatccgggccacctcccttgcgggcaacggc"
			+ "tcttggacggaacccacctatttctacgtgacagactatttagacgtcccgtcaaatatt"
			+ "gcaaaaattatcatcggccccctcatctttgtctttctcttcagtgttgtgattggaagt"
			+ "atttatctattcctgagaaagaggcagccagatgggccgctgggaccgctttacgcttct"
			+ "tcaaaccctgagtatctcagtgccagtgatgtgtttccatgctctgtgtacgtgccggac"
			+ "gagtgggaggtgtctcgagagaagatcaccctccttcgagagctggggcagggctccttc"
			+ "ggcatggtgtatgagggcaatgccagggacatcatcaagggtgaggcagagacccgcgtg"
			+ "gcggtgaagacggtcaacgagtcagccagtctccgagagcggattgagttcctcaatgag"
			+ "gcctcggtcatgaagggcttcacctgccatcacgtggtgcgcctcctgggagtggtgtcc"
			+ "aagggccagcccacgctggtggtgatggagctgatggctcacggagacctgaagagctac"
			+ "ctccgttctctgcggccagaggctgagaataatcctggccgccctccccctacccttcaa"
			+ "gagatgattcagatggcggcagagattgctgacgggatggcctacctgaacgccaagaag"
			+ "tttgtgcatcgggacctggcagcgagaaactgcatggtcgcccatgattttactgtcaaa"
			+ "attggagactttggaatgaccagagacatctatgaaacggattactaccggaaagggggc"
			+ "aagggtctgctccctgtacggtggatggcaccggagtccctgaaggatggggtcttcacc"
			+ "acttcttctgacatgtggtcctttggcgtggtcctttgggaaatcaccagcttggcagaa"
			+ "cagccttaccaaggcctgtctaatgaacaggtgttgaaatttgtcatggatggagggtat"
			+ "ctggatcaacccgacaactgtccagagagagtcactgacctcatgcgcatgtgctggcaa"
			+ "ttcaaccccaagatgaggccaaccttcctggagattgtcaacctgctcaaggacgacctg"
			+ "caccccagctttccagaggtgtcgttcttccacagcgaggagaacaaggctcccgagagt"
			+ "gaggagctggagatggagtttgaggacatggagaatgtgcccctggaccgttcctcgcac"
			+ "tgtcagagggaggaggcggggggccgggatggagggtcctcgctgggtttcaagcggagc"
			+ "tacgaggaacacatcccttacacacacatgaacggaggcaagaaaaacgggcggattctg"
			+ "accttgcctcggtccaatccttcctaacagtgcctaccgtggcgggggcgggcaggggtt"
			+ "cccattttcgctttcctctggtttgaaagcctctggaaaactcaggattctcacgactct"
			+ "accatgtccagtggagttcagagatcgttcctatacatttctgttcatcttaaggtggac"
			+ "tcgtttggttaccaatttaactagtcctgcagaggatttaactgtgaacctggagggcaa"
			+ "ggggtttccacagttgctgctcctttggggcaacgacggtttcaaaccaggattttgtgt"
			+ "tttttcgttccccccacccgcccccagcagatggaaagaaagcacctgtttttacaaatt"
			+ "cttttttttttttttttttttttttttttgctggtgtctgagcttcagtataaaagacaa"
			+ "aacttcctgtttgtggaacaaaatttcgaaagaaaaaaccaaa";

	String NUCLEOTIDE_ID = "EM_HUM:M10051";

	@Test
	public void compareIdFastaM10051() throws Exception {
		setNucleotideParams();
		input
				.setProteinOrNucleotideSequenceRepresentation(new ProteinSequenceIdentifier(
						NUCLEOTIDE_ID));
		NCBIBLASTOutput idOut = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);

		input
				.setProteinOrNucleotideSequenceRepresentation(new FASTAProteinSequence(
						NUCLEOTIDE_FASTA));
		NCBIBLASTOutput fastaOut = clientUtils.ncbiBlastSync(input,
				LONG_TIMEOUT);

		SequenceSimilarity[] idSims = idOut.getSequenceSimilarities();
		SequenceSimilarity[] fastaSims = fastaOut.getSequenceSimilarities();

		assertEquals("Expected same number of similarities", fastaSims.length,
				idSims.length);
		for (int similarity = 0; similarity < fastaSims.length; similarity++) {
			SequenceSimilarity idSim = idSims[similarity];
			SequenceSimilarity fastaSim = fastaSims[similarity];
			assertEquals("Did not match accession number for similarity "
					+ similarity, idSim.getAccessionNumber(), fastaSim
					.getAccessionNumber());
			assertEquals("Similarity patterns did not match for similarity "
					+ similarity, idSim.getAlignments(0)
					.getSequenceSimilarityPattern(), fastaSim.getAlignments(0)
					.getSequenceSimilarityPattern());
		}

	}

	@Test
	public void compareIdFastaWapRat() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new ProteinSequenceIdentifier(
						"uniprot:WAP_RAT"));
		NCBIBLASTOutput idOut = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		input
				.setProteinOrNucleotideSequenceRepresentation(new FASTAProteinSequence(
						">sp|P01174|WAP_RAT Whey acidic protein;\n"
								+ "MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMC\n"
								+ "CPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAAGPCPKDNPCSIDSDCSGTMKCCKNGCI\n"
								+ "MSCMDPEPKSPTVISFQ"));
		NCBIBLASTOutput fastaOut = clientUtils.ncbiBlastSync(input,
				LONG_TIMEOUT);

		SequenceSimilarity[] idSims = idOut.getSequenceSimilarities();
		SequenceSimilarity[] fastaSims = fastaOut.getSequenceSimilarities();

		assertEquals("Expected same number of similarities", fastaSims.length,
				idSims.length);
		for (int similarity = 0; similarity < fastaSims.length; similarity++) {
			SequenceSimilarity idSim = idSims[similarity];
			SequenceSimilarity fastaSim = fastaSims[similarity];
			assertEquals("Did not match accession number for similarity "
					+ similarity, idSim.getAccessionNumber(), fastaSim
					.getAccessionNumber());
			assertEquals("Similarity patterns did not match for similarity "
					+ similarity, idSim.getAlignments(0)
					.getSequenceSimilarityPattern(), fastaSim.getAlignments(0)
					.getSequenceSimilarityPattern());
		}
	}

	@Test(expected = AxisFault.class)
	public void defaultFails() throws Exception {
		input.setProteinOrNucleotideSequenceRepresentation(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidFastaNucleotideSyntax() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new FASTANucleotideSequence(
						">__123"));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidFastaProteinSyntax() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new FASTAProteinSequence(
						">__123"));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidNucleotideSeqIdSyntax() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new NucleotideSequenceIdentifier(
						"invalid:Syntax"));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidProteinSeqIdSyntax() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new ProteinSequenceIdentifier(
						"invalid:Syntax"));
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}
	@Test(expected = AxisFault.class)
	public void invalidValue() throws Exception {
		input
				.setProteinOrNucleotideSequenceRepresentation(new SequenceRepresentation() {
				});
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

}
