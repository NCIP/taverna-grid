package org.cagrid.cql.cqlbuilder.servicecall;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.axis.AxisFault;
import org.cagrid.cql.cqlbuilder.metadata.Association;
import org.cagrid.cql.cqlbuilder.metadata.AssociationEdge;
import org.cagrid.cql.cqlbuilder.metadata.InheritanceRelation;
import org.cagrid.cql.cqlbuilder.metadata.UMLClass;
import org.cagrid.cql.cqlbuilder.metadata.UMLClassAttribute;
import org.cagrid.cql.cqlbuilder.utils.Utils;
import org.w3c.dom.NamedNodeMap;

/**
 * Connects with the service only once to all the metadata xml,
 * but parses this xml only when needed (and caches the data)
 * @author Monika
 */
public class OneCallMetaDataProvider extends MetaDataProvider {

    private String serviceURL;
    private Node rootNode;//root node ot the metadata xml
    private LinkedHashMap<String, String> serviceDescription;
    private LinkedHashMap<String, String> researchCenter;
    private LinkedHashMap<String, String> domainModelInfo;
    private LinkedList<LinkedHashMap<String, String>> semanticMetadataItems;
    
    private LinkedList<UMLClass> umlClasses;
    private LinkedList<Association> associations;
    private LinkedList<InheritanceRelation> inheritanceRelations;    
    private XPath xpath = XPathFactory.newInstance().newXPath();
    private Exception error = null;

    /***************************************************************************/
    public OneCallMetaDataProvider(String aServiceURL) {
        this.serviceURL = aServiceURL;
    }

    /**
     * Gets general information about particular caGrid web service
     * @param aServiceAddress URL of the service
     * @return name, description, version
     */
    public LinkedHashMap<String, String> getServiceDescription() {
        if (rootNode == null) {
            if (error != null) {
                LinkedHashMap<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put(MetaDataProvider.ERROR, error.getClass() + error.getLocalizedMessage());
                return errorMap;
            } else {
                loadRootNode();
            }
        }
        if (serviceDescription == null) {
            serviceDescription = new LinkedHashMap<String, String>();
            try {

                String findServiceStr = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "ServiceMetadata",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "serviceDescription",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service", "Service");

//                System.err.println(XMLUtils.PrettyDocumentToString(rootNode.getOwnerDocument()));
                System.out.println("Root node: " + rootNode + "\nXpath Expression: " + findServiceStr);

                Node serviceNode = (Node) xpath.evaluate(findServiceStr, rootNode, XPathConstants.NODE);
                NamedNodeMap serviceNodesAttr = serviceNode.getAttributes();

                serviceDescription.put("Service name", serviceNodesAttr.getNamedItem("name").getNodeValue());
                serviceDescription.put("Service description", serviceNodesAttr.getNamedItem("description").getNodeValue());
                serviceDescription.put("Service version", serviceNodesAttr.getNamedItem("version").getNodeValue());

            } catch (Exception e) {
                serviceDescription = null;
                e.printStackTrace();
            }
        }
        return serviceDescription;
    }

    /**
     * Gets general information about particular caGrid web service's hosting research center
     * @param aServiceAddress URL of the service
     * @return research senter's name and address details
     */
    public LinkedHashMap<String, String> getResearchCenterInfo() {
        if (rootNode == null) {
            if (error != null) {
                LinkedHashMap<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put(MetaDataProvider.ERROR, error.getClass() + error.getLocalizedMessage());
                return errorMap;
            } else {
                loadRootNode();
            }
        }
        if (researchCenter == null) {
            researchCenter = new LinkedHashMap<String, String>();
            try {
                String findResearchCenter = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "ServiceMetadata",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "hostingResearchCenter",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.common", "ResearchCenter");

                Node researchCenterNode = (Node) xpath.evaluate(findResearchCenter, rootNode, XPathConstants.NODE);
                NamedNodeMap researchCenterNodeAttr = researchCenterNode.getAttributes();

                researchCenter.put("Name", researchCenterNodeAttr.getNamedItem("displayName").getNodeValue());
                researchCenter.put("Short name", researchCenterNodeAttr.getNamedItem("shortName").getNodeValue());

                NodeList researchCenterNodeChildren = researchCenterNode.getChildNodes();
                for (int k = 0; k < researchCenterNodeChildren.getLength(); k++) {
                    Node child = researchCenterNodeChildren.item(k);
                    if (child.getNodeName().endsWith("Address")) {
                        NamedNodeMap childAttr = child.getAttributes();
                        researchCenter.put("Country", childAttr.getNamedItem("country").getNodeValue());
                        researchCenter.put("State/province", childAttr.getNamedItem("stateProvince").getNodeValue());
                        researchCenter.put("Locality", childAttr.getNamedItem("locality").getNodeValue());
                        researchCenter.put("Postal code", childAttr.getNamedItem("postalCode").getNodeValue());
                        researchCenter.put("Street", childAttr.getNamedItem("street1").getNodeValue());
                        Node street2 = childAttr.getNamedItem("street2");
                        if (street2 != null && street2.getNodeValue() != null && !"".equals(street2.getNodeValue())) {
                            researchCenter.put("Street 2", street2.getNodeValue());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                researchCenter = null;
                e.printStackTrace();
            }
        }
        return researchCenter;
    }

    public LinkedHashMap<String, String> getDomainModelInfo() {
        if (rootNode == null) {
            if (error != null) {
                LinkedHashMap<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put(MetaDataProvider.ERROR, error.getClass() + error.getLocalizedMessage());
                return errorMap;
            } else {
                loadRootNode();
            }
        }
        if (domainModelInfo == null) {
            domainModelInfo = new LinkedHashMap<String, String>();
            try {
                String findDomainModelStr = Utils.buildXpath(
                        // "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd","QueryResourcePropertiesResponse",
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "DomainModel");

                Node domainModelNode = (Node) xpath.evaluate(findDomainModelStr, rootNode, XPathConstants.NODE);
                if (domainModelNode == null) {
                    domainModelInfo.put(MetaDataProvider.ERROR, "No domain model data");
                } else {
                    
                    NamedNodeMap domainModelNodesAttr = domainModelNode.getAttributes();

                    Node descr = domainModelNodesAttr.getNamedItem("projectDescription");
                    if (descr!=null)
                        domainModelInfo.put("Project description", descr.getNodeValue());
                    else
                        domainModelInfo.put("Project description", "");
                    
                    Node longName = domainModelNodesAttr.getNamedItem("projectLongName");
                    if (longName!=null)
                        domainModelInfo.put("Project long name", longName.getNodeValue());
                    else
                        domainModelInfo.put("Project long name", "");

                    Node shortName = domainModelNodesAttr.getNamedItem("projectShortName");
                    if (shortName!=null)
                        domainModelInfo.put("Project short name", shortName.getNodeValue());
                    else
                        domainModelInfo.put("Project short name", "");
                                        
                    Node ver = domainModelNodesAttr.getNamedItem("projectVersion");
                    if (ver!=null)
                        domainModelInfo.put("Project version", ver.getNodeValue());
                    else
                        domainModelInfo.put("Project version", "");                   

                    //TODO put info about UML classes
                }
            } catch (Exception e) {
                domainModelInfo = null;
                e.printStackTrace();
            }
        }
        return domainModelInfo;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    private /*synchronized*/ void loadRootNode() {
        System.out.println("loadRootNode()");
        if (error != null) {
            return;
        }
        try {
            Service service = new Service();
            Call call = (Call) service.createCall();
            System.out.println(serviceURL);
            call.setTargetEndpointAddress(new java.net.URL(serviceURL));

            Element callContent = Utils.createQueryResourcePropertiesCallBody("/");
            SOAPBodyElement[] request = new SOAPBodyElement[]{new SOAPBodyElement(callContent)};

            Vector resultVector = (Vector) call.invoke(request);
            System.out.println("SERVICE RESULT OBTAINED: loadData() " + serviceURL);

            SOAPBodyElement resultElements = (SOAPBodyElement) resultVector.get(0);
            // System.out.println(XMLUtils.PrettyDocumentToString(resultElements.getAsDocument()));
            Element resultElement = resultElements.getAsDOM();
            rootNode = resultElement.getChildNodes().item(0);

            //        System.out.println(XMLUtils.PrettyDocumentToString(rootNode.getOwnerDocument()));
        } catch (AxisFault af) {
            error = af;
            af.printStackTrace();
            throw new RuntimeException("Failed to connect with the service. Please make sure the url is correct.");
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw new RuntimeException("The url is incorrect!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<LinkedHashMap<String, String>> getSemanticMetadataItems() {
        if (rootNode == null) {
            loadRootNode();
        }
        if (error != null) {
            LinkedList<LinkedHashMap<String, String>> errorList = new LinkedList<LinkedHashMap<String, String>>();
            LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
            lhm.put(MetaDataProvider.ERROR, error.getClass() + error.getLocalizedMessage());
            errorList.add(lhm);
            return errorList;
        }
        if (semanticMetadataItems == null) {

            try {
                semanticMetadataItems = new LinkedList<LinkedHashMap<String, String>>();
                String findSemanticMetadataItems = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "ServiceMetadata",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "serviceDescription",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service", "Service",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.common", "SemanticMetadata"//TODO check if the namespace does not change depending on the node
                        );

                Object nodes = xpath.evaluate(findSemanticMetadataItems, rootNode, XPathConstants.NODESET);
                NodeList semanticMetadataItemsNodeList = (NodeList) nodes;

                for (int i = 0; i < semanticMetadataItemsNodeList.getLength(); i++) {
                    Node semanticMetadataItemNode = semanticMetadataItemsNodeList.item(i);
                    NamedNodeMap semanticMetadataItemNodeAttr = semanticMetadataItemNode.getAttributes();

                    LinkedHashMap<String, String> semanticMetadataItem = new LinkedHashMap<String, String>();
                    semanticMetadataItem.put("Concept name", semanticMetadataItemNodeAttr.getNamedItem("conceptName").getNodeValue());
                    semanticMetadataItem.put("Concept code", semanticMetadataItemNodeAttr.getNamedItem("conceptCode").getNodeValue());
                    semanticMetadataItem.put("Concept definition", semanticMetadataItemNodeAttr.getNamedItem("conceptDefinition").getNodeValue());
                    semanticMetadataItems.add(semanticMetadataItem);
                }
            } catch (Exception e) {
                serviceDescription = null;
                e.printStackTrace();
            }
        }
        return semanticMetadataItems;
    }

    @Override
    public LinkedList<Association> getAssociations() {
        System.out.println("  Entering getAssociations()");
        if (rootNode == null) {
            loadRootNode();
        }
        if (error != null) {
            return null;
        }
        if (associations == null) {
            associations = new LinkedList<Association>();
            try {
                String findUMLClassesAssociations = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "DomainModel",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "exposedUMLAssociationCollection",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "UMLAssociation");
                Object nodes = xpath.evaluate(findUMLClassesAssociations, rootNode, XPathConstants.NODESET);
                NodeList UMLClassesAssociationsNodeList = (NodeList) nodes;
                for (int i = 0;
                        i < UMLClassesAssociationsNodeList.getLength();
                        i++) {
                    Node UMLClassAssociation = UMLClassesAssociationsNodeList.item(i);

                    Association assoc = new Association();
                    String bidirectionalStr = UMLClassAssociation.getAttributes().getNamedItem("bidirectional").getNodeValue();
                    assoc.setBidirectional("true".equals(bidirectionalStr));

                    NodeList UMLClassAssociationChildren = UMLClassAssociation.getChildNodes();
                    for (int ii = 0; ii < UMLClassAssociationChildren.getLength(); ii++) {
                        if (UMLClassAssociationChildren.getLength() != 2)//should be 2 nodes
                        {
                            throw new RuntimeException("Something's wrong...");
                        }
                        Node child = UMLClassAssociationChildren.item(ii);                        

                        NamedNodeMap childAttr = child.getChildNodes().item(0).getAttributes();
                        AssociationEdge edge = new AssociationEdge();
                        edge.setMaxCardinality(childAttr.getNamedItem("maxCardinality").getNodeValue());
                        edge.setMinCardinality(childAttr.getNamedItem("minCardinality").getNodeValue());
                        //TODO investigate why roleName is sometimes not present, and what does it mean then?:/ this attribute is needed for the creating the query, isn't it?
                        try {
                            edge.setRoleName(childAttr.getNamedItem("roleName").getNodeValue());
                        } catch (NullPointerException e) {
                            edge.setRoleName(null);//probably will cause exception later..
                        }
                        childAttr = child.getChildNodes().item(0).getChildNodes().item(0).getAttributes();
                        edge.setRefId(childAttr.getNamedItem("refid").getNodeValue());

                        if (child.getNodeName().endsWith("sourceUMLAssociationEdge")) {
                            assoc.setSource(edge);
                            continue;
                        }
                        if (child.getNodeName().endsWith("targetUMLAssociationEdge")) {
                            assoc.setTarget(edge);
                            continue;
                        }
                    }
                    associations.add(assoc);
                }
            } catch (Exception e) {
                associations = null;
                e.printStackTrace();
            }
        }
        return associations;
    }

    @Override
    public LinkedList<InheritanceRelation> getInheritanceRelations() {
        System.out.println("  Entering getInheritanceRelations()");
        if (rootNode == null) {
            loadRootNode();
        }
        if (error != null) {
            return null;
        }
        if (inheritanceRelations == null) {
            inheritanceRelations = new LinkedList<InheritanceRelation>();
            try {
                String findUMLClassesInheritances = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "DomainModel",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "umlGeneralizationCollection",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "UMLGeneralization");
                Object nodes = xpath.evaluate(findUMLClassesInheritances, rootNode, XPathConstants.NODESET);
                NodeList UMLClassesInheritancesNodeList = (NodeList) nodes;
                for (int i = 0;
                        i < UMLClassesInheritancesNodeList.getLength();
                        i++) {
                    NodeList UMLClassInheritanceClasses = UMLClassesInheritancesNodeList.item(i).getChildNodes();
                    Node subClassNode = null;
                    Node superClassNode = null;
                    for (int j = 0; j < UMLClassInheritanceClasses.getLength(); j++) {
                        Node n = UMLClassInheritanceClasses.item(j);
                        if (n.getNodeName().endsWith("subClassReference")) {
                            subClassNode = n;
                            continue;
                        }
                        if (n.getNodeName().endsWith("superClassReference")) {
                            superClassNode = n;
                            continue;
                        }
                    }

                    InheritanceRelation inher = new InheritanceRelation();
                    inher.setSubClassRefId(subClassNode.getAttributes().getNamedItem("refid").getNodeValue());
                    inher.setSuperClassRefId(superClassNode.getAttributes().getNamedItem("refid").getNodeValue());

                    inheritanceRelations.add(inher);

                }
            } catch (Exception e) {
                inheritanceRelations = null;
                e.printStackTrace();
            }
        }
        return inheritanceRelations;
    }

    @Override
    public LinkedList<UMLClass> getUMLClasses() {
        System.out.println("  Entering getUMLClasses()");
        if (rootNode == null) {
            loadRootNode();
        }
        if (error != null) {
            return null;
        }
        if (umlClasses == null) {
            umlClasses = new LinkedList<UMLClass>();
            try {
                String findUMLClassesNodes = Utils.buildXpath(
                        //"http://client.cpas.labkey.org/CpasSvc", "CpasSvcResourceProperties",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "DomainModel",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "exposedUMLClassCollection",
                        "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "UMLClass");

                Object nodes = xpath.evaluate(findUMLClassesNodes, rootNode, XPathConstants.NODESET);

                NodeList UMLClassesNodeList = (NodeList) nodes;

                for (int i = 0; i < UMLClassesNodeList.getLength(); i++) {
                    Node umlClassNode = UMLClassesNodeList.item(i);
                    NamedNodeMap umlClassAttrNodeMap = umlClassNode.getAttributes();                    

                    UMLClass umlClass = new UMLClass();
                    /*umlClass.setDescription(umlClassAttrNodeMap.getNamedItem("description").getNodeValue());
                    umlClass.setName(umlClassAttrNodeMap.getNamedItem("className").getNodeValue());
                    umlClass.setPackageName(umlClassAttrNodeMap.getNamedItem("packageName").getNodeValue());
                    umlClass.setProjectName(umlClassAttrNodeMap.getNamedItem("projectName").getNodeValue());
                    umlClass.setProjectVersion(umlClassAttrNodeMap.getNamedItem("projectVersion").getNodeValue());
                    umlClass.setId(umlClassAttrNodeMap.getNamedItem("id").getNodeValue());*/
                    for (int s = 0; s < umlClassAttrNodeMap.getLength(); s++) {
                        Node attrNode = umlClassAttrNodeMap.item(s);
                        umlClass.addField(attrNode.getNodeName(), attrNode.getNodeValue());
                    }

                    Node nodesAttr = null;
                    NodeList umlClassChildNodes = umlClassNode.getChildNodes();
                    for (int ii = 0; ii < umlClassChildNodes.getLength(); ii++) {
                        Node child = umlClassChildNodes.item(ii);
                        if (child.getNodeName().endsWith("umlAttributeCollection")) {
                            nodesAttr = child;
                            break;
                        }
                    }

                    if (nodesAttr != null) {

                        NodeList attrNodeList = nodesAttr.getChildNodes();
                        for (int j = 0; j < attrNodeList.getLength(); j++) {
                            Node attributeNode = attrNodeList.item(j);
                            NamedNodeMap attributeNodeAttr = attributeNode.getAttributes();

                            UMLClassAttribute umlClassAttribute = new UMLClassAttribute();
                            /*umlClassAttribute.setDataTypeName(attributeNodeAttr.getNamedItem("dataTypeName").getNodeValue());
                            umlClassAttribute.setDescription(attributeNodeAttr.getNamedItem("description").getNodeValue());
                            umlClassAttribute.setName(attributeNodeAttr.getNamedItem("name").getNodeValue());
                            umlClassAttribute.setPublicID(attributeNodeAttr.getNamedItem("publicID").getNodeValue());
                            umlClassAttribute.setVersion(attributeNodeAttr.getNamedItem("version").getNodeValue());*/

                            for (int s = 0; s < attributeNodeAttr.getLength(); s++) {
                                Node attrNode = attributeNodeAttr.item(s);
                                umlClassAttribute.addField(attrNode.getNodeName(), attrNode.getNodeValue());
                            }

                            //TODO handle nested levels metadata of an attribute of an uml class
                           /* List<Node> nodesAttrMetadata = new LinkedList<Node>();
                            NodeList nodesAttrMetadataChildNodes = attributeNode.getChildNodes();
                            for (int ii = 0; ii < nodesAttrMetadataChildNodes.getLength(); ii++) {                                
                            Node child = nodesAttrMetadataChildNodes.item(ii);
                            
                            if (child.getNodeName().endsWith("SemanticMetadata")) {
                            nodesAttrMetadata.add(child);
                            }
                            }

                            for (int k = 0; k < nodesAttrMetadata.size(); k++) {
                            Node attributesMetadataNode = nodesAttrMetadata.get(k);
                            NamedNodeMap attributesMetadataNodeAttr = attributesMetadataNode.getAttributes();

                            LinkedHashMap<String, String> semanticItem = new LinkedHashMap<String, String>();

                            semanticItem.put("Concept name", attributesMetadataNodeAttr.getNamedItem("conceptName").getNodeValue());
                            semanticItem.put("Concept code", attributesMetadataNodeAttr.getNamedItem("conceptCode").getNodeValue());
                            semanticItem.put("Concept definition", attributesMetadataNodeAttr.getNamedItem("conceptDefinition").getNodeValue());

                            umlClassAttribute.addSemanticMetadata(semanticItem);
                            }

                            Node attributesValueDomainNode = null;
                            for (int ii = 0; ii < nodesAttrMetadataChildNodes.getLength(); ii++) {
                            Node child = nodesAttrMetadataChildNodes.item(ii);
                            if (child.getNodeName().endsWith("ValueDomain")) {
                            attributesValueDomainNode = child;
                            break;
                            }
                            }

                            NamedNodeMap attributesValueDomainNodeAttr = attributesValueDomainNode.getAttributes();
                            umlClassAttribute.setValueDomainLongName(attributesValueDomainNodeAttr.getNamedItem("longName").getNodeValue());

                            Node unitOfMeasure = attributesValueDomainNodeAttr.getNamedItem("unitOfMeasure");
                            if (unitOfMeasure != null) {
                            umlClassAttribute.setValueDomainUnitOfMeasure(unitOfMeasure.getNodeValue());
                            }*/

                            umlClass.addAttribute(umlClassAttribute);
                        }
                    }
                    //SEMANTIC METADATA
                    List<Node> nodesMetadata = new LinkedList<Node>();
                    for (int ii = 0; ii < umlClassChildNodes.getLength(); ii++) {
                        Node child = umlClassChildNodes.item(ii);
                        if (child.getNodeName().endsWith("SemanticMetadata")) {
                            nodesMetadata.add(child);
                        }
                    }

                    for (int t = 0; t < nodesMetadata.size(); t++) {
                        Node meadatadaNode = nodesMetadata.get(t);
                        NamedNodeMap meadatadaNodeAttr = meadatadaNode.getAttributes();

                        LinkedHashMap<String, String> umlClassSemanticMetadata = new LinkedHashMap<String, String>();
                        umlClassSemanticMetadata.put("Concept name", meadatadaNodeAttr.getNamedItem("conceptName").getNodeValue());
                        umlClassSemanticMetadata.put("Concept code", meadatadaNodeAttr.getNamedItem("conceptCode").getNodeValue());
                        umlClassSemanticMetadata.put("Concept definition", meadatadaNodeAttr.getNamedItem("conceptDefinition").getNodeValue());

                        umlClass.addSemanticMetadata(umlClassSemanticMetadata);

                    }

                    umlClasses.add(umlClass);
                }



                //ASSOCIATIONS (and INHERITANCE RELATIONS)

                List<Association> assocs = getAssociations();
                List<InheritanceRelation> relations = getInheritanceRelations();

                Iterator<UMLClass> it = umlClasses.iterator();
                while (it.hasNext()) {
                    UMLClass umlClass = it.next();
                    for (Association assoc : assocs) {
                        if (umlClass.getId().equals(assoc.getSource().getRefId())) {
                            assoc.getSource().setUmlClass(umlClass);
                        }
                        if (umlClass.getId().equals(assoc.getTarget().getRefId())) {
                            assoc.getTarget().setUmlClass(umlClass);
                        }
                    }
                    for (InheritanceRelation rel : relations) {
                        if (umlClass.getId().equals(rel.getSubClassRefId())) {
                            rel.setSubClass(umlClass);
                        }
                        if (umlClass.getId().equals(rel.getSuperClassRefId())) {
                            rel.setSuperClass(umlClass);
                        }
                    }
                }

                for (InheritanceRelation rel : relations) {
                    rel.getSubClass().setSuperClass(rel.getSuperClass());
                }

                for (Association assoc : assocs) {
                    //add associations to corresponding classes
                    assoc.getSource().getUmlClass().addAssociation(assoc.getTarget());
                    if (assoc.isBidirectional()) {
                        assoc.getTarget().getUmlClass().addAssociation(assoc.getSource());
                    }
                    //if a null pointer exception occurs, it would mean that the metadata
                    //service's response lacks some info.. or that I misunderstood how
                    //it works..
                }

                for (UMLClass uml : getUMLClasses()) {
                    //add all the associations of the super class
                    if (uml.getSuperClass() != null) {
                        uml.addAllAssociations(uml.getSuperClass().getAssociations());
                    }
                }

            } catch (Exception e) {
                umlClasses = null;
                e.printStackTrace();
            }

        }

        return umlClasses;
    }
}

