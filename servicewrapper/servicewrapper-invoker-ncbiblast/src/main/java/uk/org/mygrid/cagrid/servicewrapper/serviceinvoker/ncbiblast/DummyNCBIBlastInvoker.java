package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.SequenceDatabase.SequenceType;

public class DummyNCBIBlastInvoker implements NCBIBlastInvoker {

	private static final String DUMMY_OUTPUT = "ncbiblast.xml";
	private static final String DUMMY_JOB_ID = "dummy-job-id";
	private long lastRun;
	private NCBIBlastInput lastInput;

	public String checkStatus(String jobID) throws InvokerException {

		if (!jobID.equals(DUMMY_JOB_ID)) {
			return "NOT_FOUND";
		}
		long runFor = now() - lastRun;
		if (runFor < 250) {
			return "PENDING";
		} else if (runFor < 600) {
			return "RUNNING";
		} else {
			return "DONE";
		}
	}

	public Document poll(String jobID) throws InvokerException {
		if (!checkStatus(jobID).equals("DONE")) {
			throw new InvokerException();
		}
		return getDummyOutput();
	}

	public String runJob(NCBIBlastInput input) throws InvokerException {
		lastInput = input;
		lastRun = now();
		return DUMMY_JOB_ID;
	}

	public Document getDummyOutput() {
		try {
			InputStream resourceAsStream = getClass().getResourceAsStream(
					DUMMY_OUTPUT);
			SAXBuilder saxBuilder = new SAXBuilder();
			return saxBuilder.build(resourceAsStream);
		} catch (IOException e) {
			throw new RuntimeException("Can't read " + DUMMY_OUTPUT, e);
		} catch (JDOMException e) {
			throw new RuntimeException("Can't parse " + DUMMY_OUTPUT, e);
		}
	}

	public NCBIBlastInput getLastInput() {
		return lastInput;
	}

	protected long now() {
		return new Date().getTime();
	}

	public List<SequenceDatabase> getDatabases() {
		List<SequenceDatabase> databases = new ArrayList<SequenceDatabase>();
		databases.add(new SequenceDatabase("uniprot", SequenceType.protein));
		databases.add(new SequenceDatabase("swissprot", SequenceType.protein));
		databases.add(new SequenceDatabase("em_rel_env",
				SequenceType.nucleotide));
		return databases;
	}

}
