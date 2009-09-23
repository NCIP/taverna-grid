package org.cagrid.cql.cqlbuilder.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Set of useful methods
 * @author Monika
 */
public class Utils {

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

    /**
     * Creates the frame for SOAP message body for calling 'QueryResourceProperties' method of a service
     *  (getting metadata)
     * @param aXqueryExpression
     * @return
     * @throws ParserConfigurationException
     */
    public static Element createQueryResourcePropertiesCallBody(String aXqueryExpression) throws ParserConfigurationException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        String methodName = "QueryResourceProperties";
        String parameterName = "QueryExpression";
        String nameSpace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd";
        Element methodElement = doc.createElementNS(nameSpace, methodName);
        Element parameterElement = doc.createElementNS(nameSpace, parameterName);
        parameterElement.setAttribute("Dialect", "http://www.w3.org/TR/1999/REC-xpath-19991116");
        methodElement.appendChild(parameterElement);
        Node parameter = doc.createTextNode(aXqueryExpression);
        parameterElement.appendChild(parameter);

        return methodElement;
    }

    /**
     * Creates the frame for SOAP message body for calling 'query' method of a service
     *  (quryinf the service for data)
     * @param aCQLQueryNode
     * @return
     */
    public static Element createQueryCallBody(Node aCQLQueryNode) {
        Document doc = aCQLQueryNode.getOwnerDocument();
        String nsMethod = "http://gov.nih.nci.cagrid.data/DataService";
        Element methodElement = doc.createElementNS(nsMethod, "QueryRequest");

        Element parameterElement = doc.createElement("cqlQuery");
        methodElement.appendChild(parameterElement);
        parameterElement.appendChild(aCQLQueryNode);

        return methodElement;
    }

    /**
     * Pretty prints a DOM node
     * (this my own method which recognises only nodes, attrbibutes and text nodes)
     * @param node
     * @return
     */
    public static String prettyprint(Node node) {
        return prettyPrintNode(node, 0);
    }

    private static String prettyPrintNode(Node aNode, int indent) {
        StringBuilder str = new StringBuilder();
        if (aNode.getNodeType() == Node.ELEMENT_NODE) {
            String uri = aNode.getNamespaceURI() == null ? "" : (" xmlns=\"" + aNode.getNamespaceURI() + "\"");
            StringBuilder attrs = new StringBuilder();
            NamedNodeMap attrss = aNode.getAttributes();
            for (int k = 0; k < attrss.getLength(); k++) {
                Node atr = attrss.item(k);
                attrs.append(" " + atr.getNodeName() + "=\"" + atr.getNodeValue() + "\"");
            }
            for (int j = 0; j < indent; j++) {
                str.append(" ");
            }
            NodeList children = aNode.getChildNodes();
            if (children.getLength() > 0) {
                str.append("<" + aNode.getNodeName() + attrs.toString() + uri + ">\n");

                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    str.append(prettyPrintNode(node, indent + 2));
                }
                for (int j = 0; j < indent; j++) {
                    str.append(" ");
                }
                str.append("</" + aNode.getNodeName() + ">\n");
            } else {
                str.append("<" + aNode.getNodeName() + attrs.toString() + uri + "/>\n");
            }
        } else {//TEXT_NODE
            for (int j = 0; j < indent; j++) {
                str.append(" ");
            }
            str.append(aNode.getNodeValue()).append("\n");
        }
        return str.toString();
    }

    public static Node removeQueryCallBody(Node queryElement) {
        if ("QueryRequest".equals(queryElement.getNodeName())) {
            return queryElement.getFirstChild().getFirstChild();
        }
        return null;
    }

    /**
     * deletes all whitespace characters between > and <
     * @param text
     * @return
     */
    public static String stripFromWhitespaces(String text) {
        return text.replaceAll(">\\s+<", "><");//delete all whitespace characters between > and <
    }

    public static Node textToDOMNode(String savedQuery) throws SAXException {
        Element queryElement = null;
        try {
            Document document;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //builder.setErrorHandler(new DefaultErrorHandler(false));
            builder.setErrorHandler(null);
            savedQuery = Utils.stripFromWhitespaces(savedQuery);
            document = builder.parse(new InputSource(new StringReader(savedQuery)));
            queryElement = document.getDocumentElement();        
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return queryElement;
    }
     public static ArrayList<String> getVariableList(String cql){
        ArrayList<String> varList = new ArrayList<String>();
        //String cql = "<QueryRequest xmlns=\"http://gov.nih.nci.cagrid.data/DataService\"><cqlQuery><CQLQuery xmlns=\"http://CQL.caBIG/1/gov.nih.nci.cagrid.CQLQuery\"><Target name=\"gov.nih.nci.caarray.domain.project.Experiment\"><Group logicRelation=\"OR\"><attribute name=\"id\" predicate=\"EQUAL_TO\" value=\"$var1$\"/><attribute name=\"id\" predicate=\"EQUAL_TO\" value=\"$var2$\"/><attribute name=\"id\" predicate=\"EQUAL_TO\" value=\"$var0$\"/></Group></Target></CQLQuery></cqlQuery></QueryRequest>";
         System.out.println(cql);
         //String patternStr = "value=\\\"\\$*\\$\\\"";
         String patternStr = "value=\\\"\\$\\S+\\$\\\"";
         System.out.println(patternStr);
         Pattern pattern = Pattern.compile(patternStr);
         Matcher matcher = pattern.matcher(cql);
         while(matcher.find()){
              // Get all groups for this match
             String var = matcher.group();
             System.out.println(var);
             var = var.substring(var.indexOf("$")+1,var.length()-2);
             System.out.println(var);
             varList.add(var);
         }
        return varList;
    }
}
