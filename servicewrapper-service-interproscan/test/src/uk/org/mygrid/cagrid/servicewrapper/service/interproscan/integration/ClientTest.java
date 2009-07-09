package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import java.rmi.RemoteException;

import org.apache.axis.types.URI.MalformedURIException;
import org.globus.wsrf.container.ContainerException;

import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.client.InterProScanJobClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;


public class ClientTest {

	public static void main(String[] args) throws MalformedURIException, RemoteException, ContainerException, InterruptedException {
		InterProScanClient interproscan = new InterProScanClient("http://130.88.193.109:8080/wsrf/services/cagrid/InterProScan");
		InterProScanInput input;
		input = new InterProScanInput();
		input.setSequenceRepresentation(new FASTAProteinSequence("uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");

		// Using only PatternScan is a bit faster
		params.setSignatureMethod(new SignatureMethod[]{SignatureMethod.PatternScan});
		
		input.setInterProScanInputParameters(params);
		InterProScanJobReference interProJob = interproscan.interProScan(input);
		
		
		InterProScanJobClient jobClient = new InterProScanJobClient(interProJob.getEndpointReference());
		Object status = jobClient.getStatus();
		System.out.println("First status: " +  status);
	
		Thread.sleep(500);
		status = jobClient.getStatus();
		System.out.println("Second status: " +  status);

		Thread.sleep(500);
		status = jobClient.getStatus();
		System.out.println("Third status: " +  status);

		
		InterProScanOutput outputs = jobClient.getOutputs();
		System.out.println("Protein: " + outputs.getProtein().getId());
		
		
		
	}
	
}
