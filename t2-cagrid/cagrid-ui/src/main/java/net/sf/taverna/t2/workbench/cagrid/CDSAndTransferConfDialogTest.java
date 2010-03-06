package net.sf.taverna.t2.workbench.cagrid;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CDSAndTransferConfDialogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	
		/*
		CDSAndTransferConfDialog cDialog = new CDSAndTransferConfDialog(true,false);
		cDialog.pack();
		cDialog.setLocationRelativeTo(null);
		cDialog.setVisible(true);
		//cDialog;
		System.out.println("hehe");
		*/
		//T2Util.parseWorkflow(dataflowE);
		String workflowName = "cagrid-ui/src/main/resources/transfer.xml";
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilderFactory.setNamespaceAware(true); // never forget this!
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new File(workflowName));	
			Element el = doc.getDocumentElement();
			WFProperties wfp = T2Util.parseWorkflow(el);
			System.out.println(wfp.needSecurity);
			System.out.println(wfp.needTransfer);
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
			

}
