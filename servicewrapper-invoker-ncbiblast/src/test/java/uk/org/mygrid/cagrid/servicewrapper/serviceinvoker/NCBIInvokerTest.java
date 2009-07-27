package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker;

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

import uk.ac.ebi.www.wsncbiblast.Data;
import uk.ac.ebi.www.wsncbiblast.InputParams;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.DummyNCBIBlastInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvokerImpl;

public class NCBIInvokerTest {

	private static final Namespace NCBIBLAST_NS = Namespace.getNamespace("http://www.ebi.ac.uk/schema"); // sic
	private static final String EXPIRED_JOB_ID = "iprscan-20090529-1051066486";
	private NCBIBlastInvoker invoker;

	@Before
	public void findInvoker() throws InvokerException {
	//	invoker = new DummyNCBIBlastInvoker();		
		invoker = new NCBIBlastInvokerImpl();		
		
		// Only use the real InterProScanInvoker in integration tests		
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void invokeInterProScan() throws Exception {
		NCBIBlastInput analyticalServiceInput = new NCBIBlastInput();
		uk.ac.ebi.www.wsncbiblast.InputParams inputParams = uk.ac.ebi.www.wsncbiblast.InputParams.Factory.newInstance();
		inputParams.setEmail("mannen@soiland-reyes.com");
		inputParams.setDatabase("uniprot");
		inputParams.setProgram("blastp");
		analyticalServiceInput.setParams(inputParams);
		
		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();
		content[0].setContent("uniprot:wap_rat");
		content[0].setType("sequence");
		analyticalServiceInput.setContent(content);
		
		String jobID = invoker.runJob(analyticalServiceInput );
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
			System.out.println(status);
			Thread.sleep(450);
			status = invoker.checkStatus(jobID);
		}
		assertEquals("Status was not DONE", "DONE", status);
		
		Document pollXML = invoker.poll(jobID);
		Element rootElement = pollXML.getRootElement();
		assertEquals("EBIApplicationResult", rootElement.getName());
		assertEquals(NCBIBLAST_NS,
				rootElement.getNamespace());
		
		
		Element hit = rootElement.getChild("SequenceSimilaritySearchResult", NCBIBLAST_NS).getChild("hits", NCBIBLAST_NS).getChild("hit", NCBIBLAST_NS);
		assertNotNull("Hit element not returned from service", hit);
		
		// What we searched for
		assertEquals("WAP_RAT", hit.getAttributeValue("id"));
		Element alignment = hit.getChild("alignments", NCBIBLAST_NS).getChild("alignment", NCBIBLAST_NS);
		assertEquals("Unexpected number of bits in hit", "298", alignment.getChildText("bits", NCBIBLAST_NS));
		
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
