package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/*import javax.xml.namespace.QName;

import org.globus.wsrf.utils.XmlUtils;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;
*/

import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.SequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.DatabaseMatch;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureLocation;
import uk.org.mygrid.cagrid.domain.interproscan.ProteinSignatureMatch;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClientUtils;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

public class InterProScanTest {

	private static final int TIMEOUT_SECONDS = 60;

	public static void main(String[] args) throws Exception {
		
		InterProScanTest interProScanTest = new InterProScanTest();
		
		interProScanTest.ips1();
		interProScanTest.ips2();
		interProScanTest.ips3();
	}


	/**
	 * Test IPS1 (see {@link http://www.mygrid.org.uk/dev/wiki/display/caGrid/Task+3.4+-+Test+Plan}).
	 * that tests the signature method parameter as follows: 
	 * <ol>
	 * <li> not passing it should cause the default value (i.e. all methods) to be used
	 * <li> passing an incorrect value should cause an error
	 * <li> verify that the hits returned only come from the signature methods 
	 * that were provided in the input parameters of the call.
	 * </ol>
	 * 
	 */
	private void ips1() throws Exception {
		
		System.out.println("///////////////////////////////////////////////////////////////");
		System.out.println("Test IPS1: Testing the value of the signature method parameter.");
		System.out.println("///////////////////////////////////////////////////////////////");

		InterProScanClient interproscan = new InterProScanClient(
				"https://localhost:8443/wsrf/services/cagrid/InterProScan");
		InterProScanClientUtils clientUtils = new InterProScanClientUtils(
				interproscan);
		
		// Invocation 1: test that passing null for signature methods results in 
		// the default value, i.e. all methods
		System.out.println();
		System.out.println("IPS1 Service invocation 1: testing default value for signature method.");
		
		InterProScanInput input1 = new InterProScanInput();
		input1.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params1 = new InterProScanInputParameters();
		// Set the email, but do not set the signature method parameter - test the default value
		params1.setEmail("mannen@soiland-reyes.com");
		input1.setInterProScanInputParameters(params1);
		
		// Print out the input parameters
		printInputParameters(input1, params1);		
		
		// List of databases that should contain the matches for "uniprot:wap_rat" when all possible 
		// signature methods are used to search for the matches (as returned by the original service) 
		Set<String> expectedDatabases1 = new HashSet<String>();
		expectedDatabases1.add("GENE3D".toLowerCase());
		expectedDatabases1.add("PFAM".toLowerCase());
		expectedDatabases1.add("SMART".toLowerCase());
		expectedDatabases1.add("SUPERFAMILY".toLowerCase());
		expectedDatabases1.add("PRINTS".toLowerCase());
		expectedDatabases1.add("PROSITE".toLowerCase());
		expectedDatabases1.add("PANTHER".toLowerCase());
		expectedDatabases1.add("SIGNALP".toLowerCase());
		
		// List of actual signature methods that have been used to find the matches 
		// for "uniprot:wap_rat" (as returned by the original service) 
		Set<String> expectedSignatureMethods1 = new HashSet<String>();
		expectedSignatureMethods1.add("Gene3D".toLowerCase());
		expectedSignatureMethods1.add("HMMPfam".toLowerCase());
		expectedSignatureMethods1.add("HMMSmart".toLowerCase());
		expectedSignatureMethods1.add("superfamily".toLowerCase());
		expectedSignatureMethods1.add("FPrintScan".toLowerCase());
		expectedSignatureMethods1.add("PatternScan".toLowerCase());
		// expectedSignatureMethods1.add("ScanRegExp".toLowerCase()); //deprecated
		expectedSignatureMethods1.add("HMMPanther".toLowerCase());
		expectedSignatureMethods1.add("SignalPHMM".toLowerCase());
		
		InterProScanOutput interProScanOut1 = null;
		try{
			System.out.println("Invocation started at: "+ new Date().toString());
			interProScanOut1 = clientUtils.interProScanSync(input1, TIMEOUT_SECONDS * 100000);
			System.out.println("Invocation ended at: " + new Date().toString());
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut1 != null){
			printOutput(interProScanOut1, expectedDatabases1, expectedSignatureMethods1);
		}
		
		// Invocation 2: test that only signature methods passed as input parameters appear in the results
		// Use just one signature method that we know will return results
		Thread.sleep(500); // back off a bit
		System.out.println();
		System.out.println("IPS1 Service invocation 2: testing single signature method PatternScan");

		InterProScanInput input2 = new InterProScanInput();
		input2.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params2 = new InterProScanInputParameters();
		params2.setEmail("mannen@soiland-reyes.com");
		params2.setSignatureMethod(new SignatureMethod[] { SignatureMethod.PatternScan });
		input2.setInterProScanInputParameters(params2);
		
		// Print out the input parameters
		printInputParameters(input2, params2);		
		
		// List of databases that should contain the matches for "uniprot:wap_rat" when PatternScan 
		// signature method is used to search for the matches (as returned by the original service) 
		Set<String> expectedDatabases2 = new HashSet<String>();
		expectedDatabases2.add("PROSITE".toLowerCase());
		
		// List of signature methods should contain only PatternScan 
		Set<String> expectedSignatureMethods2 = new HashSet<String>();
		expectedSignatureMethods2.add(SignatureMethod.PatternScan.getValue().toLowerCase());
		
		InterProScanOutput interProScanOut2 = null;
		try{
			interProScanOut2 = clientUtils.interProScanSync(input2, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut2 != null){
			printOutput(interProScanOut2, expectedDatabases2, expectedSignatureMethods2);
		}
		
		// Invocation 3: test that only signature methods passed as input parameters appear in the results
		// Same as Invocation 2, but we use two signature methods instead of one that we know will return results
		Thread.sleep(500); // back off a bit
		System.out.println();
		System.out.println("IPS1 Service invocation 3: testing two signature methods PatternScan and ScanRegExp");
		
		InterProScanInput input3 = new InterProScanInput();
		input3.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params3 = new InterProScanInputParameters();
		params3.setEmail("mannen@soiland-reyes.com");
		params3.setSignatureMethod(new SignatureMethod[] { SignatureMethod.SuperFamily, SignatureMethod.HMMPfam });
		input3.setInterProScanInputParameters(params3);
		
		// Print out the input parameters
		printInputParameters(input3, params3);
		
		// List of databases that should contain the matches for "uniprot:wap_rat" when PatternScan 
		// and ScanRegExp signature methods are used to search for the matches (as returned by the original service) 
		Set<String> expectedDatabases3 = new HashSet<String>();
		expectedDatabases3.add("SUPERFAMILY".toLowerCase());
		expectedDatabases3.add("PFAM".toLowerCase());
		
		// List of signature methods should contain only PatternScan and ScanRegExp
		Set<String> expectedSignatureMethods3 = new HashSet<String>();
		expectedSignatureMethods3.add(SignatureMethod.SuperFamily.getValue().toLowerCase());
		expectedSignatureMethods3.add(SignatureMethod.HMMPfam.getValue().toLowerCase());
		
		InterProScanOutput interProScanOut3 = null;
		try{
			interProScanOut3 = clientUtils.interProScanSync(input3, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut3 != null){
			printOutput(interProScanOut3, expectedDatabases3, expectedSignatureMethods3);
		}
		
		// Invocation 4: confirm that passing incorrect signature method results in an error
		// Use two signature methods that we know will return results, but use an incorrect second one
		Thread.sleep(500); // back off a bit
		System.out.println();
		System.out.println("IPS1 Service invocation 4: testing incorrect value for signature method.");

		InterProScanInput input4 = new InterProScanInput();
		input4.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));		
		InterProScanInputParameters params4 = new InterProScanInputParameters();
		params4.setEmail("mannen@soiland-reyes.com");
		params4.setSignatureMethod(new SignatureMethod[] { new SignatureMethod("Dummy") }); // Incorrect signature method
		input4.setInterProScanInputParameters(params4);
		
		// Print out the input parameters
		printInputParameters(input4, params4);
		
		// We are expecting an error as we are passing an incorrect signature method
		InterProScanOutput interProScanOut4 = null;
		try{
			interProScanOut4 = clientUtils.interProScanSync(input4, TIMEOUT_SECONDS * 100000);
			System.out.println("There should have been an error in the invocation.");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	/**
	 * Test IPS2 (see {@link http://www.mygrid.org.uk/dev/wiki/display/caGrid/Task+3.4+-+Test+Plan}).
	 * that tests the protein parameter as follows: 
	 * <ol>
	 * <li> not passing it should cause an error
	 * <li> passing an incorrect value should cause an error
	 * <li> verify that the hits returned for the same protein passed as its database identifier and
	 * as FASTA sequence are the same.
	 * </ol>
	 * 
	 */
	private void ips2() throws Exception {
		
		Thread.sleep(500); // back off a bit

		System.out.println();
		System.out.println("/////////////////////////////////////////////////////");
		System.out.println("Test IPS2: Testing the value of the protein parameter.");	
		System.out.println("/////////////////////////////////////////////////////");
		
		InterProScanClient interproscan = new InterProScanClient(
		"https://localhost:8443/wsrf/services/cagrid/InterProScan");
		InterProScanClientUtils clientUtils = new InterProScanClientUtils(
				interproscan);
		
		// Invocation 1: test that passing null for a protein results in an error
		System.out.println();
		System.out.println("IPS2 Service invocation 1: testing null value for protein.");
		
		InterProScanInput input1 = new InterProScanInput();
		input1.setSequenceRepresentation(null);
		InterProScanInputParameters params1 = new InterProScanInputParameters();
		params1.setEmail("mannen@soiland-reyes.com");
		input1.setInterProScanInputParameters(params1);	

		// Print out the input parameters
		printInputParameters(input1, params1);	
		
		InterProScanOutput interProScanOut1 = null;
		try{
			interProScanOut1 = clientUtils.interProScanSync(input1, TIMEOUT_SECONDS * 100000);
			System.out.println("There should have been an error in the invocation.");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Invocation 2: test that passing an incorrect value for a protein results in an error
		Thread.sleep(500); // back off a bit
		System.out.println();
		System.out.println("IPS2 Service invocation 2: testing incorrect value for protein.");
		
		InterProScanInput input2 = new InterProScanInput();
		input2.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat_BLABLA")); //incorrect sequence
		InterProScanInputParameters params2 = new InterProScanInputParameters();
		params2.setEmail("mannen@soiland-reyes.com");
		input2.setInterProScanInputParameters(params2);	

		// Print out the input parameters
		printInputParameters(input2, params2);	
		
		InterProScanOutput interProScanOut2 = null;
		try{
			interProScanOut2 = clientUtils.interProScanSync(input2, TIMEOUT_SECONDS * 100000);
			System.out.println("There should have been an error in the invocation.");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Invocation 3: Pass the same protein both as a sequence and as its database identifier 
		// and verify that the returned hits are identical.
		Thread.sleep(500); // back off a bit
		System.out.println();
		System.out.println("IPS2 Service invocation 3: testing the same protein passed as FASTA sequence and database identifier.");
		
		InterProScanInput input3_1 = new InterProScanInput();
		input3_1.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat")); //protein identifier
		InterProScanInputParameters params3 = new InterProScanInputParameters();
		params3.setEmail("mannen@soiland-reyes.com");
		params3.setSignatureMethod(new SignatureMethod[]{SignatureMethod.PatternScan});
		input3_1.setInterProScanInputParameters(params3);	
		
		// Print out the input parameters
		printInputParameters(input3_1, params3);	
		
		InterProScanOutput interProScanOut3_1 = null;
		try{
			interProScanOut3_1 = clientUtils.interProScanSync(input3_1, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex) { // should not happen
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut3_1 != null){
			printOutput(interProScanOut3_1, null, null);
		}
		
		InterProScanInput input3_2 = new InterProScanInput();
		input3_2.setSequenceRepresentation(new FASTAProteinSequence(">sp|P01174|WAP_RAT Whey acidic protein OS=Rattus norvegicus\n"+
																		"GN=Wap PE=1 SV=2\n" +
																		"MRCSISLVLGLLALEVALARNLQEHVFNSVQSMCSDDSFSEDTECINCQTNEECAQNDMC\n" +
																		"CPSSCGRSCKTPVNIEVQKAGRCPWNPIQMIAAGPCPKDNPCSIDSDCSGTMKCCKNGCI\n" +
																		"MSCMDPEPKSPTVISFQ")); //protein FASTA sequence
		input3_2.setInterProScanInputParameters(params3);	
		
		// Print out the input parameters
		printInputParameters(input3_2, params3);
		
		InterProScanOutput interProScanOut3_2 = null;
		try{
			interProScanOut3_2 = clientUtils.interProScanSync(input3_2, TIMEOUT_SECONDS * 100000);
		}
		catch (Exception ex) {// should not happen
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut3_2 != null){
			printOutput(interProScanOut3_2, null, null);
		}
	}
	
	public void ips3() throws Exception{
		
		Thread.sleep(500); // back off a bit
		
		System.out.println();
		System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");
		System.out.println("Test IPS3: Testing the results returned by invoking the service directly and via wrapper.");
		System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");

		InterProScanClient interproscan = new InterProScanClient(
				"https://localhost:8443/wsrf/services/cagrid/InterProScan");
		InterProScanClientUtils clientUtils = new InterProScanClientUtils(
				interproscan);
		
		// the default value, i.e. all methods
		System.out.println();
		
		InterProScanInput input1 = new InterProScanInput();
		// uniprot:CO8G_HUMAN 
		input1.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:CO8G_HUMAN"));
		InterProScanInputParameters params1 = new InterProScanInputParameters();
		params1.setEmail("mannen@soiland-reyes.com");
		params1.setUseCRC(Boolean.TRUE);
		input1.setInterProScanInputParameters(params1);
		
		// Print out the input parameters
		printInputParameters(input1, params1);		
		
		InterProScanOutput interProScanOut1 = null;
		try{
			System.out.println("Invocation started at: "+ new Date().toString());
			interProScanOut1 = clientUtils.interProScanSync(input1, TIMEOUT_SECONDS * 100000);
			System.out.println("Invocation ended at: " + new Date().toString());
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
		// Print out the results
		if (interProScanOut1 != null){
			printOutput(interProScanOut1, null, null);
		}
	}
	
	
	private void printInputParameters(InterProScanInput input,
			InterProScanInputParameters params) {
		System.out.println("Input parameters:");
		String proteinString = getProteinString(input.getSequenceRepresentation());
		System.out.println(proteinString);
		if (params.getSignatureMethod()==null){
			System.out.println("Signature method: null; defaults to using all signature methods");
		}
		else{
			String methods = getSignatureMethodNames(params.getSignatureMethod());
			System.out.println("Signature method(s): " + methods);
		}
	}
	
	private void printOutput(InterProScanOutput output, Set<String> expectedDatabases, Set<String> expectedSignatureMethods){
		System.out.println("Output contains the following match(es):");		
		Set<String> foundDatabases = new HashSet<String>();
		Set<String> foundSignatureMethods = new HashSet<String>();
		for (ProteinSignatureMatch proteinSignatureMatch : output.getProteinSignatureMatches()){
			DatabaseMatch[] databaseMatches = proteinSignatureMatch.getDatabaseMatches();
			System.out.println("Match: " + proteinSignatureMatch.getName());
			for (DatabaseMatch databaseMatch : databaseMatches){
				foundDatabases.add(databaseMatch.getDatabase().getName().toLowerCase());
				ProteinSignatureLocation[] proteinSignatureLocations = databaseMatch.getProteinSignatureLocations();
				System.out.println("In database: " + databaseMatch.getDatabase().getName() + "; Number of occurrence(s): " + proteinSignatureLocations.length );
				for (ProteinSignatureLocation location : proteinSignatureLocations) {
					foundSignatureMethods.add(location.getSignatureMethod().getValue().toLowerCase());
					System.out.println("Sequence location start-end: " + location.getStart() + "-" + location.getEnd() + "; Signature method used: " + location.getSignatureMethod().getValue());
				}
			}
		}
		System.out.println("Protein (as returned by the service): " + output.getProtein().getId());

		if (expectedDatabases != null){
			if (foundDatabases.equals(expectedDatabases)){
				System.out.println("Matches found in all expected databases: " + foundDatabases.toString());
			}
			else{
				System.out.println("An error occured: expected databases do not match the found ones.");
				System.out.println("Expected databases: "+ expectedDatabases.toString());
				System.out.println("Found databases:" + foundDatabases.toString());
			}
		}
		if (expectedSignatureMethods != null){
			if (foundSignatureMethods.equals(expectedSignatureMethods)){
				System.out.println("Matches found using all expected signature methods: " + foundSignatureMethods.toString());
			}
			else{
				System.out.println("An error occured: expected signature methods do not match the found ones.");
				System.out.println("Expected signature methods: "+ expectedSignatureMethods.toString());
				System.out.println("Found signature methods: "+ foundSignatureMethods.toString());
			}
		}
	}

	private String getSignatureMethodNames(SignatureMethod[] sigMethds) {
		String methods = "";
		for (SignatureMethod method : sigMethds){
			methods += method.getValue()  + ", ";
		}
		methods = methods.substring(0, methods.length()-2);
		return methods;
	}

	private String getProteinString(SequenceRepresentation protein) {
		String proteinString = "";
		if (protein == null){
			proteinString = "Protein: null";
		}
		else if (protein instanceof FASTAProteinSequence){
			proteinString ="Protein (as FASTA sequence): "+ ((FASTAProteinSequence)protein).getSequence();
		}
		else{
			proteinString ="Protein (as database identifier): "+ ((ProteinSequenceIdentifier)protein).getSequenceId();
		}
		return proteinString;
	}
}
