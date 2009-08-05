package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.interproscan.DatabaseMatch;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureMatch;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClientUtils;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

/**
 * IPS1: signature method parameter test
 *  
 * Test empty/null: Should use default value
 * 
 * Test invalid value: Should produce error
 * 
 * Additional tests: Verify that the hits returned only come from the signature methods that were provided in the input parameters of the call.
*/

public class InterProScanSignatureMethodParameterTest{

	private static final int TIMEOUT_SECONDS = 60;
	private InterProScanClientUtils clientUtils  = null;

	@Before
	public void init(){
		InterProScanClient interproscan = null;
		clientUtils  = null;
		try {
			interproscan = new InterProScanClient("https://localhost:8443/wsrf/services/cagrid/InterProScan");
			clientUtils = new InterProScanClientUtils(interproscan);
		} catch (Exception e) {
			System.exit(1);
		}
	}
	
	// Test that passing a null value for signature parameter results in the default (i.e. all signature methods) being used
	@Test
	public void testDefaultSignatureMethod() throws Exception {
		InterProScanInput input = new InterProScanInput();
		input.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		// Set the email, but do not set the signature method parameter - test the default value
		params.setEmail("mannen@soiland-reyes.com");
		params.setSignatureMethod(null);
		input.setInterProScanInputParameters(params);
		
		// List of databases that should contain the matches for "uniprot:wap_rat" when all possible 
		// signature methods are used to search for the matches (as returned by the original service) 
		Set<String> expectedDatabases = new HashSet<String>();
		expectedDatabases.add("GENE3D".toLowerCase());
		expectedDatabases.add("PFAM".toLowerCase());
		expectedDatabases.add("SMART".toLowerCase());
		expectedDatabases.add("SUPERFAMILY".toLowerCase());
		expectedDatabases.add("PRINTS".toLowerCase());
		expectedDatabases.add("PROSITE".toLowerCase());
		expectedDatabases.add("PANTHER".toLowerCase());
		expectedDatabases.add("SIGNALP".toLowerCase());
		
		
		Set<String> foundDatabases = new HashSet<String>();
		Set<String> foundSignatureMethods = new HashSet<String>();
		
		InterProScanOutput interProScanOut = null;
		try{
			interProScanOut = clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex){ // should not happen
			ex.printStackTrace();
			System.exit(1);
		}
		
		// Parse the results
		parseResults(interProScanOut, foundDatabases, foundSignatureMethods);
		
		// Confirm that the databases in which matches have been found contain 
		// at least the expected ones obtained by looking at the original xml results
		// returned by the service in uk/org/mygrid/cagrid/servicewrapper/serviceinvoker/interproscan/wap_rat_output.xml
		// on 27 July 2009
		Assert.assertTrue(foundDatabases.containsAll(expectedDatabases));
	}
	
	// Test that passing an invalid signature method name produces an error
	@SuppressWarnings("serial")
	@Test(expected=org.apache.axis.AxisFault.class)
	public void failsInvalidSignatureMethod() throws Exception{
		InterProScanInput input = new InterProScanInput();
		input.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		// Set the email, but do not set the signature method parameter - test the default value
		params.setEmail("mannen@soiland-reyes.com");
		params.setSignatureMethod(new SignatureMethod[] { new SignatureMethod("Dummy"){} });
		input.setInterProScanInputParameters(params);
		
		// Should fail
		clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 100000);
	}
	
	// Test that only signature method passed are used to find the hits
	@Test
	public void testPassedSignatureMethod() throws Exception {
		InterProScanInput input = new InterProScanInput();
		input.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		// Set the email, but do not set the signature method parameter - test the default value
		params.setEmail("mannen@soiland-reyes.com");
		params.setSignatureMethod(null);
		params.setSignatureMethod(new SignatureMethod[] { SignatureMethod.SuperFamily, SignatureMethod.HMMPfam });
		input.setInterProScanInputParameters(params);

		// List of signature methods that were used to find the hits should contain only SuperFamily and HMMPfam
		Set<String> expectedSignatureMethods = new HashSet<String>();
		expectedSignatureMethods.add(SignatureMethod.SuperFamily.getValue().toLowerCase());
		expectedSignatureMethods.add(SignatureMethod.HMMPfam.getValue().toLowerCase());

		Set<String> foundDatabases = new HashSet<String>();
		Set<String> foundSignatureMethods = new HashSet<String>();

		InterProScanOutput interProScanOut = null;
		try{
			interProScanOut = clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex){ // should not happen
			ex.printStackTrace();
			System.exit(1);
		}
		
		// Parse the results
		parseResults(interProScanOut, foundDatabases, foundSignatureMethods);
		
		// Confirm that only SuperFamily and HMMPfam signature methods have been used to find the results
		Assert.assertTrue(expectedSignatureMethods.equals(foundSignatureMethods));
	}
	
	private void parseResults(InterProScanOutput output, Set<String> foundDatabases, Set<String> foundSignatureMethods){
		for (ProteinSignatureMatch proteinSignatureMatch : output.getProteinSignatureMatches()){
			DatabaseMatch[] databaseMatches = proteinSignatureMatch.getDatabaseMatches();
			for (DatabaseMatch databaseMatch : databaseMatches){
				foundDatabases.add(databaseMatch.getDatabase().getName().toLowerCase());
				ProteinSignatureLocation[] proteinSignatureLocations = databaseMatch.getProteinSignatureLocations();
				for (ProteinSignatureLocation location : proteinSignatureLocations) {
					foundSignatureMethods.add(location.getSignatureMethod().getValue().toLowerCase());
				}
			}
		}
	}

}
