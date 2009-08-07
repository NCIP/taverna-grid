package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.JDOMException;

import uk.org.mygrid.cagrid.domain.common.Database;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTInput;
import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInput;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.SequenceDatabase;

public class NCBIBlastConverter {
	NCBIBlastImporter importer = new NCBIBlastImporter();

	NCBIBlastExporter exporter = new NCBIBlastExporter();

	public NCBIBLASTOutput convertNCBIBlastOutput(Document data)
			throws ConverterException {
		try {
			return importer.importNCBIBlastOutput(data);
		} catch (XmlException e) {
			throw new ConverterException("Could not parse XML from " + data, e);
		} catch (JDOMException e) {
			throw new ConverterException("Could not parse XML from " + data, e);
		}
	}

	public NCBIBlastInput convertNCBIBlastInput(NCBIBLASTInput nCBIBlastInput)
			throws ConverterException {
		return exporter.exportNCBIBlastInput(nCBIBlastInput);

	}

	public List<Database> convertDatabases(List<SequenceDatabase> sequenceDBs) {
		List<Database> databases = new ArrayList<Database>();
		for (SequenceDatabase sequenceDB : sequenceDBs) {
			Database database = new Database();
			database.setName(sequenceDB.getName());
		}
		return databases;
	}

}
