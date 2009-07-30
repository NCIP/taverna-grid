package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client;

import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.stubs.types.NCBIBlastJobReference;

public class TimeOutException extends ClientException {
	private static final long serialVersionUID = -1544719477638678639L;

	public TimeOutException(String message,
			NCBIBlastJobReference jobReference) {
		super(message, jobReference);
	}

	public TimeOutException(String message, Throwable cause,
			NCBIBlastJobReference jobReference) {
		super(message, cause, jobReference);
	}

	public TimeOutException(NCBIBlastJobReference jobReference) {
		super(jobReference);
	}

	public TimeOutException(Throwable cause,
			NCBIBlastJobReference jobReference) {
		super(cause, jobReference);
	}

}