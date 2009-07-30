package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import org.apache.log4j.Logger;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.domain.common.FASTANucleotideSequence;
import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.SequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

public class InterProScanExporter {

	private static Logger logger = Logger.getLogger(InterProScanExporter.class);

	public InterProScanInput exportInterProScanInput(
			uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput interProScanInput)
			throws ConverterException {

		InterProScanInputParameters origParams = interProScanInput
				.getInterProScanInputParameters();
		uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput input = new uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput();
		InputParams params = exportParameters(origParams);
		input.setParams(params);
		Data[] content = exportContent(interProScanInput
				.getSequenceRepresentation(), params);
		input.setContent(content);
		return input;
	}

	public Data[] exportContent(SequenceRepresentation seqRep,
			InputParams params) {
		if (seqRep == null) {
			logger.warn("Sequence representation required");
			throw new ConverterException("Sequence representation required");
		}
		if (seqRep instanceof ProteinSequenceRepresentation) {
			params.setSeqtype("P");
		} else if (seqRep instanceof NucleotideSequenceRepresentation) {
			params.setSeqtype("N");
		} else {
			logger.warn("Unsupported sequence representation type "
					+ seqRep.getClass());
			throw new ConverterException(
					"Unsupported sequence representation type "
							+ seqRep.getClass());
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

	public InputParams exportParameters(InterProScanInputParameters origParams) {
		InputParams params = uk.ac.ebi.www.wsinterproscan.InputParams.Factory
				.newInstance();
		if (params == null) {
			logger.warn("Parameters required");
			throw new ConverterException("Parameters required");
		}
		if (origParams.getEmail() == null) {
			logger.warn("Email required");
			throw new ConverterException("Email required");
		}
		params.setEmail(origParams.getEmail());
		if (origParams.getUseCRC() != null) {
			params.setCrc(origParams.getUseCRC());
		}
		StringBuffer apps = new StringBuffer();
		if (origParams.getSignatureMethod() != null) {
			for (SignatureMethod signatureMethod : origParams
					.getSignatureMethod()) {
				if (apps.length() > 0) {
					apps.append(' ');
				}
				apps.append(signatureMethod.getValue());
			}
		}
		if (apps.length() > 0) {
			params.setApp(apps.toString());
		}
		return params;
	}

}
