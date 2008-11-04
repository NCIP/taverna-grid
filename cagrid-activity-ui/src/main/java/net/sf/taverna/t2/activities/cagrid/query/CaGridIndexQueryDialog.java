package net.sf.taverna.t2.activities.cagrid.query;

/*
 * 
 * Author: Wei Tan
 */

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import javax.swing.JComboBox;

import net.sf.taverna.t2.lang.ui.ShadedLabel;


/**
 * a dialog for helping create scavengers for caGrid registries that are not the default registry.
 *
 */
public class CaGridIndexQueryDialog extends JPanel {

	private static final long serialVersionUID = -57047613557546678L;
	final int q_size=3;//max query item size
	//TODO: add more well-know index service URLs
	private String[] URLs = { "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService",
			"http://cagrid01.bmi.ohio-state.edu:8080/wsrf/services/DefaultIndexService",
			"Input Your Own Index Service URL Here..."};
	//private JTextField indexServiceURL= new JTextField("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService");
	//private JTextField queryCriteria = new JTextField("");
	public JComboBox indexServiceURLs = new JComboBox(URLs);
	public JComboBox[] queryValue = new JComboBox[q_size];
	//public JTextField[] queryValue = new JTextField[q_size];
	private String[] queryStrings = { "None", "Search String", "Point Of Contact", "Service Name", "Operation Name", "Operation Input",
			"Operation Output","Operation Class", "Research Center","Concept Code",
			"Domain Model for Data Services"};
	private String[] queryValues = {};

	//Create the combo box, select item at index 0.
	public JComboBox[]  queryList = new JComboBox[q_size];
	public JButton addQuery = new JButton("Add Service Query");
    public JButton removeQuery = new JButton("Remove Service Query");
    public int q_count =1;
	



    /**
     * Default constructor.
     *
     */
    public CaGridIndexQueryDialog() {
        super();
        GridLayout layout = new GridLayout(q_size+5, 2);
        setLayout(layout);
        for(int i=0;i<q_size;i++){
        	queryValue[i]=new JComboBox(queryValues);
        	queryValue[i].setEditable(true);
        	queryList[i] = new JComboBox(queryStrings);     	
        }
        add(new ShadedLabel("Location (URL) of the index service: ", ShadedLabel.BLUE, true));
        indexServiceURLs.setEditable(true);
        indexServiceURLs.setToolTipText("caGrid Services will be retrieved from the index service whose URL you specify here!");
        add(indexServiceURLs);
        
        add(addQuery);
        add(removeQuery);
        add(new ShadedLabel("Service Query Criteria: ", ShadedLabel.BLUE, true));        
        //queryCriteria.setToolTipText("Service Query will use the query criteria you specify here!");        
        add(new ShadedLabel("Service Query Value: ", ShadedLabel.BLUE, true));
        for(int i=0;i<q_size;i++){
        	queryValue[i].setToolTipText("Service Query will use the query value you specify here!");
            add(queryList[i]);
            add(queryValue[i]);  	
        }
        for(int i=1;i<q_size;i++){
        	
            queryList[i].setVisible(false);
            queryValue[i].setVisible(false);  	
        }
        
        
    
        //add(Box.createHorizontalGlue());add(Box.createHorizontalGlue());
        setPreferredSize(this.getPreferredSize());
        setMinimumSize(this.getPreferredSize());
        setMaximumSize(this.getPreferredSize());
    }
    
    /**
     * 
     * @return the string representation of the IndexServiceURL
     */
    public String getIndexServiceURL() {
        return (String) indexServiceURLs.getSelectedItem();
    }

    /**
     * 
     * @return the string representation of the QueryCriteria
     */
    public String getQueryCriteria(int i) {
        return (String) queryList[i].getSelectedItem();
    }
    
    /**
     * 
     * @return the string representation of the QueryValue
     */
    public String getQueryValue(int i) {
        return (String) queryValue[i].getSelectedItem();
    }
   
}


