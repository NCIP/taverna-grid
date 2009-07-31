package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import uk.ac.ebi.www.wsncbiblast.Data;
import uk.ac.ebi.www.wsncbiblast.InputParams;
import uk.org.mygrid.cagrid.domain.common.FASTANucleotideSequence;
import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.SequenceRepresentation;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.valuedomains.BLASTProgram;
import uk.org.mygrid.cagrid.valuedomains.Matrix;

public class NCBIBlastExporter {

	private static Logger logger = Logger.getLogger(NCBIBlastExporter.class);

	public NCBIBlastInput exportNCBIBlastInput(NCBIBLASTInput nCBIBlastInput) {
		NCBIBlastInput input = new NCBIBlastInput();
		InputParams params = exportParameters(nCBIBlastInput
				.getNCBIBLASTInputParameters());
		input.setParams(params);
		input.setContent(exportContent(nCBIBlastInput
				.getProteinOrNucleotideSequenceRepresentation()));
		return input;
	}

	private Data[] exportContent(SequenceRepresentation seqRep) {

		if (seqRep == null) {
			logger.warn("Sequence representation required");
			throw new ConverterException("Sequence representation required");
		}
		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();

		if (seqRep instanceof FASTANucleotideSequence) {
			content[0].setContent(((FASTANucleotideSequence) seqRep)
					.getSequence());
			content[0].setType("sequence");
		} else if (seqRep instanceof FASTAProteinSequence) {
			content[0]
					.setContent(((FASTAProteinSequence) seqRep).getSequence());
			content[0].setType("sequence");
		} else if (seqRep instanceof ProteinSequenceIdentifier) {
			content[0].setContent(((ProteinSequenceIdentifier) seqRep)
					.getSequenceId());
			content[0].setType("sequence"); // oddly enough..
		} else if (seqRep instanceof NucleotideSequenceIdentifier) {
			content[0].setContent(((NucleotideSequenceIdentifier) seqRep)
					.getSequenceId());
			content[0].setType("sequence"); // oddly enough..
		} else {
			logger.warn("Unsupported sequence representation type "
					+ seqRep.getClass());
			throw new ConverterException(
					"Unsupported sequence representation type "
							+ seqRep.getClass());
		}
		return content;
	}

	private InputParams exportParameters(NCBIBLASTInputParameters inputParams) {
		InputParams params = InputParams.Factory.newInstance();
		// TODO: HAndle defaults/null values
		params.setAsync(true);

		BigInteger alignmentsToOutput = inputParams.getAlignmentsToOutput();
		if (alignmentsToOutput != null) {
			params.setAlign(alignmentsToOutput.intValue());
		}

		BLASTProgram blastProgram = inputParams.getBlastProgram();
		if (blastProgram == null) {
			throw new ConverterException("Parameter BlastProgram is required");
		}
		params.setProgram(blastProgram.getValue().toLowerCase());

		String databaseName = inputParams.getDatabaseName();
		if (databaseName == null || databaseName.isEmpty()) {
			throw new ConverterException("Parameter DatabaseName is required");
		}
		params.setDatabase(databaseName);

		String email = inputParams.getEmail();
		if (email == null || email.isEmpty()) {
			throw new ConverterException("Parameter Email is required");
		}
		params.setEmail(email);

		BigInteger dropoff = inputParams.getDropoff();
		if (dropoff != null) {
			params.setDropoff(dropoff.intValue());
		}

		Double expectedThreshold = inputParams.getExpectedThreshold();
		if (expectedThreshold != null) {
			params.setExp(expectedThreshold.floatValue());
		}
		
		BigInteger extendGap = inputParams.getExtendGap();
		if (extendGap != null) {
			params.setExtendgap(extendGap.intValue());
		}

		Boolean filter = inputParams.getFilter();
		if (filter != null) {
			params.setFilter(filter.toString());
		}

		Boolean gapAlignment = inputParams.getGapAlignment();
		if (gapAlignment != null) {
			params.setGapalign(gapAlignment.toString());
		}

		BigInteger match = inputParams.getMatch();
		if (match != null) {
			params.setMatch(match.intValue());
		}

		Matrix matrix = inputParams.getMatrix();
		if (matrix != null) {
			params.setMatrix(matrix.getValue());
		}

		BigInteger maxScores = inputParams.getMaxScores();
		if (maxScores != null) {
			params.setScores(maxScores.intValue());
		}

		BigInteger mismatch = inputParams.getMismatch();
		if (mismatch != null) {
			params.setMismatch(mismatch.intValue());
		}

		BigInteger openGap = inputParams.getOpenGap();
		if (openGap != null) {
			params.setOpengap(openGap.intValue());
		}
		
		// TODO: params.setNumal(?)
		return params;
	}

}
