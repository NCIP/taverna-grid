package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.axis.AxisFault;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.MolecularSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.NucleicAcidSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastOutput;
import uk.org.mygrid.cagrid.domain.ncbiblast.SequenceSimilarity;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.GeneGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.NucleicAcidSequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;

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

	private static final String NUCLEOTIDE_DB = "EM_HUM";
	private static final String NUCLEOTIDE_CROSSREF = "M10051";
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

	@Test
	public void compareIdFastaM10051() throws Exception {
		params.setBlastProgram(BLASTProgram.BLASTN);
		params.setQueryDatabase(NUCLEOTIDE_DATABASE);
		params.setAlignmentsToOutput(BigInteger.ONE);
		
		GeneGenomicIdentifier nucleicDNAId = new GeneGenomicIdentifier();
		NucleicAcidSequenceRepresentation sequenceRepresentation = new NucleicAcidSequenceRepresentation();
		nucleicDNAId.setDataSourceName(NUCLEOTIDE_DB);
		nucleicDNAId.setCrossReferenceId(NUCLEOTIDE_CROSSREF);
		sequenceRepresentation.setNucleicDNAId(nucleicDNAId);
		input.setSequenceRepresentation(sequenceRepresentation);
		
		NCBIBlastOutput idOut = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);

		NucleicAcidSequence nucleicAcidSequence = new NucleicAcidSequence();
		nucleicAcidSequence.setValueInFastaFormat(NUCLEOTIDE_FASTA);
		sequenceRepresentation = new NucleicAcidSequenceRepresentation();
		sequenceRepresentation.setNucleicAcidSequence(nucleicAcidSequence);
		input.setSequenceRepresentation(sequenceRepresentation);
		
		NCBIBlastOutput fastaOut = clientUtils.ncbiBlastSync(input,
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
		
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinGenomicIdentifier proteinId = new ProteinGenomicIdentifier();
		proteinId.setDataSourceName("uniprot");
		proteinId.setCrossReferenceId("WAP_RAT");
		sequenceRepresentation.setProteinId(proteinId);
		input.setSequenceRepresentation(sequenceRepresentation );
		NCBIBlastOutput idOut = clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
		
		sequenceRepresentation = new ProteinSequenceRepresentation();
		ProteinSequence proteinSequence = new ProteinSequence();
		sequenceRepresentation.setProteinSequence(proteinSequence);
		proteinSequence.setValueInFastaFormat(
						">sp|P01174|WAP_RAT Whey acidic protein;\n"
								+ "MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMC\n"
								+ "CPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAAGPCPKDNPCSIDSDCSGTMKCCKNGCI\n"
								+ "MSCMDPEPKSPTVISFQ");
		input.setSequenceRepresentation(sequenceRepresentation);
		
		NCBIBlastOutput fastaOut = clientUtils.ncbiBlastSync(input,
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
		input.setSequenceRepresentation(null);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void emptyProteinFails() throws Exception {
		input.setSequenceRepresentation(new ProteinSequenceRepresentation());
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}
	

	@Test(expected = AxisFault.class)
	public void emptyNucleotideFails() throws Exception {
		input.setSequenceRepresentation(new NucleicAcidSequenceRepresentation()));
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
