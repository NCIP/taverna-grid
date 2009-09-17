package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import uk.ac.ebi.www.wsncbiblast.Data;
import uk.ac.ebi.www.wsncbiblast.InputParams;
import uk.org.mygrid.cagrid.domain.common.MolecularSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.MolecularSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.NucleicAcidSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.DatabaseCrossReference;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.GeneGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.MessengerRNAGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.NucleicAcidSequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.Sequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;
import uk.org.mygrid.cagrid.valuedomains.Matrix;

public class NCBIBlastExporter {

	private static Logger logger = Logger.getLogger(NCBIBlastExporter.class);

	public NCBIBlastInput exportNCBIBlastInput(uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInput nCBIBlastInput) {
		NCBIBlastInput input = new NCBIBlastInput();
		InputParams params = exportParameters(nCBIBlastInput
				.getNcbiBLASTInputParameters());
		input.setParams(params);
		input.setContent(exportContent(nCBIBlastInput
				.getSequenceRepresentation()));
		return input;
	}


	private String exportSequence(Sequence sequence) throws ConverterException {
		String fastaFormat = sequence.getValueInFastaFormat();
		String value = sequence.getValue();
		if (fastaFormat != null && fastaFormat.length() > 0) {
			return fastaFormat;
		} else if (value != null && value.length() > 0) {
			return value;
		} else {
			throw new ConverterException("No sequence value");
		}

	}

	private String exportSequenceIdentifier(DatabaseCrossReference sequenceId) {
		String id = sequenceId.getDataSourceName() + ":"
				+ sequenceId.getCrossReferenceId();
		return id;
	}
	
	private Data[] exportContent(MolecularSequenceRepresentation seqRep) {

		if (seqRep == null) {
			logger.warn("Sequence representation required");
			throw new ConverterException("Sequence representation required");
		}

		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();
		content[0].setType("sequence"); // Always 'sequence' for some reason

		if (seqRep instanceof ProteinSequenceRepresentation) {
			ProteinSequenceRepresentation proteinSeqRep = (ProteinSequenceRepresentation) seqRep;
			if (proteinSeqRep.getProteinSequence() != null) {
				ProteinSequence sequence = proteinSeqRep.getProteinSequence();
				content[0].setContent(exportSequence(sequence));

			} else if (proteinSeqRep.getProteinId() != null) {
				ProteinGenomicIdentifier proteinId = proteinSeqRep
						.getProteinId();
				content[0].setContent(exportSequenceIdentifier(proteinId));
			}
		} else if (seqRep instanceof NucleicAcidSequenceRepresentation) {
			NucleicAcidSequenceRepresentation nucleicSeqRep = (NucleicAcidSequenceRepresentation) seqRep;
			if (nucleicSeqRep != null
					&& nucleicSeqRep.getNucleicAcidSequence() != null) {
				NucleicAcidSequence sequence = nucleicSeqRep
						.getNucleicAcidSequence();
				content[0].setContent(exportSequence(sequence));
			} else if (nucleicSeqRep != null
					&& nucleicSeqRep.getNucleicDNAId() != null) {
				GeneGenomicIdentifier nucleicDNAId = nucleicSeqRep
						.getNucleicDNAId();
				content[0].setContent(exportSequenceIdentifier(nucleicDNAId));
			} else if (nucleicSeqRep != null
					&& nucleicSeqRep.getNucleicRNAId() != null) {
				MessengerRNAGenomicIdentifier nucleicRNAId = nucleicSeqRep
						.getNucleicRNAId();
				content[0].setContent(exportSequenceIdentifier(nucleicRNAId));
			}
		} else {
			logger.warn("Unsupported sequence representation type "
					+ seqRep.getClass());
			throw new ConverterException(
					"Unsupported sequence representation type "
							+ seqRep.getClass());
		}

		if (content[0].getContent() == null
				|| content[0].getContent().length() == 0) {
			logger.warn("Unsupported sequence representation "
					+ seqRep.getClass());
			throw new ConverterException("Unsupported sequence representation "
					+ seqRep.getClass());
		}

		return content;
	}

	private InputParams exportParameters(NCBIBlastInputParameters ncbiBlastInputParameters) {
		InputParams params = InputParams.Factory.newInstance();
		// TODO: HAndle defaults/null values
		params.setAsync(true);

		BigInteger alignmentsToOutput = ncbiBlastInputParameters.getAlignmentsToOutput();
		if (alignmentsToOutput != null) {
			params.setNumal(alignmentsToOutput.intValue());
		}

		BLASTProgram blastProgram = ncbiBlastInputParameters.getBlastProgram();
		if (blastProgram == null) {
			throw new ConverterException("Parameter BlastProgram is required");
		}
		params.setProgram(blastProgram.getValue().toLowerCase());

		MolecularSequenceDatabase database = ncbiBlastInputParameters.getQueryDatabase();
		if (database == null || database.getName() == null
				|| database.getName().length() < 1) {
			throw new ConverterException("Required parameter 'database' with 'name' element");
		}
		params.setDatabase(database.getName());

		String email = ncbiBlastInputParameters.getEmail();
		if (email == null || email.length() < 1) {
			throw new ConverterException("Parameter 'email' is required");
		}
		params.setEmail(email);

		BigInteger dropoff = ncbiBlastInputParameters.getDropoff();
		if (dropoff != null) {
			params.setDropoff(dropoff.intValue());
		}

		Double expectedThreshold = ncbiBlastInputParameters.getExpectedThreshold();
		if (expectedThreshold != null) {
			params.setExp(expectedThreshold.floatValue());
		}

		BigInteger extendGap = ncbiBlastInputParameters.getExtendGap();
		if (extendGap != null) {
			params.setExtendgap(extendGap.intValue());
		}

		Boolean filter = ncbiBlastInputParameters.getFilter();
		if (filter != null) {
			params.setFilter(filter.toString());
		}

		Boolean gapAlignment = ncbiBlastInputParameters.getGapAlignment();
		if (gapAlignment != null) {
			params.setGapalign(gapAlignment.toString());
		}

		BigInteger match = ncbiBlastInputParameters.getMatch();
		if (match != null) {
			params.setMatch(match.intValue());
		}

		Matrix matrix = ncbiBlastInputParameters.getMatrix();
		if (matrix != null) {
			params.setMatrix(matrix.getValue());
		}

		BigInteger maxScores = ncbiBlastInputParameters.getMaxScores();
		if (maxScores != null) {
			params.setScores(maxScores.intValue());
		}

		BigInteger mismatch = ncbiBlastInputParameters.getMismatch();
		if (mismatch != null) {
			params.setMismatch(mismatch.intValue());
		}

		BigInteger openGap = ncbiBlastInputParameters.getOpenGap();
		if (openGap != null) {
			params.setOpengap(openGap.intValue());
		}

		// TODO: params.setAlign(?)
		return params;
	}


}
