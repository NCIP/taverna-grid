package org.cagrid.cql.cqlbuilder.metadata;

/**
 * This class represents association edge of an association
 * @author Monika
 */
public class AssociationEdge {

    private String roleName;
    private String minCardinality;
    private String maxCardinality;
    private String refId;
    private UMLClass umlClass;//redundant
 
    /*    @Override
    public String toString() {
    return "        Role name: " + roleName + "\n        Min cardinality: " + minCardinality +
    "\n        Max cardinality: " + maxCardinality+"\n        Ref id: "+refId;
    }*/
    @Override
    public String toString() {
        return umlClass.getFullName() + " (role: " + roleName + ")";
    }

    public UMLClass getUmlClass() {
        return umlClass;
    }

    public void setUmlClass(UMLClass umlClass) {
        this.umlClass = umlClass;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    /*   ..public UMLClass getReference() {
    return reference;
    }

    public void setReference(UMLClass reference) {
    if(this.reference!=null)
    throw new RuntimeException("why oh why???");
    this.reference = reference;
    }*/
    public String getMaxCardinality() {
        return maxCardinality;
    }

    public void setMaxCardinality(String maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public String getMinCardinality() {
        return minCardinality;
    }

    public void setMinCardinality(String minCardinality) {
        this.minCardinality = minCardinality;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
