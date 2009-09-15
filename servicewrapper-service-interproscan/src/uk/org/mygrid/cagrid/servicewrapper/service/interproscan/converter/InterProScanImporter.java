package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

import uk.ac.ebi.schema.TClassification;
import uk.ac.ebi.schema.EBIInterProScanResultsDocument;
import uk.ac.ebi.schema.TInterPro;
import uk.ac.ebi.schema.TMatch;
import uk.ac.ebi.schema.TProtein;
import uk.ac.ebi.schema.TMatch.Location;
import uk.org.mygrid.cagrid.domain.common.MolecularSequenceDatabase;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.Classification;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomain;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainIdentifier;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainLocationStatistics;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainMatch;
import uk.org.mygrid.cagrid.servicewrapper.imported.irwg.ProteinGenomicIdentifier;
import uk.org.mygrid.cagrid.servicewrapper.imported.pir.ProteinSequence;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;
import uk.org.mygrid.cagrid.valuedomains.SignatureStatus;

public class InterProScanImporter {

	private static Logger logger = Logger.getLogger(InterProScanImporter.class);

	@SuppressWarnings("unchecked")
	public ProteinDomain importDatabaseMatch(TMatch match) {
		// <match id="G3DSA:4.10.75.10" name="no description" dbname="GENE3D">
		ProteinDomain databaseMatch = new ProteinDomain();
		databaseMatch.setProteinDomainId(importProteinDomainId(match));
		databaseMatch.setName(match.getName());
		List<ProteinDomainLocation> locations = new ArrayList<ProteinDomainLocation>();
		for (Location locationArray : match.getLocationArray()) {
			locations.add(importSignatureLocation(locationArray));
		}
		databaseMatch.setProteinDomainLocations(locations
				.toArray(new ProteinDomainLocation[locations.size()]));
		return databaseMatch;
	}

	public ProteinDomainIdentifier importProteinDomainId(TMatch match) {
		// <match id="G3DSA:4.10.75.10" dbname="GENE3D">
		ProteinDomainIdentifier proteinDomainIdentifier = new ProteinDomainIdentifier();
		proteinDomainIdentifier.setDataSourceName(match.getDbname());
		proteinDomainIdentifier.setCrossReferenceId(match.getId());
		return proteinDomainIdentifier;
	}

	@SuppressWarnings("unchecked")
	public InterProScanOutput importInterProScanOutput(Document data)
			throws XmlException, JDOMException {

		DOMOutputter domOutputter = new DOMOutputter();
		EBIInterProScanResultsDocument appResults = EBIInterProScanResultsDocument.Factory
				.parse(domOutputter.output(data));

		TProtein tProtein = appResults.getEBIInterProScanResults()
				.getInterproMatches().getProteinArray(0);
		// TODO: What to do with the other proteins? What if there's none?

		InterProScanOutput output = new InterProScanOutput();
		ProteinSequence proteinSequence = importProtein(tProtein);
		output.setProteinSequence(proteinSequence);
		output.setProteinId(importProteinId(tProtein.getId()));
		List<ProteinDomainMatch> protSigMatches = new ArrayList<ProteinDomainMatch>();
		for (TInterPro interproArray : tProtein.getInterproArray()) {
			ProteinDomainMatch match = importSignatureMatch(interproArray);
			protSigMatches.add(match);
		}
		output.setProteinDomainMatches(protSigMatches
				.toArray(new ProteinDomainMatch[protSigMatches.size()]));
		return output;

	}

	private ProteinGenomicIdentifier importProteinId(String id) {
		// sp|P01174|WAP_RAT --> dataSource=sp crossRef=WAP_RAT
		ProteinGenomicIdentifier proteinGenomicIdentifier = new ProteinGenomicIdentifier();
		String[] idSplit = id.split("|");
		if (idSplit.length > 3 || idSplit.length < 2) {
			throw new ConverterException("Can't parse protein ID: " + id);
		}
		proteinGenomicIdentifier.setDataSourceName(idSplit[0]);
		// Last one, ie 'uniprot'
		proteinGenomicIdentifier
				.setCrossReferenceId(idSplit[idSplit.length - 1]);
		return proteinGenomicIdentifier;
	}

	public ProteinSequence importProtein(TProtein tProtein) {
		// <protein id="sp|P01174|WAP_RAT" length="137"
		// crc64="1C2E8ADA9FD97949">
		ProteinSequence protein = new ProteinSequence();
		protein.setId(tProtein.getId());
		protein.setChecksum(tProtein.getCrc64());
		protein.setLength(new BigInteger(tProtein.getLength()));
		return protein;
	}

	public ProteinDomainLocation importSignatureLocation(Location locationArray) {
		// <location start="30" end="72" score="7.4e-05" status="T"
		// evidence="HMMPfam" />
		ProteinDomainLocation location = new ProteinDomainLocation();
		location.setStart(BigInteger.valueOf(locationArray.getStart()));
		location.setEnd(BigInteger.valueOf(locationArray.getEnd()));

		ProteinDomainLocationStatistics stats = new ProteinDomainLocationStatistics();
		location.setProteinDomainLocationStatistics(stats);

		String score = locationArray.getScore();
		if (score.equalsIgnoreCase("NA")) {
			stats.setEValue(null);
		} else {
			stats.setEValue(Double.parseDouble(score));
		}

		String status = locationArray.getStatus();
		if (status.equalsIgnoreCase("T")) {
			stats.setStatus(SignatureStatus.known);
		} else if (status.equals("?")) {
			stats.setStatus(SignatureStatus.unknown);
		} else {
			logger.warn("Unknown status " + status + " in " + locationArray);
			stats.setStatus(SignatureStatus.unknown);
		}

		String evidence = locationArray.getEvidence();
		SignatureMethod sigMethod = importSignatureMethod(evidence);
		stats.setSignatureMethod(sigMethod);

		return location;
	}

	@SuppressWarnings("unchecked")
	public ProteinDomainMatch importSignatureMatch(TInterPro interproArray) {
		// <interpro id="IPR008197"
		// name="Whey acidic protein, 4-disulphide core" type="Domain"
		// parent_id="IPR015874">
		ProteinDomainMatch proteinSignatureMatch = new ProteinDomainMatch();
		proteinSignatureMatch.setId(interproArray.getId());
		proteinSignatureMatch.setName(interproArray.getName());
		proteinSignatureMatch.setParentId(interproArray.getParentId());
		proteinSignatureMatch.setType(interproArray.getType());

		List<ProteinDomain> databaseMatches = new ArrayList<ProteinDomain>();
		for (TMatch match : interproArray.getMatchArray()) {
			databaseMatches.add(importDatabaseMatch(match));
		}

		proteinSignatureMatch
				.setProteinDomainPerDatabaseMatches(databaseMatches
						.toArray(new ProteinDomain[databaseMatches.size()]));

		List<Classification> classifications = new ArrayList<Classification>();
		for (uk.ac.ebi.schema.TClassification classification : interproArray
				.getClassificationArray()) {
			classifications.add(importClassification(classification));
		}
		proteinSignatureMatch.setClassifications(classifications
				.toArray(new Classification[classifications.size()]));
		return proteinSignatureMatch;
	}

	private Classification importClassification(TClassification tClassification) {
		Classification classification = new Classification();
		classification.setCategory(tClassification.getCategory());
		classification.setClassificationType(tClassification.getClassType());
		classification.setId(tClassification.getId());
		classification.setDescription(tClassification.getDescription());
		return classification;
	}

	public SignatureMethod importSignatureMethod(String evidence) {
		if (evidence.equalsIgnoreCase("superfamily")) {
			// In lowercase from EBI for some reason
			return SignatureMethod.SuperFamily;
		} else if (evidence.equalsIgnoreCase("signalp")) {
			return SignatureMethod.SignalPHMM;
		}

		try {
			return SignatureMethod.fromValue(evidence);
		} catch (IllegalArgumentException ex) {
			logger.warn("Unknown signature method evidence " + evidence);
			return null;
		}
	}
}
