
import java.rmi.RemoteException;

import org.apache.axis.types.URI.MalformedURIException;
import org.globus.wsrf.container.ContainerException;

import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.client.InterProScanJobClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;


public class ClientTest {

	public static void main(String[] args) throws MalformedURIException, RemoteException, ContainerException, InterruptedException {
		InterProScanClient interproscan = new InterProScanClient("http://130.88.193.109:8080/wsrf/services/cagrid/InterProScan");
		InterProScanInput input;
		input = new InterProScanInput();
		input.setSequenceRepresentation(new ProteinSequenceIdentifier("uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");
		input.setInterProScanInputParameters(params);
		InterProScanJobReference interProJob = interproscan.interProScan(input);
		
		
		System.out.println(interProJob);
				
		InterProScanJobClient jobClient = new InterProScanJobClient(interProJob.getEndpointReference());
		
		InterProScanInput returnedInputs = jobClient.getInputs();
		System.out.println(returnedInputs); // Should be null
	
		Thread.sleep(2000);
		jobClient.getStatus("wrong way!");
	}
	
}
