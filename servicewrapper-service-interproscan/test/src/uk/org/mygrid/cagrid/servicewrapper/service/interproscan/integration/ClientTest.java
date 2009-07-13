package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.integration;

import gov.nih.nci.cagrid.common.Utils;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.utils.XmlUtils;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
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
		final Object lock = new Object();
		jobClient.subscribe(InterProScanJobConstants.INTERPROSCANOUTPUT,
				new NotifyCallback() {
					@Override
					public void deliver(List topicPath,
							EndpointReferenceType producer, Object message) {
							ResourcePropertyValueChangeNotificationType changeMessage = ((ResourcePropertyValueChangeNotificationElementType) message)
									.getResourcePropertyValueChangeNotification();
							MessageElement messageElement = changeMessage.getNewValue().get_any()[0];
							StringReader reader = new StringReader(XmlUtils
									.toString(messageElement));
							InterProScanOutput outputs;
							try {
								outputs = (InterProScanOutput) Utils.deserializeObject(reader,
												InterProScanOutput.class);
								System.out.println("Protein: "
										+ outputs.getProtein().getId());
							} catch (Exception e) {
								System.err.println("Could not deserialise InterProScanOutput " + messageElement);
							}
							synchronized (lock) {
								lock.notifyAll();
							}
					}
				});

		synchronized (lock) {
			lock.wait(50 * 1000);
		}
	}
}
