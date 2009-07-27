package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;

public class ClientException extends RuntimeException {

	private static final long serialVersionUID = -8155148068226421609L;
	private final InterProScanJobReference jobReference;

	public ClientException(InterProScanJobReference jobReference) {
		super();
		this.jobReference = jobReference;
	}

	public ClientException(String message, Throwable cause,
			InterProScanJobReference jobReference) {
		super(message, cause);
		this.jobReference = jobReference;
	}

	public ClientException(String message, InterProScanJobReference jobReference) {
		super(message);
		this.jobReference = jobReference;
	}

	public ClientException(Throwable cause,
			InterProScanJobReference jobReference) {
		super(cause);
		this.jobReference = jobReference;
	}

	public InterProScanJobReference getJobReference() {
		return jobReference;
	}
}