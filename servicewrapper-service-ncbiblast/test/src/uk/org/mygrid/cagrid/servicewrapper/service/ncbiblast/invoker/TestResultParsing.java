package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker;

import static org.junit.Assert.*;
import gov.nih.nci.cagrid.common.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import uk.ac.ebi.ncbiblast.EBIApplicationResult;

public class TestResultParsing {
	@Test
	public void deserialisingNcbiResults() throws Exception {

		InputStream results = getClass()
				.getResourceAsStream("/ncbiresults.xml");
		Reader xmlReader = new InputStreamReader(results);
		EBIApplicationResult eBIApplicationResult;
		eBIApplicationResult = (EBIApplicationResult) Utils.deserializeObject(
				xmlReader, EBIApplicationResult.class);
		assertTrue(eBIApplicationResult.getHeader().getCommandLine()
				.getCommand().contains(" -M BLOSUM62 "));
	}
}
