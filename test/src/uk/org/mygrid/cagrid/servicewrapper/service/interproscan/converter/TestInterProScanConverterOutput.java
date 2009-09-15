package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.jdom.Document;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomain;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinDomainMatch;
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
		assertEquals("Wrong ID for match 0", "WAP_RAT", converted
				.getProteinId().getCrossReferenceId());
		assertEquals("Wrong ID for match 0", "sp", converted.getProteinId()
				.getDataSourceName());
		assertEquals("Wrong ID for match 0", "sp|P01174|WAP_RAT", converted
				.getProteinSequence().getId());

		assertEquals("Wrong sequence length", BigInteger.valueOf(137),
				converted.getProteinSequence().getLength());
		assertEquals("Wrong CRC 64", "1C2E8ADA9FD97949", converted
				.getProteinSequence().getChecksum());
	}

	@Test
	public void proteinSignatureMatches() throws Exception {
		ProteinDomainMatch[] protMatches = converted.getProteinDomainMatches();
		assertEquals("Wrong number of signature matches", 4, protMatches.length);

		ProteinDomainMatch protMatch0 = protMatches[0];
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
		ProteinDomainMatch[] protMatches = converted.getProteinDomainMatches();
		ProteinDomain[] databaseMatches = protMatches[0]
				.getProteinDomainPerDatabaseMatches();
		assertEquals("Wrong number of database matches for signature match 0",
				4, databaseMatches.length);

		// <match id="G3DSA:4.10.75.10" name="no description" dbname="GENE3D">
		assertEquals("Wrong ID for database match 0", "G3DSA:4.10.75.10",
				databaseMatches[0].getProteinDomainId().getCrossReferenceId());
		assertEquals("Wrong database for database match 0", "GENE3D",
				databaseMatches[0].getProteinDomainId().getDataSourceName());
		// TODO: Should it be null or "" instead?
		assertEquals("Wrong signature for database match 0", "no description",
				databaseMatches[0].getName());

		assertEquals("Wrong ID for database match 1", "PF00095",
				databaseMatches[1].getProteinDomainId().getCrossReferenceId());
		assertEquals("Wrong database for database match 1", "PFAM",
				databaseMatches[1].getProteinDomainId().getDataSourceName());
		assertEquals("Wrong signature for database match 1", "WAP",
				databaseMatches[1].getName());

		assertEquals("Wrong ID for database match 2", "SM00217",
				databaseMatches[2].getProteinDomainId().getCrossReferenceId());
		assertEquals("Wrong database for database match 2", "SMART",
				databaseMatches[2].getProteinDomainId().getDataSourceName());
		assertEquals("Wrong signature for database match 2", "WAP",
				databaseMatches[2].getName());

		assertEquals("Wrong ID for database match 3", "SSF57256",
				databaseMatches[3].getProteinDomainId().getCrossReferenceId());
		assertEquals("Wrong database for database match 3", "SUPERFAMILY",
				databaseMatches[3].getProteinDomainId().getDataSourceName());
		assertEquals("Wrong signature for database match 3", "Elafin-like",
				databaseMatches[3].getName());

		// The rest of the proteinMatches
		assertEquals("Wrong number of database matches for signature match 1",
				1, protMatches[1].getProteinDomainPerDatabaseMatches().length);
		assertEquals("Wrong number of database matches for signature match 2",
				2, protMatches[2].getProteinDomainPerDatabaseMatches().length);
		assertEquals("Wrong number of database matches for signature match 3",
				2, protMatches[3].getProteinDomainPerDatabaseMatches().length);
	}

	@Test
	public void locations() throws Exception {
		ProteinDomainMatch[] protMatches = converted.getProteinDomainMatches();
		ProteinDomain[] databaseMatches = protMatches[0]
				.getProteinDomainPerDatabaseMatches();
		ProteinDomainLocation[] locations0 = databaseMatches[0]
				.getProteinDomainLocations();
		assertEquals("Wrong number of locations for db match 0 of protmatch 0",
				1, locations0.length);
		// <location start="77" end="128" score="9.5e-05" status="T"
		// evidence="Gene3D" />
		ProteinDomainLocation location00 = locations0[0];
		assertEquals("Invalid location start", BigInteger.valueOf(77),
				location00.getStart());
		assertEquals("Invalid location end", BigInteger.valueOf(128),
				location00.getEnd());
		assertEquals("Invalid location eValue", 9.5e-05, location00
				.getProteinDomainLocationStatistics().getEValue(), 1e-06);
		assertEquals("Invalid location status", SignatureStatus.known,
				location00.getProteinDomainLocationStatistics().getStatus());
		assertEquals("Invalid location evidence", SignatureMethod.Gene3D,
				location00.getProteinDomainLocationStatistics()
						.getSignatureMethod());

		ProteinDomainLocation[] locations1 = databaseMatches[1]
				.getProteinDomainLocations();
		assertEquals("Wrong number of locations for db match 1 of protmatch 0",
				2, locations1.length);
		ProteinDomainLocation location10 = locations1[0];
		assertEquals("Invalid location start", BigInteger.valueOf(30),
				location10.getStart());
		assertEquals("Invalid location end", BigInteger.valueOf(72), location10
				.getEnd());
		assertEquals("Invalid location evidence", SignatureMethod.HMMPfam,
				location10.getProteinDomainLocationStatistics()
						.getSignatureMethod());

		assertEquals("Invalid location start", BigInteger.valueOf(79),
				locations1[1].getStart());

		assertEquals("Invalid location evidence", SignatureMethod.HMMSmart,
				databaseMatches[2].getProteinDomainLocations()[0]
						.getProteinDomainLocationStatistics()
						.getSignatureMethod());

		ProteinDomainLocation location31 = databaseMatches[3]
				.getProteinDomainLocations()[1];
		assertEquals("Invalid location evidence", SignatureMethod.SuperFamily,
				location31.getProteinDomainLocationStatistics().getSignatureMethod());
		assertEquals("Invalid location eValue", 0.004, location31.getProteinDomainLocationStatistics().getEValue(),
				0.0001);

		// <location start="48" end="61" score="NA" status="?"
		// evidence="ScanRegExp" />
		ProteinDomainLocation weirdLocation = protMatches[2]
				.getProteinDomainPerDatabaseMatches()[1].getProteinDomainLocations()[0];
		assertNull("Invalid location eValue", weirdLocation.getProteinDomainLocationStatistics().getEValue());
		assertEquals("Invalid location status", SignatureStatus.unknown,
				weirdLocation.getProteinDomainLocationStatistics().getStatus());
		assertEquals("Invalid location evidence", SignatureMethod.ScanRegExp,
				weirdLocation.getProteinDomainLocationStatistics().getSignatureMethod());
		// FIXME: What to do about unknown signature methods?

	}

}
