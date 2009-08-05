package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClientUtils;


/**
 * 
 * IPS2: protein parameter test
 *  
 * Test empty/null: Should produce error
 * 
 * Test invalid value: Should produce error
 *
 */
public class InterProScanProteinParameterTest {
	
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
	
	
	// Test that passing null for the protein parameter produces an error
	@Test(expected=org.apache.axis.AxisFault.class)
	public void failsNullProtein() throws Exception{
		InterProScanInput input = new InterProScanInput();
		input.setSequenceRepresentation(null);
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");
		params.setSignatureMethod(null);
		input.setInterProScanInputParameters(params);
				
		// Should fail
		clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 100000);
	}
	
	// Test that passing an invalid protein parameter produces an error
	@Test(expected=org.apache.axis.AxisFault.class)
	public void failsInvalidProtein() throws Exception{
		InterProScanInput input = new InterProScanInput();
		input.setSequenceRepresentation(new ProteinSequenceIdentifier("BLA_BLA")); //incorrect sequence
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");
		params.setSignatureMethod(null);
		input.setInterProScanInputParameters(params);
			
		// Should fail
		clientUtils.interProScanSync(input, TIMEOUT_SECONDS * 100000);
	}
}
