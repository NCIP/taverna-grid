package org.cagrid.cql.cqlbuilder.cqlquery;

public class CQLQueryAssociation {
    private String umlClassName;
    private String umlClassPackageName;
    private String roleName;

    public CQLQueryAssociation(String umlName, String umlPackageName, String roleName) {
        this.roleName=roleName;
        this.umlClassName=umlName;
        this.umlClassPackageName=umlPackageName;
    }

    String getRoleName() {
        return roleName;
    }

    String getUmlClassName() {
        return umlClassName;
    }

    String getUmlClassFullName() {
        return umlClassPackageName+"."+umlClassName;
    }
}
