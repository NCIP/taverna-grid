package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client;

import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.cagrid.metadata.service.Fault;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.utils.XmlUtils;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import uk.org.mygrid.cagrid.domain.common.JobStatus;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.client.InterProScanJobClient;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.common.InterProScanJobConstants;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;

public class InterProScanClientUtils {

	private static Logger logger = Logger
			.getLogger(InterProScanClientUtils.class);

	private static final int DEFAULT_REFRESH_MS = 500;

	protected final InterProScanClient client;

	/**
	 * Construct a InterProScanClientUtils.
	 * 
	 * @param client
	 *            The initialized InterProScanClient to use for the job
	 *            submission
	 * @param timeoutMs
	 *            The timeout
	 * @param refreshMs
	 */
	public InterProScanClientUtils(InterProScanClient client) {
		this.client = client;
	}

	public InterProScanOutput interProScanSync(
			InterProScanInput interProScanInput, int timeoutMs)
			throws RemoteException, ClientException {
		return interProScanSync(interProScanInput, timeoutMs,
				DEFAULT_REFRESH_MS);
	}

	public InterProScanOutput interProScanSync(
			InterProScanInput interProScanInput, int timeoutMs, int refreshMs)
			throws RemoteException, ClientException {
		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.MILLISECOND, timeoutMs);
		InterProScanJobReference job = client.interProScan(interProScanInput);
		InterProScanJobClient jobClient;
		try {
			jobClient = new InterProScanJobClient(job.getEndpointReference());
		} catch (MalformedURIException e) {
			throw new RuntimeException(
					"Unexpected malformed URI in job endpoint reference: "
							+ job.getEndpointReference(), e);
		}
		JobStatus status = JobStatus.pending;
		while (timeout.after(Calendar.getInstance())) {
			status = jobClient.getStatus();
			if (!(status.equals(JobStatus.pending) || status
					.equals(JobStatus.running))) {
				break;
			}
			try {
				Thread.sleep(refreshMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		status = jobClient.getStatus();
		if (status.equals(JobStatus.pending)
				|| status.equals(JobStatus.running)) {
			throw new TimeOutException("Timed out, status is still " + status,
					job);
		}
		if (status.equals(JobStatus.done)) {
			return jobClient.getOutputs();
		}
		throw new ClientException("Can't get job output, status is " + status,
				job);
	}

	public void interProScanAsync(InterProScanInput interProScanInput,
			JobCallBack callback) {
		InterProScanJobReference job;
		try {
			job = client.interProScan(interProScanInput);
		} catch (RemoteException e1) {
			throw new ClientException("Can't submit job", e1, null);
		}
		InterProScanJobClient jobClient;
		try {
			try {
				jobClient = new InterProScanJobClient(job
						.getEndpointReference());
			} catch (MalformedURIException e) {
				throw new RuntimeException(
						"Unexpected malformed URI in job endpoint reference: "
								+ job.getEndpointReference(), e);
			} catch (RemoteException e) {
				throw new ClientException("Can't make job client", e, job);
			}
			CallbackProxy callBackProxy = new CallbackProxy(callback);
			try {
				jobClient.subscribe(InterProScanJobConstants.INTERPROSCANOUTPUT,
						callBackProxy);
				jobClient.subscribe(InterProScanJobConstants.JOBSTATUS,
						callBackProxy);
				jobClient.subscribe(InterProScanJobConstants.FAULT,
						callBackProxy);
			} catch (RemoteException e) {
				throw new ClientException("Can't subscribe to job changes", e,
						job);
			} catch (ContainerException e) {
				throw new ClientException("Can't subscribe to job changes", e,
						job);
			}
		} catch (MalformedURIException e) {
			throw new RuntimeException(
					"Unexpected malformed URI in job endpoint reference: "
							+ job.getEndpointReference(), e);
		}
	}

	protected static class CallbackProxy implements NotifyCallback {
		private final JobCallBack<InterProScanOutput> callback;

		public CallbackProxy(JobCallBack<InterProScanOutput> callback) {
			this.callback = callback;
		}

		@SuppressWarnings("unchecked")
		public void deliver(List topicPath, EndpointReferenceType producer,
				Object message) {
			ResourcePropertyValueChangeNotificationType changeMessage = ((ResourcePropertyValueChangeNotificationElementType) message)
					.getResourcePropertyValueChangeNotification();
			if (changeMessage == null) {
				return;
			}
			QName topic = (QName) topicPath.get(0);
			Class<?> valueClass;
			if (topic.equals(InterProScanJobConstants.JOBSTATUS)) {
				valueClass = JobStatus.class;
			} else if (topic
					.equals(InterProScanJobConstants.INTERPROSCANOUTPUT)) {
				valueClass = InterProScanOutput.class;
			} else if (topic.equals(InterProScanJobConstants.FAULT)) {
				valueClass = Fault.class;
			} else {
				logger.error("Invalid topic: " + topic);
				return;
			}

			Object oldValue = null;
			if (changeMessage.getOldValue() != null
					&& changeMessage.getOldValue().get_any().length > 0) {
				MessageElement oldElem = changeMessage.getOldValue().get_any()[0];
				StringReader reader = new StringReader(XmlUtils
						.toString(oldElem));
				try {
					oldValue = Utils.deserializeObject(reader, valueClass);
				} catch (Exception e) {
					throw new RuntimeException("Can't deserialize " + oldElem,
							e);
				}
			}

			Object newValue = null;
			if (changeMessage.getNewValue() != null
					&& changeMessage.getNewValue().get_any().length > 0) {
				MessageElement newElem = changeMessage.getNewValue().get_any()[0];
				StringReader reader = new StringReader(XmlUtils
						.toString(newElem));
				try {
					newValue = Utils.deserializeObject(reader, valueClass);
				} catch (Exception e) {
					throw new RuntimeException("Can't deserialize " + newElem,
							e);
				}
			}
			if (valueClass == JobStatus.class) {
				callback.jobStatusChanged((JobStatus) oldValue,
						(JobStatus) newValue);
			} else if (valueClass == InterProScanOutput.class) {
				callback.jobOutputReceived((InterProScanOutput)newValue);
			} else {
				callback.jobError((Fault) newValue);
			}

		}
	}

}
