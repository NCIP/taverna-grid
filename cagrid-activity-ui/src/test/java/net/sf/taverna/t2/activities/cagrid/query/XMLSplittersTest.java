package net.sf.taverna.t2.activities.cagrid.query;

import static org.junit.Assert.assertNotNull;

import net.sf.taverna.t2.activities.cagrid.servicedescriptions.XMLInputSplitterActivityIcon;
import net.sf.taverna.t2.activities.cagrid.servicedescriptions.XMLOutputSplitterActivityIcon;
import net.sf.taverna.t2.activities.cagrid.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.t2.activities.cagrid.xmlsplitter.XMLOutputSplitterActivity;

import org.junit.Test;

public class XMLSplittersTest {

	@Test
	public void testXMLInputSplitterIcon() {
		assertNotNull(new XMLInputSplitterActivityIcon().getIcon(new XMLInputSplitterActivity()));
	}
	
	@Test
	public void testXMLOutputSplitterIcon() {
		assertNotNull(new XMLOutputSplitterActivityIcon().getIcon(new XMLOutputSplitterActivity()));
	}
}