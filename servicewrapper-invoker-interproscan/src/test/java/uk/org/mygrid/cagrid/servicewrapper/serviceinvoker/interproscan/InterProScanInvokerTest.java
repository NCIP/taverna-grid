package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;


public class InterProScanInvokerTest {

	private static final Namespace INTERPROSCAN_NS = Namespace.getNamespace("http://www.ebi.ac.uk/schema"); // sic
	private static final String EXPIRED_JOB_ID = "iprscan-20090529-1051066486";
	private InterProScanInvoker invoker;

	@Before
	public void findInvoker() throws InvokerException {
		invoker = new DummyInterProScanInvoker();		
	//	invoker = new InterProScanInvokerImpl();		
		
		// Only use the real InterProScanInvoker in integration tests		
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void invokeInterProScan() throws Exception {
		InterProScanInput analyticalServiceInput = new InterProScanInput();
		InputParams inputParams = uk.ac.ebi.www.wsinterproscan.InputParams.Factory.newInstance();
		inputParams.setEmail("mannen@soiland-reyes.com");
		inputParams.setSeqtype("P");
		inputParams.setApp("PatternScan");
		analyticalServiceInput.setParams(inputParams);
		
		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();
		content[0].setContent("uniprot:wap_rat");
		content[0].setType("sequence");
		analyticalServiceInput.setContent(content);
		
		String jobID = invoker.runJob(analyticalServiceInput);
		String status = invoker.checkStatus(jobID);
		assertTrue("Expected status RUNNING/PENDING, but was " + status, status
				.equals("RUNNING")
				|| status.equals("PENDING"));
		
		try {
			invoker.poll(jobID);
			fail("premature poll did not fail");
		} catch (InvokerException ex) {
			// expected			
		}
		
		while (status.equals("RUNNING") || status.equals("PENDING")) {
			Thread.sleep(450);
			status = invoker.checkStatus(jobID);
		}
		assertEquals("Status was not DONE", "DONE", status);
		
		Document pollXML = invoker.poll(jobID);
		Element rootElement = pollXML.getRootElement();
		assertEquals("EBIInterProScanResults", rootElement.getName());
		assertEquals(INTERPROSCAN_NS,
				rootElement.getNamespace());
		
		
		Element protein = rootElement.getChild("interpro_matches", INTERPROSCAN_NS).getChild("protein", INTERPROSCAN_NS);
		assertNotNull("Protein element not returned from service", protein);
		
		// What we searched for
		assertEquals("sp|P01174|WAP_RAT", protein.getAttributeValue("id"));
		
		String matchID = "PS00317";
		boolean found = false;		
		for (Element interpro : (List<Element>)protein.getChildren("interpro", INTERPROSCAN_NS)) {
			for (Element match : (List<Element>)interpro.getChildren("match", INTERPROSCAN_NS)) {				
				if (match.getAttributeValue("id").equals(matchID)) {
					found = true;
				}
			}
		}
		assertTrue("Did not find interpro match " + matchID, found);
		
	}
	
	@Test
	public void getStatusNotFound() throws Exception {
		assertEquals("NOT_FOUND", invoker.checkStatus(EXPIRED_JOB_ID));
	}

	@Test(expected=InvokerException.class)
	public void pollExpiredFails() throws Exception {
		invoker.poll(EXPIRED_JOB_ID);
	}
	
}
