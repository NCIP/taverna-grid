package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import org.jdom.Document;

import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;

public class InterProScanConverter {

	InterProScanImporter importer = new InterProScanImporter();

	public InterProScanOutput convertInterProScanOutput(Document data) {
		return importer.importInterProScanOutput(data);
	}

}
