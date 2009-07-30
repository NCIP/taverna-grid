package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.client;

import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.stubs.types.NCBIBlastJobReference;

public class ClientException extends RuntimeException {

	private static final long serialVersionUID = -8155148068226421609L;
	private final NCBIBlastJobReference jobReference;

	public ClientException(NCBIBlastJobReference jobReference) {
		super();
		this.jobReference = jobReference;
	}

	public ClientException(String message, Throwable cause,
			NCBIBlastJobReference jobReference) {
		super(message, cause);
		this.jobReference = jobReference;
	}

	public ClientException(String message, NCBIBlastJobReference jobReference) {
		super(message);
		this.jobReference = jobReference;
	}

	public ClientException(Throwable cause,
			NCBIBlastJobReference jobReference) {
		super(cause);
		this.jobReference = jobReference;
	}

	public NCBIBlastJobReference getJobReference() {
		return jobReference;
	}
}