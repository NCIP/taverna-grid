package org.cagrid.cql.cqlbuilder.cqlquery;

import java.util.LinkedList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CQLQuery {
    
    private Document _doc;
    private Element _mainNode;
    //the namespace used in all CQL query's nodes
    private static final String nsQuery = "http://CQL.caBIG/1/gov.nih.nci.cagrid.CQLQuery";
    //convenience fields
    private static final int type_WHOLE_OBJECT = 0;
    private static final int type_COUNT_ONLY = 1;
    private static final int type_DISTINCT_ATTRIBUTE = 2;
    private static final int type_SELECTED_ATTRIBUTES = 3;

    public Node get_mainNode() {
        return _mainNode;
    }

    public Document get_document() {
        return _doc;
    }

    /**
     * Creates CQL query based on: targetUML, logicRelationPredicate (AND or OR),
     * list of criteria, type of queryModifiers: count only, and an array of
     * selected attrs - this array may be empty(count only or whole object), have one(distinct
     * attribute) or more elements(selected attrubutes)
     * @param targetUmlClassName
     * @param logicRelationPredicate
     * @param criteria
     * @param isCountOnly
     * @param selectedAttributes
     * @return a CQLquery
     */
    public static CQLQuery newCqlQuery(String targetUmlClassName, String logicRelationPredicate, LinkedList<CQLQueryCriterion> criteria, boolean isCountOnly, String[] selectedAttributes) {
        CQLQuery query = new CQLQuery();
        Document doc = query.get_document();
        Node root = query.get_mainNode();

        Element target = CQLQuery.createTargetNode(doc, targetUmlClassName);
        root.appendChild(target);

        Element queryModifiers = CQLQuery.createQueryModifierNodeTree(doc,
                isCountOnly ? CQLQuery.type_COUNT_ONLY : (selectedAttributes.length == 0 ? CQLQuery.type_WHOLE_OBJECT : (selectedAttributes.length == 1 ? CQLQuery.type_DISTINCT_ATTRIBUTE : CQLQuery.type_SELECTED_ATTRIBUTES)),
                selectedAttributes);

        if (queryModifiers != null) {
            root.appendChild(queryModifiers);
        }

        Element logicGroup = CQLQuery.createGroupNode(doc, logicRelationPredicate);
        target.appendChild(logicGroup);

        for (CQLQueryCriterion crit : criteria) {

            if (!crit.areAssociationsSet()) {
                Element criterion = CQLQuery.createAttributeNode(doc, crit.get_attributeName(), crit.get_value(), crit.get_predicate());
                logicGroup.appendChild(criterion);
            } else {
                int numberOfNestedAssociations = crit.getAssociationsList().size();

                CQLQueryAssociation assoc = crit.getAssociationsList().get(0);
                Element topAssoc = CQLQuery.createAssociationNode(doc, assoc.getRoleName(), assoc.getUmlClassFullName());
                Element lastAssoc = topAssoc;
                for (int i = 1; i < numberOfNestedAssociations; i++) {
                    assoc = crit.getAssociationsList().get(i);
                    Element subCrit = CQLQuery.createAssociationNode(doc, assoc.getRoleName(), assoc.getUmlClassFullName());
                    lastAssoc.appendChild(subCrit);
                    lastAssoc = subCrit;
                }
                Element criterion = CQLQuery.createAssociatedAttributeNode(doc, crit.get_associatedAtributeName(), crit.get_value(), crit.get_predicate());
                lastAssoc.appendChild(criterion);

                logicGroup.appendChild(topAssoc);
            }

        }
        return query;
    }

    public static CQLQuery newCqlQuery(String targetUmlClassName, DefaultMutableTreeNode mainNode, boolean isCountOnly, String[] selectedAttributes) {
        CQLQuery query = new CQLQuery();
        Document doc = query.get_document();
        Node root = query.get_mainNode();

        Element target = CQLQuery.createTargetNode(doc, targetUmlClassName);
        root.appendChild(target);

        Element queryModifiers = CQLQuery.createQueryModifierNodeTree(doc,
                isCountOnly ? CQLQuery.type_COUNT_ONLY : (selectedAttributes.length == 0 ? CQLQuery.type_WHOLE_OBJECT : (selectedAttributes.length == 1 ? CQLQuery.type_DISTINCT_ATTRIBUTE : CQLQuery.type_SELECTED_ATTRIBUTES)),
                selectedAttributes);

        if (queryModifiers != null) {
            root.appendChild(queryModifiers);
        }
        Element mainLogicGroup = createGroupStructure(mainNode, doc);
        target.appendChild(mainLogicGroup);
        return query;
    }
    /** private methods ********************************************************/

       //recursive method
       private static Element createGroupStructure(DefaultMutableTreeNode mainNode, Document doc) {
        String logicRelationPredicate = mainNode.getUserObject().toString();
        Element logicGroup = CQLQuery.createGroupNode(doc, logicRelationPredicate);
        for (int j = 0; j < mainNode.getChildCount(); j++) {
            Object curr = ((DefaultMutableTreeNode)mainNode.getChildAt(j)).getUserObject();
            if (curr instanceof CQLQueryCriterion) {
                CQLQueryCriterion crit = (CQLQueryCriterion) curr;

                if (!crit.areAssociationsSet()) {
                    Element criterion = CQLQuery.createAttributeNode(doc, crit.get_attributeName(), crit.get_value(), crit.get_predicate());
                    logicGroup.appendChild(criterion);
                } else {
                    int numberOfNestedAssociations = crit.getAssociationsList().size();

                    CQLQueryAssociation assoc = crit.getAssociationsList().get(0);
                    Element topAssoc = CQLQuery.createAssociationNode(doc, assoc.getRoleName(), assoc.getUmlClassFullName());
                    Element lastAssoc = topAssoc;
                    for (int i = 1; i < numberOfNestedAssociations; i++) {
                        assoc = crit.getAssociationsList().get(i);
                        Element subCrit = CQLQuery.createAssociationNode(doc, assoc.getRoleName(), assoc.getUmlClassFullName());
                        lastAssoc.appendChild(subCrit);
                        lastAssoc = subCrit;
                    }
                    Element criterion = CQLQuery.createAssociatedAttributeNode(doc, crit.get_associatedAtributeName(), crit.get_value(), crit.get_predicate());
                    lastAssoc.appendChild(criterion);

                    logicGroup.appendChild(topAssoc);
                }
            } else {//the element is another list of groups
                DefaultMutableTreeNode newNode = (DefaultMutableTreeNode)mainNode.getChildAt(j);
                logicGroup.appendChild(createGroupStructure(newNode, doc));
            }
        }
        return logicGroup;
    }

    //constructor may have been public, but somehow it happen it got so weird... ;p
    private CQLQuery() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            _doc = builder.newDocument();
            _mainNode = _doc.createElementNS(nsQuery, "CQLQuery");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Element createQueryModifierNodeTree(Document doc, int aType, String... aArgs) {
        //Element node = doc.createElementNS(nsQuery, "QueryModifier");
        Element node = doc.createElement("QueryModifier");
        switch (aType) {
            case type_COUNT_ONLY:
                node.setAttribute("countOnly", "true");                
            break;
            case type_WHOLE_OBJECT:
                //node.setAttribute("countOnly", "false");
                return null;                
            case type_DISTINCT_ATTRIBUTE:
                node.setAttribute("countOnly", "false");
                Element da = doc.createElementNS(nsQuery, "DistinctAttribute");
                node.appendChild(da);
                Node dtext = doc.createTextNode(aArgs[0]);
                da.appendChild(dtext);
                break;
            case type_SELECTED_ATTRIBUTES:
                node.setAttribute("countOnly", "false");
                for (String str : aArgs) {
                    Element an = doc.createElementNS(nsQuery, "AttributeNames");
                    node.appendChild(an);
                    Node atext = doc.createTextNode(str);
                    an.appendChild(atext);
                }
                break;
        }
        return node;
    }

    private static Element createTargetNode(Document doc, String aUmlClassFullName) {
        //Element node = doc.createElementNS(nsQuery, "Target");
        Element node = doc.createElement("Target");
        node.setAttribute("name", aUmlClassFullName);
        return node;
    }

    private static Element createGroupNode(Document doc, String aRelation) {
        //Element node = doc.createElementNS(nsQuery, "Group");
        Element node = doc.createElement("Group");
        node.setAttribute("logicRelation", aRelation);
        return node;
    }

    private static Element createAssociationNode(Document doc, String aAssociatedClassRoleName, String aAssociatedUmlClassFullName) {
        //Element node = doc.createElementNS(nsQuery, "Association");
        Element node = doc.createElement("Association");
        node.setAttribute("roleName", aAssociatedClassRoleName);
        node.setAttribute("name", aAssociatedUmlClassFullName);
        return node;
    }

    private static Element createAttributeNode(Document doc, String aAttrName, String aAttrValue, String aPredicate) {
        //Element node = doc.createElementNS(nsQuery, "Attribute");
        Element node = doc.createElement("Attribute");
        node.setAttribute("name", aAttrName);
        if (!("".equals(aAttrValue))) {
            node.setAttribute("value", aAttrValue);

        }
        node.setAttribute("predicate", aPredicate);
        return node;
    }

    private static Element createAssociatedAttributeNode(Document doc, String attrName, String value, String _predicate) {
        //Element node = doc.createElementNS(nsQuery, "Attribute");
        Element node = doc.createElement("Attribute");
        node.setAttribute("name", attrName);
        node.setAttribute("predicate", _predicate);
        node.setAttribute("value", value);
        return node;
    }

    
    /*public static Element createAssociationNode(Document doc, String fullName, String roleName) {
    Element node = doc.createElementNS(nsQuery, "Association");
    node.setAttribute("name", fullName);
    node.setAttribute("roleName", roleName);
    return node;
    }*/
    /*
    <ns1:QueryModifier countOnly="true"/>

    <ns1:QueryModifier countOnly="false">
    <ns1:DistinctAttribute>id</ns1:DistinctAttribute>
    </ns1:QueryModifier>

    <ns1:QueryModifier countOnly="false">
    <ns1:AttributeNames>id</ns1:AttributeNames>
    <ns1:AttributeNames>source</ns1:AttributeNames>
    <ns1:AttributeNames>value</ns1:AttributeNames>
    </ns1:QueryModifier>

     */
    /*   Element targetElement = doc.createElementNS(nsQuery, "Target");
    targetElement.setAttribute("name", "gov.nih.nci.caarray.domain.project.Experiment");

    rootNode.appendChild(targetElement);

    Element groupElement = doc.createElementNS(nsQuery, "Group");
    groupElement.setAttribute("logicRelation", "AND");

    targetElement.appendChild(groupElement);

    Element attrElement = doc.createElementNS(nsQuery, "Attribute");
    groupElement.appendChild(attrElement);
    attrElement.setAttribute("name", "id");
    attrElement.setAttribute("predicate", "EQUAL_TO");
    attrElement.setAttribute("value", "95");*/
}
