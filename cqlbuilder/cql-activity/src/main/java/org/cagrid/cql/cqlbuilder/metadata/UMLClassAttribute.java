package org.cagrid.cql.cqlbuilder.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * This class represents attribute of an UML class
 * @author Monika
 */
public class UMLClassAttribute {

    //fields
    private HashMap<String, String> fields = new HashMap<String, String>();
    /*   private String name;
    private String publicID;
    private String version;
    private String description;
    private String dataTypeName;*/
    // value domain
    private String valueDomainLongName;
    private String valueDomainUnitOfMeasure;
    // semantic metadata
    private LinkedList<LinkedHashMap<String, String>> semanticMetadata = new LinkedList<LinkedHashMap<String, String>>();

    public String getName() {
        return fields.get("name");
    }

    public LinkedList<LinkedHashMap<String, String>> getSemanticMetadata() {
        return semanticMetadata;
    }

    public void addSemanticMetadata(LinkedHashMap<String, String> semanticMetadataItem) {
        this.semanticMetadata.add(semanticMetadataItem);
    }

    public String getValueDomainLongName() {
        return valueDomainLongName;
    }

    public void setValueDomainLongName(String valueDomainLongName) {
        this.valueDomainLongName = valueDomainLongName;
    }

    public String getValueDomainUnitOfMeasure() {
        return valueDomainUnitOfMeasure;
    }

    public void setValueDomainUnitOfMeasure(String valueDomainUnitOfMeasure) {
        this.valueDomainUnitOfMeasure = valueDomainUnitOfMeasure;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void addField(String fieldName, String fieldValue) {
        this.fields.put(fieldName, fieldValue);
    }

    public HashMap<String, String> getFields() {
        return fields;
    }
}

