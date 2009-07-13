package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import java.rmi.RemoteException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.encoding.Deserializer;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.JobStatus;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client.InterProScanClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.client.InterProScanJobClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.common.InterProScanJobConstants;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

public class ClientTest {

	public static void main(String[] args) throws MalformedURIException,
			RemoteException, ContainerException, InterruptedException {
		InterProScanClient interproscan = new InterProScanClient(
				"http://130.88.193.109:8080/wsrf/services/cagrid/InterProScan");
		InterProScanInput input;
		input = new InterProScanInput();
		input.setSequenceRepresentation(new FASTAProteinSequence(
				"uniprot:wap_rat"));
		InterProScanInputParameters params = new InterProScanInputParameters();
		params.setEmail("mannen@soiland-reyes.com");

		// Using only PatternScan is a bit faster
		params.setSignatureMethod(new SignatureMethod[] { SignatureMethod.PatternScan });

		input.setInterProScanInputParameters(params);
		InterProScanJobReference interProJob = interproscan.interProScan(input);

		final InterProScanJobClient jobClient = new InterProScanJobClient(
				interProJob.getEndpointReference());
		// Object status = jobClient.getStatus();
		// System.out.println("First status: " + status);
		
		final Object lock = new Object();
		jobClient.subscribe(InterProScanJobConstants.INTERPROSCANOUTPUT,
//		jobClient.subscribe(InterProScanJobConstants.JOBSTATUS,
				new NotifyCallback() {
					@Override
					public void deliver(List topicPath,
							EndpointReferenceType producer, Object message) {
						try {
							ResourcePropertyValueChangeNotificationType changeMessage = ((ResourcePropertyValueChangeNotificationElementType) message)
									.getResourcePropertyValueChangeNotification();
							
							String status = changeMessage.getNewValue().get_any()[0].getValue();
							System.err.println("It's a " + changeMessage.getNewValue().get_any()[0]);
							JobStatus jobStatus = JobStatus.fromValue(status);
							// or simply a second call with..
							// jobStatus = jobClient.getJobStatus();
							
							if (jobStatus.equals(JobStatus.done)) {
								InterProScanOutput outputs = jobClient
										.getOutputs();
								System.out.println("Protein: "
										+ outputs.getProtein().getId());
								synchronized(lock) {
									lock.notifyAll();
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});

		synchronized(lock) {
			lock.wait(50* 1000);
		}
	}

}
