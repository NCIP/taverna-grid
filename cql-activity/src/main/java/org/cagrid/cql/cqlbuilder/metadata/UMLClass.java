package org.cagrid.cql.cqlbuilder.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * This class represents single UML class
 * @author Monika
 */
public class UMLClass implements Comparable<UMLClass> {

    //fields
    private HashMap<String, String> fields = new HashMap<String, String>();
    /*   private String name;
    private String packageName;
    private String description;
    private String projectName;//maybe is always same as in semanticmetadata of a service..?
    private String projectVersion;//maybe is always same as in semanticmetadata of a service..?
    private String id;*/
    //attributes
    private LinkedList<UMLClassAttribute> attributes = new LinkedList<UMLClassAttribute>();
    //associations    
    private LinkedList<AssociationEdge> associations = new LinkedList<AssociationEdge>();//redundant
    //generalizations:
    private UMLClass superClass;
    //semantic metadata
    private LinkedList<LinkedHashMap<String, String>> semanticMetadata = new LinkedList<LinkedHashMap<String, String>>();

    public String getId() {
        return fields.get("id");
    }

    public String getName() {
        return fields.get("className");
    }

    public String getPackageName() {
        return fields.get("packageName");
    }

    public String getFullName() {
        return this.getPackageName() + "." + this.getName();
    }

    public void addAssociation(AssociationEdge association) {
        this.associations.add(association);
    }

    public void addAllAssociations(LinkedList<AssociationEdge> associations) {
        this.associations.addAll(associations);
    }

    public LinkedList<AssociationEdge> getAssociations() {
        return associations;
    }

    public void addAttribute(UMLClassAttribute attribute) {
        this.attributes.add(attribute);
    }

    public LinkedList<UMLClassAttribute> getAttributes() {
        return attributes;
    }

    public void addSemanticMetadata(LinkedHashMap<String, String> item) {
        this.semanticMetadata.add(item);
    }

    public LinkedList<LinkedHashMap<String, String>> getSemanticMetadata() {
        return semanticMetadata;
    }

    @Override
    public String toString() {
        return getPackageName() + "." + getName();
    }

    public int compareTo(UMLClass o) {
        return this.toString().compareTo(o.toString());
    }

    public UMLClass getSuperClass() {
        return superClass;
    }

    public void setSuperClass(UMLClass superClass) {
        this.superClass = superClass;
    }

    public void addField(String fieldName, String fieldValue) {
        this.fields.put(fieldName, fieldValue);
    }

    public HashMap<String,String> getFields(){
        return fields;
    }
}
