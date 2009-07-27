package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.client;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference;

public class TimeOutException extends ClientException {
	private static final long serialVersionUID = -1544719477638678639L;

	public TimeOutException(String message,
			InterProScanJobReference jobReference) {
		super(message, jobReference);
	}

	public TimeOutException(String message, Throwable cause,
			InterProScanJobReference jobReference) {
		super(message, cause, jobReference);
	}

	public TimeOutException(InterProScanJobReference jobReference) {
		super(jobReference);
	}

	public TimeOutException(Throwable cause,
			InterProScanJobReference jobReference) {
		super(cause, jobReference);
	}

}