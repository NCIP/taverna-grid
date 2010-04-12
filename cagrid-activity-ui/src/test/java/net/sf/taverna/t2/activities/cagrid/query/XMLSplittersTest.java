package net.sf.taverna.t2.activities.cagrid.query;

import static org.junit.Assert.assertNotNull;

import net.sf.taverna.cagrid.activity.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.cagrid.activity.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.cagrid.ui.servicedescriptions.XMLInputSplitterActivityIcon;
import net.sf.taverna.cagrid.ui.servicedescriptions.XMLOutputSplitterActivityIcon;

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