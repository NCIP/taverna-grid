package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.jdom.Document;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.interproscan.Database;
import uk.org.mygrid.cagrid.domain.interproscan.DatabaseMatch;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureMatch;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.DummyInterProScanInvoker;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;
import uk.org.mygrid.cagrid.valuedomains.SignatureStatus;

public class TestInterProScanConverterOutput {
	private Document original;
	private InterProScanConverter converter;
	private InterProScanOutput converted;

	@Before
	public void prepare() throws Exception {
		DummyInterProScanInvoker invoker = new DummyInterProScanInvoker();
		original = invoker.getDummyOutput();
		converter = new InterProScanConverter();
		converted = converter.convertInterProScanOutput(original);
	}

	@Test
	public void protein() throws Exception {
		assertEquals("Wrong ID for match 0", "sp|P01174|WAP_RAT", converted
				.getProtein().getId());
		assertEquals("Wrong sequence length", BigInteger.valueOf(137),
				converted.getProtein().getSequenceLength());
		assertEquals("Wrong CRC 64", "1C2E8ADA9FD97949", converted.getProtein()
				.getCrc64());
	}

	@Test
	public void proteinSignatureMatches() throws Exception {
		ProteinSignatureMatch[] protMatches = converted
				.getProteinSignatureMatches();
		assertEquals("Wrong number of signature matches", 4, protMatches.length);

		ProteinSignatureMatch protMatch0 = protMatches[0];
		// <interpro id="IPR008197"
		// name="Whey acidic protein, 4-disulphide core"
		// type="Domain" parent_id="IPR015874">
		assertEquals("Wrong match ID for signature match 0", "IPR008197",
				protMatch0.getId());
		assertEquals("Wrong name for signature match 0",
				"Whey acidic protein, 4-disulphide core", protMatch0.getName());
		assertEquals("Wrong type for signature match 0", "Domain", protMatch0
				.getType());
		assertEquals("Wrong parent ID for signature match 0", "IPR015874",
				protMatch0.getParentId());

		// The rest of the proteinMatches
		assertEquals("Wrong ID for signature match 1", "IPR015874",
				protMatches[1].getId());

		assertEquals("Wrong ID for signature match 2", "IPR018069",
				protMatches[2].getId());
		assertEquals("Wrong name for signature match 2",
				"Whey acidic protein, 4-disulphide core, conserved site",
				protMatches[2].getName());

		assertEquals("Wrong ID for match 3", "noIPR", protMatches[3].getId());

	}

	@Test
	public void databaseMatches() throws Exception {
		ProteinSignatureMatch[] protMatches = converted
				.getProteinSignatureMatches();
		DatabaseMatch[] databaseMatches = protMatches[0].getDatabaseMatches();
		assertEquals("Wrong number of database matches for signature match 0",
				4, databaseMatches.length);

		// <match id="G3DSA:4.10.75.10" name="no description" dbname="GENE3D">
		assertEquals("Wrong ID for database match 0", "G3DSA:4.10.75.10",
				databaseMatches[0].getId());
		assertEquals("Wrong database for database match 0", new Database("GENE3D"),
				databaseMatches[0].getDatabase());
		// TODO: Should it be null or "" instead?
		assertEquals("Wrong signature for database match 0", "no description",
				databaseMatches[0].getSignatureName());

		assertEquals("Wrong ID for database match 1", "PF00095",
				databaseMatches[1].getId());
		assertEquals("Wrong database for database match 1", new Database("PFAM"),
				databaseMatches[1].getDatabase());
		assertEquals("Wrong signature for database match 1", "WAP",
				databaseMatches[1].getSignatureName());

		assertEquals("Wrong ID for database match 2", "SM00217",
				databaseMatches[2].getId());
		assertEquals("Wrong database for database match 2", new Database("SMART"),
				databaseMatches[2].getDatabase());
		assertEquals("Wrong signature for database match 2", "WAP",
				databaseMatches[2].getSignatureName());

		assertEquals("Wrong ID for database match 3", "SSF57256",
				databaseMatches[3].getId());
		assertEquals("Wrong database for database match 3", new Database("SUPERFAMILY"),
				databaseMatches[3].getDatabase());
		assertEquals("Wrong signature for database match 3", "Elafin-like",
				databaseMatches[3].getSignatureName());

		// The rest of the proteinMatches
		assertEquals("Wrong number of database matches for signature match 1",
				1, protMatches[1].getDatabaseMatches().length);
		assertEquals("Wrong number of database matches for signature match 2",
				2, protMatches[2].getDatabaseMatches().length);
		assertEquals("Wrong number of database matches for signature match 3",
				2, protMatches[3].getDatabaseMatches().length);
	}

	@Test
	public void locations() throws Exception {
		ProteinSignatureMatch[] protMatches = converted
				.getProteinSignatureMatches();
		DatabaseMatch[] databaseMatches = protMatches[0].getDatabaseMatches();
		ProteinSignatureLocation[] locations0 = databaseMatches[0]
				.getProteinSignatureLocations();
		assertEquals("Wrong number of locations for db match 0 of protmatch 0",
				1, locations0.length);
		// <location start="77" end="128" score="9.5e-05" status="T"
		// evidence="Gene3D" />
		ProteinSignatureLocation location00 = locations0[0];
		assertEquals("Invalid location start", BigInteger.valueOf(77),
				location00.getStart());
		assertEquals("Invalid location end", BigInteger.valueOf(128),
				location00.getEnd());
		assertEquals("Invalid location eValue", 9.5e-05, location00
				.getEValue(), 1e-06);
		assertEquals("Invalid location status", SignatureStatus.KNOWN,
				location00.getStatus());
		assertEquals("Invalid location evidence", SignatureMethod.Gene3D,
				location00.getSignatureMethod());

		ProteinSignatureLocation[] locations1 = databaseMatches[1]
				.getProteinSignatureLocations();
		assertEquals("Wrong number of locations for db match 1 of protmatch 0",
				2, locations1.length);
		ProteinSignatureLocation location10 = locations1[0];
		assertEquals("Invalid location start", BigInteger.valueOf(30),
				location10.getStart());
		assertEquals("Invalid location end", BigInteger.valueOf(72), location10
				.getEnd());
		assertEquals("Invalid location evidence", SignatureMethod.HMMPfam,
				location10.getSignatureMethod());

		assertEquals("Invalid location start", BigInteger.valueOf(79),
				locations1[1].getStart());

		assertEquals("Invalid location evidence", SignatureMethod.HMMSmart,
				databaseMatches[2].getProteinSignatureLocations()[0]
						.getSignatureMethod());

		ProteinSignatureLocation location31 = databaseMatches[3]
				.getProteinSignatureLocations()[1];
		assertEquals("Invalid location evidence", SignatureMethod.SuperFamily,
				location31.getSignatureMethod());
		assertEquals("Invalid location eValue", 0.004, location31.getEValue(), 0.0001);

		// <location start="48" end="61" score="NA" status="?"
		// evidence="ScanRegExp" />
		ProteinSignatureLocation weirdLocation = protMatches[2]
				.getDatabaseMatches()[1].getProteinSignatureLocations()[0];
		// FIXME: Should probably be made optional in schema so it can be null here
		assertEquals("Invalid location eValue", 0.0, weirdLocation.getEValue(), 0.0);
		assertEquals("Invalid location status", SignatureStatus.UNKNOWN,
				weirdLocation.getStatus());
		assertEquals("Invalid location evidence", SignatureMethod.ScanRegExp,
				weirdLocation.getSignatureMethod());
		// FIXME: What to do about unknown signature methods?
		
		
		
	}

}
