package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.JDOMException;

import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput;

public class InterProScanConverter {

	InterProScanImporter importer = new InterProScanImporter();

	InterProScanExporter exporter = new InterProScanExporter();
	
	public InterProScanOutput convertInterProScanOutput(Document data) throws ConverterException {
		try {
			return importer.importInterProScanOutput(data);
		} catch (XmlException e) {
			throw new ConverterException("Invalid XML", e);
		} catch (JDOMException e) {
			throw new ConverterException("Invalid JDOM", e);
		}
	}

	public InterProScanInput convertInterProScanInput(
			uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput interProScanInput) throws ConverterException{
		return exporter.exportInterProScanInput(interProScanInput);
	}

}
