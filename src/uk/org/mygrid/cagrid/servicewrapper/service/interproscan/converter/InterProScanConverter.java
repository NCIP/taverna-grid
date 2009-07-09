package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

import java.math.BigInteger;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import uk.org.mygrid.cagrid.domain.interproscan.InterProScanOutput;
import uk.org.mygrid.cagrid.domain.interproscan.Protein;

public class InterProScanConverter {

	public InterProScanOutput convertInterProScanOutput(Document data) {
		InterProScanOutput output = new InterProScanOutput();
		Element rootElement = data.getRootElement();
		
		
		Element protein = rootElement.getChild("interpro_matches", INTERPROSCAN_NS).getChild("protein", INTERPROSCAN_NS);
		
		for (Element interpro : (List<Element>)protein.getChildren("interpro", INTERPROSCAN_NS)) {
			for (Element match : (List<Element>)interpro.getChildren("match", INTERPROSCAN_NS)) {				

			}
		}
		
		output.setProtein(new Protein("asdasd", "asdasd", BigInteger.valueOf(123)));
		return output;
	}
}
