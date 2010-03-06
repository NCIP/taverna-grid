package net.sf.taverna.t2.workbench.cagrid;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jdom.output.DOMOutputter;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public class T2Util {
	
	
	/**
	 * @param dataflow
	 * @return true if it contains a secured cagrid-activity 
	 * i.e., starts with https://
	 */
	public static boolean needSecurity(Dataflow dataflow){
		return true;
	}
	
	/**
	 * @param dataflow
	 * @return  TRANSFER_NONE = 0; if there is no cagrid-transfer plug-in
	 *  TRANSFER_UPLOAD_ONLY = 1; if there is only upload transfer
	 *  TRANSFER_DOWNLOAD_ONLY = 2; if there is download only transfer
	 *  TRANSFER_BOTH = 3; if it has both upload and download
	 */
	public static int needTransfer(Dataflow dataflow){
		return WFProperties.TRANSFER_NONE;
	}
	/*
	public static WFProperties parseWorkflow(Dataflow dataflow){
		WFProperties wfp = new WFProperties();
		return wfp;
		
	}
	*/
	public static WFProperties parseWorkflow(Element workflowDef){
		//outputter.output(workflowDef, System.out);
		//String workflowDefString = outputter.outputString(dataflowE);
	    XPath xpath = XPathFactory.newInstance().newXPath();
	   
		// /processor/activities/activity/raven/artifact= cagridtransfer-activity
		///processor/activities/activity/configBean/org.cagrid.transfer.CaGridTransferConfigurationBean/isUpload = true 
		///processor/activities/activity/configBean/net.sf.taverna.cagrid.activity.CaGridActivityConfigurationBean/wsdl = https://...
		String searchTransfer = buildXpath("http://taverna.sf.net/2008/xml/t2flow", "processor",
				"http://taverna.sf.net/2008/xml/t2flow", "activities",
				"http://taverna.sf.net/2008/xml/t2flow", "activity",
				"http://taverna.sf.net/2008/xml/t2flow", "configBean",
				"", "org.cagrid.transfer.CaGridTransferConfigurationBean",
				"","isUpload")+"/text()";
		String searchSecurity =  buildXpath("http://taverna.sf.net/2008/xml/t2flow", "processor",
				"http://taverna.sf.net/2008/xml/t2flow", "activities",
				"http://taverna.sf.net/2008/xml/t2flow", "activity",
				"http://taverna.sf.net/2008/xml/t2flow", "configBean",
				"", "net.sf.taverna.cagrid.activity.CaGridActivityConfigurationBean",
				"","wsdl")+"/text()";
		//System.out.println(searchTransfer);
		//System.out.println(searchSecurity);
		WFProperties wfp = new WFProperties();	
	

		try {
			
			NodeList isUploadNodeList = (NodeList) xpath.evaluate(searchTransfer, workflowDef, XPathConstants.NODESET);
			NodeList wsdlNodeList = (NodeList) xpath.evaluate(searchSecurity, workflowDef, XPathConstants.NODESET);
			//Object rootNode;
			//Node serviceNode = (Node) xpath.evaluate(findServiceStr, rootNode, XPathConstants.NODE);
			for (int k = 0; k < wsdlNodeList.getLength(); k++) {
				Node wsdlChild = wsdlNodeList.item(k);
				if (wsdlChild.getTextContent().contains("https://")) {
					wfp.needSecurity = true;
					break;
				}
				}
			boolean hasUpload = false;
			boolean hasDownload = false;
			for (int k = 0; k < isUploadNodeList.getLength(); k++) {
				Node uploadChild = isUploadNodeList.item(k);
				if (uploadChild.getTextContent().equals("true")) {
					hasUpload = true;
				}
				if (uploadChild.getTextContent().equals("false")) {
					hasDownload = true;
				}
			}
			if(hasUpload&&hasDownload){
				wfp.needTransfer = WFProperties.TRANSFER_BOTH;
			}
			else if(!hasUpload&&!hasDownload){
				wfp.needTransfer = WFProperties.TRANSFER_NONE;			
			}
			else if(hasUpload&&!hasDownload){
				wfp.needTransfer = WFProperties.TRANSFER_UPLOAD_ONLY;			
			}
			else {
				wfp.needTransfer = WFProperties.TRANSFER_DOWNLOAD_ONLY;			
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return wfp;
}
	
	 /**
     * Creates an x-path query in the following form:<br>
     * //*[namespace-uri()='a1' and local-name()='a2]/*[namespace-uri()='a3' and local-name()='a3]...
     * @param inputPairs these are a1, a2, a3, a4, ... elements in the above example
     * @return
     */
    public static String buildXpath(String... inputPairs) {
        boolean namespace = true;
        StringBuilder sb = new StringBuilder("/");
        for (String str : inputPairs) {
            if (namespace) {
                if (str == null || "".equals(str)) {
                    sb.append("/*[");
                } else {
                    sb.append("/*[namespace-uri()='").append(str).append("' and ");
                }
            } else {
                sb.append("local-name()='").append(str).append("']");
            }
            namespace = !namespace;
        }
        return sb.toString();
    }
}
