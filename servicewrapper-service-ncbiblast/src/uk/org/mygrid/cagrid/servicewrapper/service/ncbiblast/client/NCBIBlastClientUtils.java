package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client;

import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.cagrid.metadata.service.Fault;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.utils.XmlUtils;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import uk.ac.ebi.ncbiblast.EBIApplicationResult;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBlastOutput;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.client.NCBIBlastJobClient;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.common.NCBIBlastJobConstants;
import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.stubs.types.NCBIBlastJobReference;
import uk.org.mygrid.cagrid.valuedomains.JobStatus;

public class NCBIBlastClientUtils {

	private static Logger logger = Logger.getLogger(NCBIBlastClientUtils.class);

	private static final int DEFAULT_REFRESH_MS = 500;

	protected final NCBIBlastClient client;

	private Map<NCBIBlastInput, NCBIBlastJobClient> jobClients = new HashMap<NCBIBlastInput, NCBIBlastJobClient>();

	/**
	 * Construct a NCBIBlastClientUtils.
	 * 
	 * @param client
	 *            The initialized NCBIBlastClient to use for the job submission
	 * @param timeoutMs
	 *            The timeout
	 * @param refreshMs
	 */
	public NCBIBlastClientUtils(NCBIBlastClient client) {
		this.client = client;
	}

	public NCBIBlastJobClient getJobClientForInput(NCBIBlastInput input) {
		synchronized (jobClients) {
			return jobClients.get(input);
		}
	}

	public NCBIBlastOutput ncbiBlastSync(NCBIBlastInput nCBIBlastInput,
			int timeoutMs) throws RemoteException, ClientException {
		return ncbiBlastSync(nCBIBlastInput, timeoutMs, DEFAULT_REFRESH_MS);
	}

	public NCBIBlastOutput ncbiBlastSync(NCBIBlastInput nCBIBlastInput,
			int timeoutMs, int refreshMs) throws RemoteException,
			ClientException {
		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.MILLISECOND, timeoutMs);

		NCBIBlastJobReference job = client.ncbiBlast(nCBIBlastInput);
		NCBIBlastJobClient jobClient;
		try {
			jobClient = new NCBIBlastJobClient(job.getEndpointReference());
		} catch (MalformedURIException e) {
			throw new RuntimeException(
					"Unexpected malformed URI in job endpoint reference: "
							+ job.getEndpointReference(), e);
		}
		synchronized (jobClients) {
			jobClients.put(nCBIBlastInput, jobClient);
		}
		JobStatus status = JobStatus.pending;
		while (timeout.after(Calendar.getInstance())) {
			status = jobClient.getStatus().getStatus();
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
		status = jobClient.getStatus().getStatus();
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

	public EBIApplicationResult getOriginalOutput(NCBIBlastJobClient jobClient)
			throws RemoteException, Exception {
		GetResourcePropertyResponse resourceResp = jobClient
				.getResourceProperty(NCBIBlastJobConstants.EBIAPPLICATIONRESULT);
		MessageElement[] messageElements = resourceResp.get_any();
		if (messageElements == null) {
			logger.warn("No output document");
			return null;
		}
		MessageElement messageElement = messageElements[0];
		StringReader reader = new StringReader(XmlUtils
				.toString(messageElement));
		EBIApplicationResult originalOutput = (EBIApplicationResult) Utils
				.deserializeObject(reader, EBIApplicationResult.class);
		return originalOutput;
	}

	@SuppressWarnings("unchecked")
	public void ncbiBlastAsync(NCBIBlastInput nCBIBlastInput,
			JobCallBack callback) {
		NCBIBlastJobReference job;
		try {
			job = client.ncbiBlast(nCBIBlastInput);
		} catch (RemoteException e1) {
			throw new ClientException("Can't submit job", e1, null);
		}
		NCBIBlastJobClient jobClient;
		try {
			try {
				jobClient = new NCBIBlastJobClient(job.getEndpointReference());
			} catch (MalformedURIException e) {
				throw new RuntimeException(
						"Unexpected malformed URI in job endpoint reference: "
								+ job.getEndpointReference(), e);
			} catch (RemoteException e) {
				throw new ClientException("Can't make job client", e, job);
			}
			synchronized (jobClients) {
				jobClients.put(nCBIBlastInput, jobClient);
			}

			CallbackProxy callBackProxy = new CallbackProxy(callback);
			try {
				jobClient.subscribe(NCBIBlastJobConstants.NCBIBLASTOUTPUT,
						callBackProxy);
				jobClient.subscribe(NCBIBlastJobConstants.JOB,
						callBackProxy);
				jobClient.subscribe(NCBIBlastJobConstants.FAULT, callBackProxy);
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
		private final JobCallBack<NCBIBlastOutput> callback;

		public CallbackProxy(JobCallBack<NCBIBlastOutput> callback) {
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
			if (topic.equals(NCBIBlastJobConstants.JOB)) {
				valueClass = JobStatus.class;
			} else if (topic.equals(NCBIBlastJobConstants.NCBIBLASTOUTPUT)) {
				valueClass = NCBIBlastOutput.class;
			} else if (topic.equals(NCBIBlastJobConstants.FAULT)) {
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
			} else if (valueClass == NCBIBlastOutput.class) {
				callback.jobOutputReceived((NCBIBlastOutput) newValue);
			} else {
				callback.jobError((Fault) newValue);
			}

		}
	}

}
