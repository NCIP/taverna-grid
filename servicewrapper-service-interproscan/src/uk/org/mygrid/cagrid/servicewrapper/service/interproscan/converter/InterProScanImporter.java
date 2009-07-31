package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

import uk.ac.ebi.schema.EBIInterProScanResultsDocument;
import uk.ac.ebi.schema.TInterPro;
import uk.ac.ebi.schema.TMatch;
import uk.ac.ebi.schema.TProtein;
import uk.ac.ebi.schema.TMatch.Location;
import uk.org.mygrid.cagrid.domain.interproscan.Database;
import uk.org.mygrid.cagrid.domain.interproscan.DatabaseMatch;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.Protein;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureMatch;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;
import uk.org.mygrid.cagrid.valuedomains.SignatureStatus;

public class InterProScanImporter {

	private static Logger logger = Logger
			.getLogger(InterProScanImporter.class);
	
	@SuppressWarnings("unchecked")
	public DatabaseMatch importDatabaseMatch(TMatch match) {
		// <match id="G3DSA:4.10.75.10" name="no description" dbname="GENE3D">
		DatabaseMatch databaseMatch = new DatabaseMatch();
		databaseMatch.setId(match.getId());
		databaseMatch.setSignatureName(match.getName());

		databaseMatch.setDatabase(new Database(match
				.getDbname()));

		List<ProteinSignatureLocation> locations = new ArrayList<ProteinSignatureLocation>();
		for (Location locationArray : match.getLocationArray()) {
			locations.add(importSignatureLocation(locationArray));
		}
		databaseMatch.setProteinSignatureLocations(locations
				.toArray(new ProteinSignatureLocation[locations.size()]));

		// databaseMatch.setProteinSignatureLocations(proteinSignatureLocations);

		return databaseMatch;
	}

	@SuppressWarnings("unchecked")
	public InterProScanOutput importInterProScanOutput(Document data) throws XmlException, JDOMException {
		
		DOMOutputter domOutputter = new DOMOutputter();
		EBIInterProScanResultsDocument appResults = EBIInterProScanResultsDocument.Factory.parse
				(domOutputter.output(data));
		
		TProtein tProtein = appResults.getEBIInterProScanResults().getInterproMatches().getProteinArray(0);
		// TODO: What to do with the other proteins? What if there's none?
		
		InterProScanOutput output = new InterProScanOutput();
		output.setProtein(importProtein(tProtein));
		List<ProteinSignatureMatch> protSigMatches = new ArrayList<ProteinSignatureMatch>();
		for (TInterPro interproArray : tProtein.getInterproArray()) {
			ProteinSignatureMatch match = importSignatureMatch(interproArray);
			protSigMatches.add(match);
		}
		output.setProteinSignatureMatches(protSigMatches
				.toArray(new ProteinSignatureMatch[protSigMatches.size()]));
		return output;

	}

	public Protein importProtein(TProtein tProtein) {
		// <protein id="sp|P01174|WAP_RAT" length="137" crc64="1C2E8ADA9FD97949"
		// >
		Protein protein = new Protein();
		protein.setId(tProtein.getId());
		protein.setCrc64(tProtein.getCrc64());
		protein.setSequenceLength(new BigInteger(tProtein
				.getLength()));
		return protein;
	}

	public ProteinSignatureLocation importSignatureLocation(Location locationArray) {
		// <location start="30" end="72" score="7.4e-05" status="T"
		// evidence="HMMPfam" />
		ProteinSignatureLocation location = new ProteinSignatureLocation();
		location.setStart(BigInteger.valueOf(locationArray
				.getStart()));
		location.setEnd(BigInteger.valueOf(locationArray.getEnd()));

		String score = locationArray.getScore();
		if (score.equalsIgnoreCase("NA")) {
			location.setEValue(null);
		} else {
			location.setEValue(Double.parseDouble(score));
		}

		String status = locationArray.getStatus();
		if (status.equalsIgnoreCase("T")) {
			location.setStatus(SignatureStatus.KNOWN);
		} else if (status.equals("?")) {
			location.setStatus(SignatureStatus.UNKNOWN);
		} else {
			logger.warn("Unknown status " + status + " in " + locationArray);
			location.setStatus(SignatureStatus.UNKNOWN);
		}

		String evidence = locationArray.getEvidence();
		SignatureMethod sigMethod = importSignatureMethod(evidence);
		location.setSignatureMethod(sigMethod);

		return location;
	}

	@SuppressWarnings("unchecked")
	public ProteinSignatureMatch importSignatureMatch(TInterPro interproArray) {
		// <interpro id="IPR008197"
		// name="Whey acidic protein, 4-disulphide core" type="Domain"
		// parent_id="IPR015874">
		ProteinSignatureMatch proteinSignatureMatch = new ProteinSignatureMatch();
		proteinSignatureMatch.setId(interproArray.getId());
		proteinSignatureMatch.setName(interproArray
				.getName());
		proteinSignatureMatch.setParentId(interproArray
				.getParentId());
		proteinSignatureMatch.setType(interproArray
				.getType());

		List<DatabaseMatch> databaseMatches = new ArrayList<DatabaseMatch>();
		for (TMatch match : interproArray.getMatchArray()) {
			databaseMatches.add(importDatabaseMatch(match));
		}
		proteinSignatureMatch.setDatabaseMatches(databaseMatches
				.toArray(new DatabaseMatch[databaseMatches.size()]));
		return proteinSignatureMatch;
	}

	public SignatureMethod importSignatureMethod(String evidence) {
		if (evidence.equalsIgnoreCase("superfamily")) {
			// In lowercase from EBI for some reason
			return SignatureMethod.SuperFamily;
		} else if(evidence.equalsIgnoreCase("signalp")) {
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
