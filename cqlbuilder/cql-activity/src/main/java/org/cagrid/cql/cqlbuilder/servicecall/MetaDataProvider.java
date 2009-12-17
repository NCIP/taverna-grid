package org.cagrid.cql.cqlbuilder.servicecall;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import java.util.Vector;
import org.cagrid.cql.cqlbuilder.metadata.Association;
import org.cagrid.cql.cqlbuilder.metadata.InheritanceRelation;
import org.cagrid.cql.cqlbuilder.metadata.UMLClass;
import org.cagrid.cql.cqlbuilder.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Instances of this class providing the application with metadata of a service
 * One MetadataProvider corresponds to one service
 * @author Monika
 */
public abstract class MetaDataProvider {

    private static LinkedList<String> availableServices;
    //the key value for items containing error message instead of data
    public static final String ERROR = "_ERROR_";
    
    public abstract String getServiceURL();

    /**
     * Gets general information about particular caGrid web service
     * @param aServiceAddress URL of the service
     * @return name, description, version
     */
    public abstract LinkedHashMap<String, String> getServiceDescription();

    /**
     * Gets general information about particular caGrid web service's hosting research center
     * @param aServiceAddress URL of the service
     * @return research senter's name and address details
     */
    public abstract LinkedHashMap<String, String> getResearchCenterInfo();

      /**
     * Gets general information about particular caGrid web service's domain model
     * @param aServiceAddress URL of the service
     * @return domain model details
     */
    public abstract LinkedHashMap<String, String> getDomainModelInfo();

      /**
     * Gets general information about particular caGrid web service's semantic metadata
     * @param aServiceAddress URL of the service
     * @return list of semantic metadata item's details
     */
    public abstract LinkedList<LinkedHashMap<String, String>> getSemanticMetadataItems();

     /**
     * Gets list of UML classes of a particular webService
     * @param aServiceAddress URL of the service
     * @return list of UMLClasses
     */
    public abstract LinkedList<UMLClass> getUMLClasses();

     /**
     * Gets list of UML classes associations of a particular webService
     * @param aServiceAddress URL of the service
     * @return list of associations
     */
    public abstract LinkedList<Association> getAssociations();

     /**
     * Gets list of inheritance relations between pairs ofUML classes 
     * @param aServiceAddress URL of the service
     * @return list of inheritance relation pairs
     */
    public abstract LinkedList<InheritanceRelation> getInheritanceRelations();

    /**
     * Gets list of available caGrid services' URLs     
     * @return list of URL strings
     */    
    public static List<String> getAvailableServices() {
        if (availableServices == null) {
            availableServices = new LinkedList<String>();
            //final String indexServiceUrl = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
            final String indexServiceUrl = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";

            try {
                Service service = new Service();
                Call call = (Call) service.createCall();
                call.setTargetEndpointAddress(new java.net.URL(indexServiceUrl));

                String findServicesStr = Utils.buildXpath(
                        "http://mds.globus.org/bigindex/2008/11/24", "BigIndexResourceProperties",
                        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd", "Entry",
                        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd", "MemberServiceEPR",
                        "http://schemas.xmlsoap.org/ws/2004/03/addressing", "Address");

                Element callContent = Utils.createQueryResourcePropertiesCallBody(findServicesStr);
                SOAPBodyElement[] request = new SOAPBodyElement[]{new SOAPBodyElement(callContent)};

                Vector resultVector = (Vector) call.invoke(request);
                System.out.println("SERVICE RESULT OBTAINED: getAvailableServices()");

                SOAPBodyElement resultElements = (SOAPBodyElement) resultVector.get(0);
                Element resultElement = resultElements.getAsDOM();
                NodeList availableServicesNodes = resultElement.getChildNodes();

                for (int i = 0; i < availableServicesNodes.getLength(); i++) {
                    Node address = availableServicesNodes.item(i);
                    String addressStr = address.getChildNodes().item(0).getNodeValue();
                    availableServices.add(addressStr);
                }

            } catch (Exception e) {
                availableServices = null;
                e.printStackTrace();
            }
        }
        return availableServices;
    }

    
}

