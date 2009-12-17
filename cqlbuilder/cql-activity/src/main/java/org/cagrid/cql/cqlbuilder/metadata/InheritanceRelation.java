package org.cagrid.cql.cqlbuilder.metadata;

/**
 * This class represents inheritance relation between two UML classes
 * @author Monika
 */

public class InheritanceRelation {
    /* by 'redundant' I mean this information is being held also somewhere else
     * (here: below)
     * (which has no 'redundant' comment), but this one is still neccessary
     */
    private UMLClass subClass;//redundant
    private UMLClass superClass;//redundant

    private String subClassRefId;
    private String superClassRefId;

    public String getSubClassRefId() {
        return subClassRefId;
    }

    public void setSubClassRefId(String subClassRefId) {
        this.subClassRefId = subClassRefId;
    }

    public String getSuperClassRefId() {
        return superClassRefId;
    }

    public void setSuperClassRefId(String superClassRefId) {
        this.superClassRefId = superClassRefId;
    }

    public UMLClass getSubClass() {
        return subClass;
    }

    public void setSubClass(UMLClass subClass) {
        this.subClass = subClass;
    }

    public UMLClass getSuperClass() {
        return superClass;
    }   

    public void setSuperClass(UMLClass superClass) {
        this.superClass = superClass;
    }

    
}
