package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import org.apache.log4j.Logger;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.domain.common.MolecularSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.NucleicAcidSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.DatabaseCrossReference;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.GeneGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.MessengerRNAGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.NucleicAcidSequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.Sequence;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
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

	public Data[] exportContent(MolecularSequenceRepresentation seqRep,
			InputParams params) throws ConverterException {
		if (seqRep == null) {
			logger.warn("Sequence representation required");
			throw new ConverterException("Sequence representation required");
		}

		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();
		content[0].setType("sequence"); // Always 'sequence' for some reason

		if (seqRep instanceof ProteinSequenceRepresentation) {
			ProteinSequenceRepresentation proteinSeqRep = (ProteinSequenceRepresentation) seqRep;
			params.setSeqtype("P");
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
			params.setSeqtype("N");
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
		if (origParams.getSignatureMethods() != null) {
			for (SignatureMethod signatureMethod : origParams
					.getSignatureMethods()) {
				if (apps.length() > 0) {
					apps.append(' ');
				}
				apps.append(exportSignatureMethod(signatureMethod));
			}
		}
		if (apps.length() > 0) {
			params.setApp(apps.toString());
		}
		return params;
	}

	private String exportSignatureMethod(SignatureMethod signatureMethod) {
		if (signatureMethod.equals(SignatureMethod.SignalPHMM)) {
			return "signalp";
		} else if (signatureMethod.equals(SignatureMethod.SuperFamily)) {
			return "superfamily";
		}
		return signatureMethod.getValue();
	}

}
