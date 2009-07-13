package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import uk.org.mygrid.cagrid.domain.interproscan.Database;
import uk.org.mygrid.cagrid.domain.interproscan.DatabaseMatch;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.Protein;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureMatch;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;
import uk.org.mygrid.cagrid.valuedomains.SignatureStatus;

public class InterProScanImporter {

	private static final String CRC64 = "crc64";
	private static final String ID = "id";
	private static final String INTERPRO = "interpro";
	private static final String INTERPRO_MATCHES = "interpro_matches";
	private static final String LENGTH = "length";
	private static final String LOCATION = "location";
	private static Logger logger = Logger
			.getLogger(InterProScanImporter.class);
	private static final String MATCH = "match";
	private static final String NAME = "name";

	private static final Namespace NS = Namespace
			.getNamespace("http://www.ebi.ac.uk/schema");
	
	@SuppressWarnings("unchecked")
	public DatabaseMatch importDatabaseMatch(Element dbMatchElem) {
		// <match id="G3DSA:4.10.75.10" name="no description" dbname="GENE3D">
		DatabaseMatch databaseMatch = new DatabaseMatch();
		databaseMatch.setId(dbMatchElem.getAttributeValue(ID));
		databaseMatch.setSignatureName(dbMatchElem.getAttributeValue(NAME));

		databaseMatch.setDatabase(new Database(dbMatchElem
				.getAttributeValue("dbname")));

		List<ProteinSignatureLocation> locations = new ArrayList<ProteinSignatureLocation>();
		for (Element locationElem : (List<Element>) dbMatchElem.getChildren(
				LOCATION, NS)) {
			locations.add(importSignatureLocation(locationElem));
		}
		databaseMatch.setProteinSignatureLocations(locations
				.toArray(new ProteinSignatureLocation[locations.size()]));

		// databaseMatch.setProteinSignatureLocations(proteinSignatureLocations);

		return databaseMatch;
	}

	@SuppressWarnings("unchecked")
	public InterProScanOutput importInterProScanOutput(Document data) {
		InterProScanOutput output = new InterProScanOutput();
		Element rootElement = data.getRootElement();

		Element protein = rootElement.getChild(INTERPRO_MATCHES, NS).getChild(
				"protein", NS);
		output.setProtein(importProtein(protein));

		List<ProteinSignatureMatch> protSigMatches = new ArrayList<ProteinSignatureMatch>();
		for (Element signatureMatchElem : (List<Element>) protein.getChildren(
				INTERPRO, NS)) {
			ProteinSignatureMatch match = importSignatureMatch(signatureMatchElem);
			protSigMatches.add(match);
		}
		output.setProteinSignatureMatches(protSigMatches
				.toArray(new ProteinSignatureMatch[protSigMatches.size()]));
		return output;

	}

	public Protein importProtein(Element proteinElem) {
		// <protein id="sp|P01174|WAP_RAT" length="137" crc64="1C2E8ADA9FD97949"
		// >
		Protein protein = new Protein();
		protein.setId(proteinElem.getAttributeValue(ID));
		protein.setCrc64(proteinElem.getAttributeValue(CRC64));
		protein.setSequenceLength(new BigInteger(proteinElem
				.getAttributeValue(LENGTH)));
		return protein;
	}

	public ProteinSignatureLocation importSignatureLocation(Element locationElem) {
		// <location start="30" end="72" score="7.4e-05" status="T"
		// evidence="HMMPfam" />
		ProteinSignatureLocation location = new ProteinSignatureLocation();
		location.setStart(new BigInteger(locationElem
				.getAttributeValue("start")));
		location.setEnd(new BigInteger(locationElem.getAttributeValue("end")));

		String score = locationElem.getAttributeValue("score");
		if (score.equalsIgnoreCase("NA")) {
			location.setEValue(null);
		} else {
			location.setEValue(Double.parseDouble(score));
		}

		String status = locationElem.getAttributeValue("status");
		if (status.equalsIgnoreCase("T")) {
			location.setStatus(SignatureStatus.KNOWN);
		} else if (status.equals("?")) {
			location.setStatus(SignatureStatus.UNKNOWN);
		} else {
			logger.warn("Unknown status " + status + " in " + locationElem);
			location.setStatus(SignatureStatus.UNKNOWN);
		}

		String evidence = locationElem.getAttributeValue("evidence");
		SignatureMethod sigMethod = importSignatureMethod(evidence);
		location.setSignatureMethod(sigMethod);

		return location;
	}

	@SuppressWarnings("unchecked")
	public ProteinSignatureMatch importSignatureMatch(Element signatureMatchElem) {
		// <interpro id="IPR008197"
		// name="Whey acidic protein, 4-disulphide core" type="Domain"
		// parent_id="IPR015874">
		ProteinSignatureMatch proteinSignatureMatch = new ProteinSignatureMatch();
		proteinSignatureMatch.setId(signatureMatchElem.getAttributeValue(ID));
		proteinSignatureMatch.setName(signatureMatchElem
				.getAttributeValue(NAME));
		proteinSignatureMatch.setParentId(signatureMatchElem
				.getAttributeValue("parent_id"));
		proteinSignatureMatch.setType(signatureMatchElem
				.getAttributeValue("type"));

		List<DatabaseMatch> databaseMatches = new ArrayList<DatabaseMatch>();
		for (Element dbMatchElem : (List<Element>) signatureMatchElem
				.getChildren(MATCH, NS)) {
			databaseMatches.add(importDatabaseMatch(dbMatchElem));
		}
		proteinSignatureMatch.setDatabaseMatches(databaseMatches
				.toArray(new DatabaseMatch[databaseMatches.size()]));
		return proteinSignatureMatch;
	}

	public SignatureMethod importSignatureMethod(String evidence) {
		if (evidence.equals("superfamily")) {
			// In lowercase from EBI for some reason
			evidence = "SuperFamily";
		}

		try {
			return SignatureMethod.fromValue(evidence);
		} catch (IllegalArgumentException ex) {
			logger.warn("Unknown signature method evidence " + evidence);
			return null;
		}
	}
}
