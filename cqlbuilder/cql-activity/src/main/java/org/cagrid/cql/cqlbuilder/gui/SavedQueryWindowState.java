package org.cagrid.cql.cqlbuilder.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import org.cagrid.cql.cqlbuilder.metadata.UMLClass;

/**
 * This class holds all the info required to load a query back to CQLBuilder
 * @author Monika
 */
public class SavedQueryWindowState {

    private DefaultMutableTreeNode mainGroupNode = null;
    private UMLClass umlClass = null;
    private String fUrl = null;
    private boolean isManual;
    private String manualString = null;
    private boolean addInputForDataServiceCheck = false;

    public SavedQueryWindowState(DefaultMutableTreeNode mainGroupNode, UMLClass umlClass, String fUrl, boolean addInputForDataServiceCheck) {
        isManual = false;
        this.mainGroupNode = mainGroupNode;
        this.umlClass = umlClass;
        this.fUrl = fUrl;
        this.addInputForDataServiceCheck = addInputForDataServiceCheck;
    }

    /**
     * Constructor for manual query
     * @param manualString
     * @param fUrl
     */
    public SavedQueryWindowState(String manualString, String fUrl, boolean addInputForDataServiceCheck) {        
        isManual = true;
        this.manualString = manualString;
        this.fUrl = fUrl;
        this.addInputForDataServiceCheck = addInputForDataServiceCheck;
    }

    public UMLClass getUMLClass() {
        return umlClass;
    }

    public boolean isIsManual() {
        return isManual;
    }

    public String getManualString() {        
        return manualString;
    }

    public DefaultMutableTreeNode getMainGroupNode() {
        return mainGroupNode;
    }

    public String getUrl() {
        return fUrl;
    }

    boolean isManual() {
        return isManual;
    }

    public boolean isAddInputForDataServiceCheck() {
        return addInputForDataServiceCheck;
    }
}
