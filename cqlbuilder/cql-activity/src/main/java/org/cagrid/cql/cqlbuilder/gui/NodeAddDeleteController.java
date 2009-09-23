package org.cagrid.cql.cqlbuilder.gui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class is for adding and removing nodes from the query tree as well as for
 * keeping eye on empty Group nodes and judging whether the query is correct or not
 * @author Monika
 */
public class NodeAddDeleteController {

    //keeps track of if the query can be submitted - if there are any empty groups or  not
    public int numberOfEmptyGroups = 0;
    public static final String TREE_EMPTY_GROUP = "<add criteria>";

    public void resetCounter() {
        numberOfEmptyGroups = 0;
    }

    public void incCounter(){
        numberOfEmptyGroups++;
    }

    /**
     * Returns new node
     * @param targetNode
     * @param userObject
     * @param pos
     * @return
     */
    public DefaultMutableTreeNode newTreeNode(DefaultMutableTreeNode targetNode, Object userObject, int pos, boolean isEmptyForbidden) {
        removeEmptyGroupMark(targetNode);
        if (pos == -1) {//insert at the end
            pos = targetNode.getChildCount();
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);
        targetNode.insert(node, pos);
        if (isEmptyForbidden) {
            /*insert new <add criterion> node*/
            addEmptyGroupMarkIfEmpty(node);
        }
        return node;
    }

    /**
     * Returns the last before the removed one
     * @param targetParentNode
     * @param pos
     * @return
     */
    public DefaultMutableTreeNode deleteTreeNode(DefaultMutableTreeNode targetParentNode, int pos) {
        if (targetParentNode.getChildAt(pos).isLeaf()) {
            targetParentNode.remove(pos);
        } else {
            //it is not enugh just to simply delete the node
            // as we have to keep track on how many empty groups we have (zero or not)
            DefaultMutableTreeNode rootNodeToDelete = (DefaultMutableTreeNode) targetParentNode.getChildAt(pos);
            DefaultMutableTreeNode currNode = rootNodeToDelete.getLastLeaf();
            DefaultMutableTreeNode currNodesParent = (DefaultMutableTreeNode) currNode.getParent();


            while (!currNode.equals(rootNodeToDelete)) {
                if (currNode.getUserObject().equals(TREE_EMPTY_GROUP)) {
                    numberOfEmptyGroups--;
                }
                currNodesParent.remove(currNodesParent.getIndex(currNode));

                currNode =
                        currNodesParent.getLastLeaf();

                currNodesParent = (DefaultMutableTreeNode) currNode.getParent();
            }

            currNodesParent.remove(currNodesParent.getIndex(currNode));


        }
        addEmptyGroupMarkIfEmpty(targetParentNode);
        if (pos >= targetParentNode.getChildCount()) {
            pos--;
        }
        return (DefaultMutableTreeNode) targetParentNode.getChildAt(pos);
    }

    /**
     * in case targetNode has empty group marker child it removes it
     * also it decreases number of empty gorups
     * @param node
     * @return if empty gorup mark was removed or not (if not it means it wasnot present)
     */
    private boolean removeEmptyGroupMark(DefaultMutableTreeNode selectedNode) {
        if (selectedNode.getChildCount() == 1 && ((DefaultMutableTreeNode) selectedNode.getChildAt(0)).getUserObject().equals(TREE_EMPTY_GROUP)) {
            //remove <add criteria> only child
            selectedNode.removeAllChildren();
            numberOfEmptyGroups--;
            return true;
        }

        return false;
    }

    /**
     * in case targetNode has no children, it adds empty group marker child
     * - this is to indicate to the user he has to enter something there
     * also it increases number of empty gorups
     * @param targetNode
     * @return if empty gorup mark was added or not
     */
    private boolean addEmptyGroupMarkIfEmpty(DefaultMutableTreeNode selectedNode) {
        if (selectedNode.getChildCount() == 0) {
            selectedNode.insert(new DefaultMutableTreeNode(TREE_EMPTY_GROUP), 0);
            numberOfEmptyGroups++;
            return true;
        }
        return false;
    }

    public boolean isQueryValid() {
        return numberOfEmptyGroups == 0;
    }
}
