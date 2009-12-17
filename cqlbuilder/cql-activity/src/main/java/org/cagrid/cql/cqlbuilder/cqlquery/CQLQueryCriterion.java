package org.cagrid.cql.cqlbuilder.cqlquery;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * This class holds info about the Criterion chosen by the user is gui window
 * Instances of this class are contained in JTree query tree as "UserObjects" of its nodes.
 * @author Monika
 */
public class CQLQueryCriterion {

    //this affetcs the beginning of a tree node label containing a criterion
    public static final String TREE_CRITERION_PREFIX = ">>";
    //these two fields hold info about the attribute and predicate
    private String _predicate;
    private String _value;
   
    //either _attributeName or _associatedAtributeName is set
    private String _attributeName;
   
    private boolean wereAssociarionsSet = false;
    private LinkedList<CQLQueryAssociation> _associations = new LinkedList<CQLQueryAssociation>();

    private String _associatedAtributeName;

    //this influences the way the criterion is displayed in the query tree in main window
    @Override
    public String toString() {
        String attr;
        if (!wereAssociarionsSet) {
            attr = _attributeName;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < _associations.size(); i++) {
                CQLQueryAssociation assoc = _associations.get(i);
                sb.append(assoc.getUmlClassName()).append("(role:" + assoc.getRoleName() + ")->");
            }
            sb.append(_associatedAtributeName);
            attr = sb.toString();
        }
        return TREE_CRITERION_PREFIX + "\"" + attr + "\"" + " " + _predicate + (("".equals(_value))?"":(" '" + _value + "'"));//;
    }

    public static String[] getPredicates() {
        return new String[]{"EQUAL_TO",
                    "LIKE",
                    "GREATER_THAN",
                    "GREATER_THAN_EQUAL_TO",
                    "IS_NOT_NULL",
                    "IS_NULL",
                    "LESS_THAN",
                    "LESS_THAN_EQUAL_TO",
                    "NOT_EQUAL_TO"};
    }

    public static List<String> getNonValuePredicates() {
        return Arrays.asList(new String[]{
                    "IS_NOT_NULL",
                    "IS_NULL"});
    }

    /***getters and setters****************************************************/
    public String get_attributeName() {
        return _attributeName;
    }

    public void set_attributeName(String _attribute) {
        this._attributeName = _attribute;
    }

    public String get_predicate() {
        return _predicate;
    }

    public void set_predicate(String _predicate) {
        this._predicate = _predicate;
    }

    public String get_value() {
        return _value;
    }

    public void set_value(String _value) {
        this._value = _value;
    }        

    /** nestedAssociations start from top */
    public void setAssociations(LinkedList<CQLQueryAssociation> nestedAssociations, String attrName) {
        _associations = nestedAssociations;
        _associatedAtributeName = attrName;
        wereAssociarionsSet = true;
    }

    public LinkedList<CQLQueryAssociation> getAssociationsList() {
        return _associations;
    }

    public String get_associatedAtributeName() {
        return _associatedAtributeName;
    }
    
    public boolean areAssociationsSet() {
        return wereAssociarionsSet;
    }
}
