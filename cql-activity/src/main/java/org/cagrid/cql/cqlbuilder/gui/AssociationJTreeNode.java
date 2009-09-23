package org.cagrid.cql.cqlbuilder.gui;


import javax.swing.tree.DefaultMutableTreeNode;
import org.cagrid.cql.cqlbuilder.metadata.AssociationEdge;
import org.cagrid.cql.cqlbuilder.metadata.UMLClassAttribute;


/**
 * This is a class extending DefaultMutableTreeNode, used in JTree, in "create criterion" window
 * - this is to make the tree dynamic - it creates its nodes when the user expands paths
 * FROM: http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-JTree.html
 * @author Monika
 */
public class AssociationJTreeNode extends DefaultMutableTreeNode {

    public AssociationJTreeNode() {
        super();
    }

    public AssociationJTreeNode(Object object) {
        super(object);
    }
    private boolean areChildrenDefined = false;

    @Override
    public boolean isLeaf() {
        return this.getUserObject() instanceof UMLClassAttribute;
        //return false;
    }

    @Override
    public int getChildCount() {
        if (!areChildrenDefined) {
            defineChildNodes();
        }
        return (super.getChildCount());
    }

    private void defineChildNodes() {
        // You must set the flag before defining children if you
        // use "add" for the new children. Otherwise you get an infinite
        // recursive loop, since add results in a call to getChildCount.
        // However, you could use "insert" in such a case.
        areChildrenDefined = true;

        Object obj = getUserObject();
        if (obj instanceof AssociationEdge) {
            AssociationEdge umlClass = (AssociationEdge) getUserObject();
            for (AssociationEdge c : umlClass.getUmlClass().getAssociations()) {
                add(new AssociationJTreeNode(c));
            }
            for (UMLClassAttribute attr : umlClass.getUmlClass().getAttributes()) {
                add(new AssociationJTreeNode(attr));
            }
            if (getChildCount() == 0) {
                throw new RuntimeException("No i mamy lipę..-- it would mean a class has no attributes -- so it is very baad, as this UML class will be a leaf, therefore might be treated as an attribute... and ClassCastExcaption...;p .. or.. actually not, as it is not distinguished by being a leaft but by its UserObject's class type. Good. So only the user will be mislead by the tree node's icon image:)");
            }
        } else if (obj instanceof UMLClassAttribute) {
            //nothing
        }
    }
}
