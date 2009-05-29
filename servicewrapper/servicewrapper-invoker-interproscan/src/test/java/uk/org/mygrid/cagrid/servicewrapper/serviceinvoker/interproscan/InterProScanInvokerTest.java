package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan;

import static org.junit.Assert.*;

import org.apache.xmlbeans.XmlBeans;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.Invoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;


public class InterProScanInvokerTest {

	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String JOB_ID = "iprscan-20090529-1051066486";
	private Invoker<InterProScanInput, byte[]> invoker;

	@Before
	public void findInvoker() throws InvokerException {
		invoker = new InterProScanInvoker();		
	}

	@Ignore
	@Test
	public void invokeInterProScan() throws Exception {
		InterProScanInput analyticalServiceInput = new InterProScanInput();
		InputParams inputParams = uk.ac.ebi.www.wsinterproscan.InputParams.Factory.newInstance();
		inputParams.setEmail("mannen@soiland-reyes.com");
		inputParams.setAsync(true);
		inputParams.setSeqtype("P");
		analyticalServiceInput.setParams(inputParams);
		
		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();
		content[0].setContent("uniprot:wap_rat");
		content[0].setType("sequence");
		analyticalServiceInput.setContent(content);
		
		String jobID = invoker.runJob(analyticalServiceInput);
		String status = "RUNNING";
		while (status.equals("RUNNING")) {
			Thread.sleep(1500);
			status = invoker.checkStatus(jobID);
			System.out.println(status);
		}
		byte[] poll = invoker.poll(jobID);
		System.out.println(new String(poll));
	}
	
	@Test
	public void getStatus() throws Exception {
		assertEquals("DONE", invoker.checkStatus(JOB_ID));
	}

	@Test
	public void poll() throws Exception {
		byte[] poll = invoker.poll(JOB_ID);
		String pollXML = new String(poll, ISO_8859_1);
		assertTrue(pollXML.startsWith("<?xml"));
		assertTrue(pollXML.contains("sp|P01174|WAP_RAT"));
		assertTrue(pollXML.contains("GO:0030414"));
	}
	
}
