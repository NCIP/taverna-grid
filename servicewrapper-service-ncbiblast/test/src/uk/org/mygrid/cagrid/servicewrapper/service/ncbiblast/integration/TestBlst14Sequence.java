package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.integration;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.apache.axis.AxisFault;
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
			+ ">embl|M10051|M10051 Human insulin receptor mRNA, complete cds. \n"
			+ "ggggggctgcgcggccgggtcggtgcgcacacgagaaggacgcgcggcccccagcgctct\n"
			+ "tgggggccgcctcggagcatgacccccgcgggccagcgccgcgcgcctgatccgaggaga\n"
			+ "ccccgcgctcccgcagccatgggcaccgggggccggcggggggcggcggccgcgccgctg\n"
			+ "ctggtggcggtggccgcgctgctactgggcgccgcgggccacctgtaccccggagaggtg\n"
			+ "tgtcccggcatggatatccggaacaacctcactaggttgcatgagctggagaattgctct\n"
			+ "gtcatcgaaggacacttgcagatactcttgatgttcaaaacgaggcccgaagatttccga\n"
			+ "gacctcagtttccccaaactcatcatgatcactgattacttgctgctcttccgggtctat\n"
			+ "gggctcgagagcctgaaggacctgttccccaacctcacggtcatccggggatcacgactg\n"
			+ "ttctttaactacgcgctggtcatcttcgagatggttcacctcaaggaactcggcctctac\n"
			+ "aacctgatgaacatcacccggggttctgtccgcatcgagaagaacaatgagctctgttac\n"
			+ "ttggccactatcgactggtcccgtatcctggattccgtggaggataatcacatcgtgttg\n"
			+ "aacaaagatgacaacgaggagtgtggagacatctgtccgggtaccgcgaagggcaagacc\n"
			+ "aactgccccgccaccgtcatcaacgggcagtttgtcgaacgatgttggactcatagtcac\n"
			+ "tgccagaaagtttgcccgaccatctgtaagtcacacggctgcaccgccgaaggcctctgt\n"
			+ "tgccacagcgagtgcctgggcaactgttctcagcccgacgaccccaccaagtgcgtggcc\n"
			+ "tgccgcaacttctacctggacggcaggtgtgtggagacctgcccgcccccgtactaccac\n"
			+ "ttccaggactggcgctgtgtgaacttcagcttctgccaggacctgcaccacaaatgcaag\n"
			+ "aactcgcggaggcagggctgccaccaatacgtcattcacaacaacaagtgcatccctgag\n"
			+ "tgtccctccgggtacacgatgaattccagcaacttgctgtgcaccccatgcctgggtccc\n"
			+ "tgtcccaaggtgtgccacctcctagaaggcgagaagaccatcgactcggtgacgtctgcc\n"
			+ "caggagctccgaggatgcaccgtcatcaacgggagtctgatcatcaacattcgaggaggc\n"
			+ "aacaatctggcagctgagctagaagccaacctcggcctcattgaagaaatttcagggtat\n"
			+ "ctaaaaatccgccgatcctacgctctggtgtcactttccttcttccggaagttacgtctg\n"
			+ "attcgaggagagaccttggaaattgggaactactccttctatgccttggacaaccagaac\n"
			+ "ctaaggcagctctgggactggagcaaacacaacctcaccaccactcaggggaaactcttc\n"
			+ "ttccactataaccccaaactctgcttgtcagaaatccacaagatggaagaagtttcagga\n"
			+ "accaaggggcgccaggagagaaacgacattgccctgaagaccaatggggacaaggcatcc\n"
			+ "tgtgaaaatgagttacttaaattttcttacattcggacatcttttgacaagatcttgctg\n"
			+ "agatgggagccgtactggccccccgacttccgagacctcttggggttcatgctgttctac\n"
			+ "aaagaggccccttatcagaatgtgacggagttcgatgggcaggatgcgtgtggttccaac\n"
			+ "agttggacggtggtagacattgacccacccctgaggtccaacgaccccaaatcacagaac\n"
			+ "cacccagggtggctgatgcggggtctcaagccctggacccagtatgccatctttgtgaag\n"
			+ "accctggtcaccttttcggatgaacgccggacctatggggccaagagtgacatcatttat\n"
			+ "gtccagacagatgccaccaacccctctgtgcccctggatccaatctcagtgtctaactca\n"
			+ "tcatcccagattattctgaagtggaaaccaccctccgaccccaatggcaacatcacccac\n"
			+ "tacctggttttctgggagaggcaggcggaagacagtgagctgttcgagctggattattgc\n"
			+ "ctcaaagggctgaagctgccctcgaggacctggtctccaccattcgagtctgaagattct\n"
			+ "cagaagcacaaccagagtgagtatgaggattcggccggcgaatgctgctcctgtccaaag\n"
			+ "acagactctcagatcctgaaggagctggaggagtcctcgtttaggaagacgtttgaggat\n"
			+ "tacctgcacaacgtggttttcgtccccagaaaaacctcttcaggcactggtgccgaggac\n"
			+ "cctaggccatctcggaaacgcaggtcccttggcgatgttgggaatgtgacggtggccgtg\n"
			+ "cccacggtggcagctttccccaacacttcctcgaccagcgtgcccacgagtccggaggag\n"
			+ "cacaggccttttgagaaggtggtgaacaaggagtcgctggtcatctccggcttgcgacac\n"
			+ "ttcacgggctatcgcatcgagctgcaggcttgcaaccaggacacccctgaggaacggtgc\n"
			+ "agtgtggcagcctacgtcagtgcgaggaccatgcctgaagccaaggctgatgacattgtt\n"
			+ "ggccctgtgacgcatgaaatctttgagaacaacgtcgtccacttgatgtggcaggagccg\n"
			+ "aaggagcccaatggtctgatcgtgctgtatgaagtgagttatcggcgatatggtgatgag\n"
			+ "gagctgcatctctgcgtctcccgcaagcacttcgctctggaacggggctgcaggctgcgt\n"
			+ "gggctgtcaccggggaactacagcgtgcgaatccgggccacctcccttgcgggcaacggc\n"
			+ "tcttggacggaacccacctatttctacgtgacagactatttagacgtcccgtcaaatatt\n"
			+ "gcaaaaattatcatcggccccctcatctttgtctttctcttcagtgttgtgattggaagt\n"
			+ "atttatctattcctgagaaagaggcagccagatgggccgctgggaccgctttacgcttct\n"
			+ "tcaaaccctgagtatctcagtgccagtgatgtgtttccatgctctgtgtacgtgccggac\n"
			+ "gagtgggaggtgtctcgagagaagatcaccctccttcgagagctggggcagggctccttc\n"
			+ "ggcatggtgtatgagggcaatgccagggacatcatcaagggtgaggcagagacccgcgtg\n"
			+ "gcggtgaagacggtcaacgagtcagccagtctccgagagcggattgagttcctcaatgag\n"
			+ "gcctcggtcatgaagggcttcacctgccatcacgtggtgcgcctcctgggagtggtgtcc\n"
			+ "aagggccagcccacgctggtggtgatggagctgatggctcacggagacctgaagagctac\n"
			+ "ctccgttctctgcggccagaggctgagaataatcctggccgccctccccctacccttcaa\n"
			+ "gagatgattcagatggcggcagagattgctgacgggatggcctacctgaacgccaagaag\n"
			+ "tttgtgcatcgggacctggcagcgagaaactgcatggtcgcccatgattttactgtcaaa\n"
			+ "attggagactttggaatgaccagagacatctatgaaacggattactaccggaaagggggc\n"
			+ "aagggtctgctccctgtacggtggatggcaccggagtccctgaaggatggggtcttcacc\n"
			+ "acttcttctgacatgtggtcctttggcgtggtcctttgggaaatcaccagcttggcagaa\n"
			+ "cagccttaccaaggcctgtctaatgaacaggtgttgaaatttgtcatggatggagggtat\n"
			+ "ctggatcaacccgacaactgtccagagagagtcactgacctcatgcgcatgtgctggcaa\n"
			+ "ttcaaccccaagatgaggccaaccttcctggagattgtcaacctgctcaaggacgacctg\n"
			+ "caccccagctttccagaggtgtcgttcttccacagcgaggagaacaaggctcccgagagt\n"
			+ "gaggagctggagatggagtttgaggacatggagaatgtgcccctggaccgttcctcgcac\n"
			+ "tgtcagagggaggaggcggggggccgggatggagggtcctcgctgggtttcaagcggagc\n"
			+ "tacgaggaacacatcccttacacacacatgaacggaggcaagaaaaacgggcggattctg\n"
			+ "accttgcctcggtccaatccttcctaacagtgcctaccgtggcgggggcgggcaggggtt\n"
			+ "cccattttcgctttcctctggtttgaaagcctctggaaaactcaggattctcacgactct\n"
			+ "accatgtccagtggagttcagagatcgttcctatacatttctgttcatcttaaggtggac\n"
			+ "tcgtttggttaccaatttaactagtcctgcagaggatttaactgtgaacctggagggcaa\n"
			+ "ggggtttccacagttgctgctcctttggggcaacgacggtttcaaaccaggattttgtgt\n"
			+ "tttttcgttccccccacccgcccccagcagatggaaagaaagcacctgtttttacaaatt\n"
			+ "cttttttttttttttttttttttttttttgctggtgtctgagcttcagtataaaagacaa\n"
			+ "aacttcctgtttgtggaacaaaatttcgaaagaaaaaaccaaa\n";

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
		input.setSequenceRepresentation(new NucleicAcidSequenceRepresentation());
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	
	@Test(expected = AxisFault.class)
	public void invalidFastaNucleotideSyntax() throws Exception {
		NucleicAcidSequence invalidFasta = new NucleicAcidSequence();
		invalidFasta.setValueInFastaFormat(">__123");
		NucleicAcidSequenceRepresentation sequenceRepresentation = new NucleicAcidSequenceRepresentation();
		sequenceRepresentation.setNucleicAcidSequence(invalidFasta);
		input.setSequenceRepresentation(sequenceRepresentation);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidFastaProteinSyntax() throws Exception {
		
		ProteinSequence invalidFasta = new ProteinSequence();
		invalidFasta.setValueInFastaFormat(">__123");
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		sequenceRepresentation.setProteinSequence(invalidFasta);
		input.setSequenceRepresentation(sequenceRepresentation);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidNucleotideSeqIdSyntax() throws Exception {
		
		NucleicAcidSequenceRepresentation sequenceRepresentation = new NucleicAcidSequenceRepresentation();
		GeneGenomicIdentifier nucleicDNAId = new GeneGenomicIdentifier();
		nucleicDNAId.setDataSourceName("invalid");
		nucleicDNAId.setCrossReferenceId("Syntax");
		sequenceRepresentation.setNucleicDNAId(nucleicDNAId );
		input.setSequenceRepresentation(sequenceRepresentation);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

	@Test(expected = AxisFault.class)
	public void invalidProteinSeqIdSyntax() throws Exception {
		ProteinGenomicIdentifier proteinId = new ProteinGenomicIdentifier();
		proteinId.setDataSourceName("invalid");
		proteinId.setCrossReferenceId("Syntax");
		ProteinSequenceRepresentation sequenceRepresentation = new ProteinSequenceRepresentation();
		sequenceRepresentation.setProteinId(proteinId );
		input.setSequenceRepresentation(sequenceRepresentation);
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}
	@Test(expected = AxisFault.class)
	public void invalidValue() throws Exception {
		MolecularSequenceRepresentation sequenceRepresentation = new MolecularSequenceRepresentation() {
		};;;
		input.setSequenceRepresentation(sequenceRepresentation );
		clientUtils.ncbiBlastSync(input, LONG_TIMEOUT);
	}

}
