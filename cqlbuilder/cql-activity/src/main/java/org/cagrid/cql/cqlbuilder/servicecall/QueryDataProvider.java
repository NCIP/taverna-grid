package org.cagrid.cql.cqlbuilder.servicecall;

import java.io.StringWriter;
import java.util.Vector;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.cagrid.cql.cqlbuilder.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This provider provides the application with query results of a service
 * It can query any service
 * @author Monika
 */
public class QueryDataProvider {

    public static String executeCQLQuery(Node aCQLQueryNode, String serviceURL) throws Exception {
        Service service = new Service();
        Call call = (Call) service.createCall();        
        call.setTargetEndpointAddress(new java.net.URL(serviceURL));
        System.out.println("Querying: "+serviceURL);
        Element query = Utils.createQueryCallBody(aCQLQueryNode);
        //  Element query = (Element) aCQLQueryNode;
        StringWriter sw = new StringWriter();
        XMLUtils.PrettyElementToWriter(query, sw);
        sw.flush();
        sw.close();
        System.out.println("The sent query: \n" + sw.toString());
        SOAPBodyElement[] request = new SOAPBodyElement[]{new SOAPBodyElement(query)};
        Vector resultVector = (Vector) call.invoke(request);
        SOAPBodyElement resultElements = (SOAPBodyElement) resultVector.get(0);
        Element resultElement = resultElements.getAsDOM();
        sw = new StringWriter();
        XMLUtils.PrettyElementToWriter(resultElement, sw);
        sw.flush();
        sw.close();
        System.out.println("The received XML: \n" + sw.toString());
        return sw.toString();
    }
}
