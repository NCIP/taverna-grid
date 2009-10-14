/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.cagrid.ui.servicedescriptions;

import gov.nih.nci.cadsr.umlproject.domain.Project;
import gov.nih.nci.cadsr.umlproject.domain.UMLClassMetadata;
import gov.nih.nci.cadsr.umlproject.domain.UMLPackageMetadata;
import org.cagrid.cadsr.client.CaDSRUMLModelService;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.cagrid.activity.config.CaGridConfiguration;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

/**
 * Dialog that lets user specify query criteria to be used when
 * searching for caGrid services.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public abstract class CaGridServicesSearchDialog extends HelpEnabledDialog {

	private static Logger logger = Logger.getLogger(CaGridServicesSearchDialog.class);

	final int max_query_size = 5; // max number of queries
	
	// CaGrid name the user wishes to add service from (e.g. Training, Production , etc.) that can
	// be configured from the preferences panel
	private String[] caGridNames;
	public JComboBox caGridNamesComboBox;

	// Index Services (one per caGrid)
	private String[] indexServicesURLs;
	
	// caDSR Service used to search for caGrid services
	private String[] caDSRServicesURLs;	
	
	// List of queries to be used when searching for caGrid services 
	private CaGridServiceQuery[] serviceQueryList;
	
	public JComboBox[]  queryList = new JComboBox[max_query_size];
	private String[] queryStrings = { "None", "Search String", "Point Of Contact", "Service Name", "Operation Name", "Operation Input",
			"Operation Output","Operation Class", "Research Center","Concept Code",
			"Domain Model for Data Services"};
	public JComboBox[] queryValue = new JComboBox[max_query_size];
	private String[] queryValues = {};
	
	// Label showing the progress when update metadata process is running
	private JLabel updatingMetadata ;
	
	public CaGridServicesSearchDialog()  {
		super((Frame) null, "caGrid Services Search", false, null); // create a non-modal dialog - updating metadata takes too long to have the dialog block everything else
		initComponents();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		
		CaGridConfiguration configuration = CaGridConfiguration.getInstance();
		// Get default list of CaGridS - keys in the map contain CaGrid names and values contain 
		// various properties set for the CaGrid (Index Service URL, AuthN service URL, Dorian Service URL, 
		// proxy lifetime and CaDSR URL).
		HashSet<String> caGridNamesSet = new HashSet<String>(configuration.getDefaultPropertyMap().keySet());
		// Get all other caGridS that may have been configured though preferences (may include 
		// the default ones as well if some of their values have been changed), 
		// but set will ignore duplicates so we are OK
		caGridNamesSet.addAll(configuration.getKeys());
		caGridNames = caGridNamesSet.toArray(new String[caGridNamesSet.size()]);
		caGridNamesComboBox = new JComboBox(caGridNames);
		// Get Index Service URLs for all caGrids
		indexServicesURLs = new String[caGridNames.length];
		for (int i = 0; i < caGridNames.length; i++){
			indexServicesURLs[i] = configuration.getPropertyStringList(caGridNames[i]).get(0);
		}
		// Get CaDSR Service URLs for all caGrids, if defined
		caDSRServicesURLs = new String[caGridNames.length];
		for (int i = 0; i < caGridNames.length; i++){
			caDSRServicesURLs[i] = configuration.getPropertyStringList(caGridNames[i]).get(4);
		}
		
		this.getContentPane().setLayout(new BorderLayout());
        
        JPanel indexServicePanel = new JPanel(new BorderLayout());
        indexServicePanel.setBorder(new EmptyBorder(10,10,10,10));
        for(int i=0;i<max_query_size;i++){
        	queryValue[i]=new JComboBox(queryValues);
        	queryValue[i].setEditable(true);
        	FontMetrics fm = getFontMetrics(queryValue[i].getFont());
        	int width = 50 + fm.stringWidth("RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen"); //the longest string
        	queryValue[i].setPreferredSize(new Dimension(width,queryValue[i].getPreferredSize().height));
        	queryValue[i].setMinimumSize(new Dimension(width,queryValue[i].getPreferredSize().height));
        	queryList[i] = new JComboBox(queryStrings);     
        	
        	final JComboBox final_queryValue = queryValue[i];
            // Listener for queryList comboBox - if selected service query criteria is "Operation Class",
        	// "Operation Input" or "Operation Output", retrieve possible query values from caDSR Service
			queryList[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String queryCriteriaString = (String) ((JComboBox) ae
							.getSource()).getSelectedItem();
					// Use values from classNameArray - later on should get them from caDSR
					if (queryCriteriaString.equals("Operation Class")
							|| queryCriteriaString.equals("Operation Input")
							|| queryCriteriaString.equals("Operation Output")) {
						// Should contact caDSR really for the up-to-date values
						// of classNameArray
						final_queryValue.setModel(new DefaultComboBoxModel(
								classNameArray));
					} else {
						// keep the combobox empty and editable
						String[] emptyValue = {};
						final_queryValue.setModel(new DefaultComboBoxModel(emptyValue));

					}
					final_queryValue.validate();
				}
			});
        }
        indexServicePanel.add(new ShadedLabel("Select grid", ShadedLabel.BLUE, true), BorderLayout.WEST);
        caGridNamesComboBox.setToolTipText("caGrid services will be retrieved from the grid which you specify here");
        indexServicePanel.add(caGridNamesComboBox, BorderLayout.CENTER);
        this.getContentPane().add(indexServicePanel, BorderLayout.NORTH);
        
        JPanel serviceQueryPanel = new JPanel(new BorderLayout());
        serviceQueryPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder(EtchedBorder.LOWERED)));
        // Panel with queries
        JPanel queryPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5,0,5,5);
        queryPanel.add(new ShadedLabel("Service query criteria: ", ShadedLabel.BLUE, true), c);        
		c.gridx = 1;
		c.gridy = 0;
        queryPanel.add(new ShadedLabel("Service query value: ", ShadedLabel.BLUE, true), c);
        
        c.gridy = 1;
        for (int i=0 ; i<max_query_size; i++){
        	c.gridx = 0;
        	queryValue[i].setToolTipText("Service search will use the query value you specify here");
        	queryPanel.add(queryList[i], c);
        	c.gridx = 1;
        	queryPanel.add(queryValue[i], c);  	
        	c.gridy++;
        }
        serviceQueryPanel.add(queryPanel, BorderLayout.CENTER);
        this.getContentPane().add(serviceQueryPanel, BorderLayout.CENTER);

        // Panel with buttons
        JPanel queryButtonsPanel = new JPanel();
        //queryButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        queryButtonsPanel.setLayout(new GridBagLayout());
        queryButtonsPanel.setBorder(new EmptyBorder(0,0,0,25));
        
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 10, 0, 0);
        updatingMetadata = new JLabel("");
        queryButtonsPanel.add(updatingMetadata, gbc);
        
        final JButton updateCaDSRDataButton = new JButton("Update caDSR metadata");
        updateCaDSRDataButton.setToolTipText("Get an updated UML class list from caDSR Service. \n" +
		"This operation may take a few minutes depending on network status.");
        updateCaDSRDataButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				Thread updateCaDSRDataThread = new Thread("Updating caDSR metadata") {
					public void run() {
						
   					 	updatingMetadata.setIcon(WorkbenchIcons.workingIcon);
						updatingMetadata.setText("Updating metadata ...");
						
						// Update the value of classNameArray
                    	 ArrayList<String> classNameList = new ArrayList<String>();
    					 Project[] projs = null;

    					 CaDSRUMLModelService cadsr  = null;
    					 UMLPackageMetadata[] packs = null;
    					 UMLClassMetadata[] classes = null;
    					 logger.info("Updating caDSR Metadata: using caDSR Service " + caDSRServicesURLs[caGridNamesComboBox.getSelectedIndex()]);
    					 
    					 //Note: the try-catch module should be with fine granularity
    					 try {
    						 cadsr = new CaDSRUMLModelService(caDSRServicesURLs[caGridNamesComboBox.getSelectedIndex()]);		                					            
    					     projs = cadsr.findAllProjects();
    					 }
    					 catch (Exception e) {
    						 e.printStackTrace();
    					 }
    					 
    					 if(projs !=null){
    						 for (int i = 0; i<projs.length;i++){
    							 Project project = projs[i];
    							 logger.info("Project: "+ project.getLongName());
    							 //the bridg and c3pr project always yield error -- don't know why. so simply bypass them
    							 if(!project.getShortName().equals("BRIDG")&&!project.getShortName().equals("C3PR")){
    								 try {
    									 packs = cadsr.findPackagesInProject(project);
    								 }
    								 catch (Exception e) {
    									 e.printStackTrace();
    								 }
    								 if(packs !=null){
    									 for(int j= 0;j<packs.length;j++){
    										 UMLPackageMetadata pack = packs[j];
    										 logger.info("\t-" + pack.getName());
    										 try {
    											 classes = cadsr.findClassesInPackage(project, pack.getName());
    										 }
    										 catch (Exception e) {
    											 e.printStackTrace();
    										 }
    										 if(classes !=null){
    											 for (int k=0;k<classes.length;k++){
    												 UMLClassMetadata clazz = classes [k];
    												 logger.info("\t\t-"+clazz.getName());
    												 if(!classNameList.contains(clazz.getName()))
    													 //classNameList is updated here!
    													 classNameList.add((String)clazz.getName());
    												 else {
    													 //logger.info("Duplicated class name found.");
    												 }
    											 }
    										 }
    									 }
    								 }
    								 else{
										 logger.info("0 packages found in project " + project.getLongName()); 
    								 }
    							 }
    						 }
    					 }
    					 updatingMetadata.setText("Finished updating metadata.");
    					 updatingMetadata.setIcon(WorkbenchIcons.greentickIcon);
    					 String [] clsNameArray;
    					 // If the retrieved class name list is not empty, update the static datatype classNameArray
    					 if(!classNameList.isEmpty()){
    						 clsNameArray = (String[]) classNameList.toArray(new String[0]);
    						 Arrays.sort(clsNameArray,String.CASE_INSENSITIVE_ORDER);		                					       
    						 logger.info("=========Class Names Without Duplications=============");
    						 for(int i=0;i<clsNameArray.length;i++){
    							 logger.info(clsNameArray[i]);
    						 }
    						 classNameArray  = clsNameArray;
    						 JOptionPane.showMessageDialog(null, "caDSR metadata has been updated.\nThere are " + classNameArray.length + " classes in the list.", null, JOptionPane.INFORMATION_MESSAGE);   
    						 logger.info("caDSR metadata has been updated. There are " + classNameArray.length + " classes in the list.");
    					 }
    					 else{
    						 //the current value of the GT4ScavengerHelper.classNameArray is not updated
    						 JOptionPane.showMessageDialog(null,"Empty class list retrieved from the caDSR Service.\nUsing the default class names.", null, JOptionPane.INFORMATION_MESSAGE);
    						 logger.info("Empty class name list retrieved from caDSR Service. Using the default class names.");
    					 }	 
                    }
            	};
            	updateCaDSRDataThread.start();
            }
		});
        // Disable 'update caDSR metadata' button if there is no caDSR service URL defined
        caGridNamesComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (caDSRServicesURLs[caGridNamesComboBox.getSelectedIndex()].equals("")){
					updateCaDSRDataButton.setEnabled(false);
				}
				else{
					updateCaDSRDataButton.setEnabled(true);
				}
			}
        });
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				List<CaGridServiceQuery> qList = new ArrayList<CaGridServiceQuery>();
                for (int i=0; i<max_query_size; i++){
                	if(!getQueryCriteria(i).equals("None") && !getQueryValue(i).equals("")){
                		qList.add(new CaGridServiceQuery(getQueryCriteria(i),getQueryValue(i)));
                	}	
                }

                serviceQueryList = (CaGridServiceQuery[]) qList.toArray(new CaGridServiceQuery[0]);
				addRegistry(getCaGridName(), 
						getIndexServiceURL(),
						serviceQueryList);
				
				setVisible(false);
				dispose();				
			}
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();				
			}
        });
 
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 0.0;
		gbc.ipadx = 0;

		gbc.gridx = 1;
        queryButtonsPanel.add(okButton, gbc);
        
		gbc.gridx = 2;
        queryButtonsPanel.add(updateCaDSRDataButton, gbc);
        
		gbc.gridx = 3;
        queryButtonsPanel.add(cancelButton, gbc);   
        
        this.getContentPane().add(queryButtonsPanel, BorderLayout.SOUTH);

        // Set the default CaGrid to Production CaGrid
		caGridNamesComboBox.setSelectedItem(CaGridConfiguration.PRODUCTION_CAGRID_NAME);
		
        this.setLocation(50,50);
        this.pack();
	}
	
	protected abstract void addRegistry(String caGridName, String indexServiceURL,
			CaGridServiceQuery[] serviceQueryList);
	
    /**
     * 
     * @return the string representation of the n-th query criteria
     */
    private String getQueryCriteria(int i) {
        return (String) queryList[i].getSelectedItem();
    }
    
    /**
     * 
     * @return the string representation of the n-th query value
     */
    private String getQueryValue(int n) {
        return (String) queryValue[n].getSelectedItem();
    }
    
    /**
     * 
     * @return the selected Index Service URL
     */
    public String getIndexServiceURL() {
        return indexServicesURLs[caGridNamesComboBox.getSelectedIndex()];
    }
    
    /**
     * 
     * @return the selected CaGrid name
     */
    public String getCaGridName() {
        return (String)caGridNamesComboBox.getSelectedItem();
    }
    
    /**
     * 
     * @return the selected Index Service URL
     */
    public CaGridServiceQuery[] getServiceQueryList() {
        return serviceQueryList;
    }

	// Give an initial value to classNameArray.
	// The data is retrieved from production caDSR at
    // http://cadsr-dataservice.nci.nih.gov/wsrf/services/cagrid/CaDSRDataService
    // on October 14th, 2009.
    
    /*
     * We used the following python script (written by Stian Soiland-Reyes) to format 
     * the list of classnames. The file "classes" contains one class name per row.
     * 
     * classes = open("classes").read().split()
     * quoted = ['"%s", ' % k for k in classes]
     * line = ""
     * lines = []
		for q in quoted:
		 if len(line + q) > 80:
		    lines.append(line)
		    line = q
		  else:
		    line += q
		lines.append(line)
		print "\n".join(lines)
     */
	public static String[] classNameArray = {
		"A2Conjugate", "A2Experiment", "A2LP4Parameters", "A2Plate", "A2Sample", 
		"A2Spot42", "A2SpotData", "A2SpotSetup", "A2SpotsStatistics", 
		"A2StandardCurve", "A2Well", "AbsoluteCodingSchemeVersionReference", 
		"AbsoluteCodingSchemeVersionReferenceList", "AbsoluteNeutrophilCount", 
		"AbstractAdverseEventTerm", "AbstractArrayData", "AbstractBioMaterial", 
		"AbstractCaArrayEntity", "AbstractCaArrayObject", "AbstractCancerModel", 
		"AbstractCharacteristic", "AbstractChromosomalSegment", "AbstractContact", 
		"AbstractDataColumn", "AbstractDesignElement", "AbstractDomainObject", 
		"AbstractExperimentGraphNode", "AbstractFactorValue", "Abstraction", 
		"AbstractMeddraDomain", "AbstractMicroarrayParameters", 
		"AbstractParameterValue", "AbstractParticipant", 
		"AbstractParticipantObservation", "AbstractPosition", "AbstractProbe", 
		"AbstractProbeAnnotation", "AbstractSpecimen", 
		"AbstractSpecimenCollectionGroup", "AbstractStudyDisease", "AbstractValue", 
		"Accession", "AccessionCharacteristics", "AccessRights", 
		"AcquisitionProcedure", "ActionSuccessor", "ActivationMethod", 
		"ActiveIngredient", "ActiveObservation", "ActiveOption", "ActiveSite", 
		"Activity", "ActivityRelationship", "ActivitySummary", "ActivityType", 
		"AcuteGraftVersusHostDisease", "AD", "Adapter", "Add", "AdditionalFinding", 
		"AdditionalFindings", "AdditionalInformation", "AdditionalOrganismName", 
		"AdditionalPathologicFinding", "Address", "AddToReferenceParameters", 
		"AdjacencyMatrix", "AdministeredComponent", 
		"AdministeredComponentClassSchemeItem", "AdministeredComponentContact", 
		"AdministeredDrug", "ADPart", "AdverseEvent", "AdverseEventAttribution", 
		"AdverseEventCtcTerm", "AdverseEventData", "AdverseEventDetail", 
		"AdverseEventMeddraLowLevelTerm", "AdverseEventResponseDescription", 
		"AdverseEventTherapy", "ADXP", "ADXPADL", "ADXPAL", "ADXPBNN", "ADXPBNR", 
		"ADXPBNS", "ADXPBR", "ADXPCAR", "ADXPCEN", "ADXPCNT", "ADXPCPA", "ADXPCTY", 
		"ADXPDAL", "ADXPDINST", "ADXPDINSTA", "ADXPDINSTQ", "ADXPDIR", "ADXPDMOD", 
		"ADXPDMODID", "ADXPINT", "ADXPPOB", "ADXPPRE", "ADXPSAL", "ADXPSTA", "ADXPSTB", 
		"ADXPSTR", "ADXPSTTYP", "ADXPUNID", "ADXPUNIT", "ADXPZIP", 
		"AECausalAssessment", "AEIdentification", "AeTerminology", "Agent", 
		"AgentAlias", "AgentOccurrence", "AgentSynonym", "AgentTarget", 
		"AlcoholHealthAnnotation", "Algorithm", "Alignment", "Aliquot", 
		"AlleleFrequency", "Allergy", "AlternateProteinHit", "Amendment", 
		"AmendmentApproval", "AmplifiedOverlappedChromosomalSegmentWithSample", 
		"AmplifiedOverlappedChromosomalSegmentWithSource", "Analysis", "AnalysisGroup", 
		"AnalysisGroupResult", "AnalysisParameters", "AnalysisRecord", 
		"AnalysisRoutine", "AnalysisRun", "AnalysisRunSet", "AnalysisVariable", 
		"Analyte", "AnalyteProcessingStep", "AnalyzedDataSet", "AnatomicEntity", 
		"AnatomicSite", "Animal", "AnimalAvailability", "AnimalDistributor", 
		"AnimalModel", "AnimalParticipant", "AnnotatableEntity", "AnnotatableEvent", 
		"Annotation", "AnnotationColumn", "AnnotationCriterion", 
		"AnnotationEventParameters", "AnnotationManager", "AnnotationOfAnnotation", 
		"AnnotationSet", "AnnotationSetRequest", "AnnotationValueSet", "Anomaly", 
		"AnomalyType", "AnovaParameter", "AnovaResult", "Antibody", "Antigen", "ANY", 
		"Application", "ApplicationContext", "ApplicationSponsor", "AppliedParameter", 
		"ApprovalStatus", "AracneParameter", "Array", "ArrayDataType", "ArrayDesign", 
		"ArrayDesignDetails", "ArrayGroup", "ArrayManufacture", 
		"ArrayManufactureDeviation", "ArrayProvider", "ArrayReporter", 
		"ArrayReporterCytogeneticLocation", "ArrayReporterPhysicalLocation", 
		"Artefact", "ArtifactMask", "Assay", "AssayDataPoint", "AssayType", "Assembly", 
		"Assessment", "AssessmentPerformedActivityRelationship", 
		"AssessmentRelationship", "Assessor", "AssociatableElement", 
		"AssociatedConcept", "AssociatedConceptList", "AssociatedData", 
		"AssociatedDataList", "AssociatedElement", "AssociatedFile", 
		"AssociatedObservationWrapper", "AssociatedPerson", "Association", 
		"AssociationData", "AssociationIdentification", "AssociationInstance", 
		"AssociationList", "AssociationQualification", "AssociationTarget", 
		"AssociativeFunction", "Atom", "Attachment", "Attribute", 
		"AttributeSetDescriptor", "AttributeTypeMetadata", "Audit", "AuditEvent", 
		"AuditEventDetails", "AuditEventLog", "AuditEventQueryLog", "Author", 
		"Availability", "AvailableQuantity", "AveragedSpotInformation", 
		"BACCloneReporter", "BAG", "BaseHaematologyPathologyAnnotation", 
		"BaselineData", "BaselineHistoryPE", "BaselineStage", 
		"BasePathologyAnnotation", "BaseShipment", 
		"BaseSolidTissuePathologyAnnotation", "BaseUnit", "BasicHistologicGrade", 
		"BehavioralMeasure", "BibliographicReference", "BIN", "Binary", 
		"BinaryContent", "BindingSite", "BioAssay", "BioAssayCreation", "BioAssayData", 
		"BioAssayDataCluster", "BioAssayDatum", "BioAssayDimension", "BioAssayMap", 
		"BioAssayMapping", "BioAssayTreatment", "BiocartaMap", "BiocartaReport", 
		"BiocartaSource", "BioCharacteristics", "BiochemicalReaction", "BioDataCube", 
		"BioDataTuples", "BioDataValues", "BioEvent", "Biohazard", "BiologicalProcess", 
		"BiologicMaterial", "BioMarker", "BioMarkerList", "BioMarkerValue", 
		"BioMaterial", "Biomaterial", "BioMaterial_package", 
		"BiomaterialKeywordSearchCriteria", "BioMaterialMeasurement", 
		"BiomaterialSearchCriteria", "Biopolymer", "BioSample", "BioSequence", 
		"BioSource", "BioSpecimen", "BL", "BlackoutDate", "BLNONNULL", "BloodContact", 
		"BloodSmearSlide", "BN", "Book", "BooleanColumn", "BooleanParameter", 
		"BreastCancerAccessionCharacteristics", "BreastCancerBiomarkers", 
		"BreastCancerComputerVisionPanel01", "BreastCancerMolecularPanel01", 
		"BreastCancerTNMFinding", "BreastMargin", "BreastMarginInvolved", 
		"BreastMarginUninvolved", "BreastNegativeSurgicalMargin", 
		"BreastPathologyAnnotation", "BreastPositiveSurgicalMargin", 
		"BreastSpecimenCharacteristics", "BreastSpecimenNottinghamHistologicScore", 
		"BreastSpecimenPathologyAnnotation", "BreastSurgicalPathologySpecimen", 
		"BRIDGRelativeTS", "CaArrayEntityReference", "CaArrayFile", 
		"CaDSRRegistration", "CAL", "CalciumBindingRegion", "CalculatedMeasurement", 
		"CalculatedMeasurementProtocol", "Calculation", "CalculationResult", 
		"Canceled", "CancerClinicalPanel", "CancerModel", "CancerResearchGroup", 
		"CancerStage", "CancerTNMFinding", "Capacity", "CarbonNanotube", 
		"CarbonNanotubeComposition", "CarcinogenExposure", "CarcinogenicIntervention", 
		"CarcinomaInSituStatus", "Cardinality", "Cart", "CartObject", "caseDetail", 
		"CaseReportForm", "Caspase3Activation", "CatalystActivity", "Category", 
		"CategoryObservation", "CategorySummaryExport", "CategorySummaryReportRow", 
		"CD", "CDCV", "CellLine", "CellLysateFinding", "cells", "CellSpecimen", 
		"CellSpecimenRequirement", "CellSpecimenReviewParameters", "CellViability", 
		"Center", "CenterForEpidemiologicStudiesDepressionScale", "CenterPlatform", 
		"CentralLaboratory", "CEQ", "CFU_GM", "cghSamples", "Chain", "Change", 
		"ChangedGenes", "changeType", "Channel", "Characteristic", "Characteristics", 
		"Characterization", "CharacterizationProtocol", "CharacterParameter", 
		"CheckInCheckOutEventParameter", "ChemicalAssociation", 
		"ChemicalCharacteristics", "ChemicalClass", "ChemicalStressor", 
		"ChemicalStressorProtocol", "ChemicalTreatment", "ChemoRXAnnotation", 
		"Chemotaxis", "Chemotherapy", "ChemotherapyData", "ChimerismSample", 
		"ChromatogramPoint", "ChromatographyAnalysis", "ChromosomalSegment", 
		"ChromosomalSegmentOverlapParameters", "ChromosomalSegmentWithMean", 
		"ChromosomalSegmentWithMeanAndMarker", "Chromosome", "ChromosomeMap", 
		"ChronicGraftVersusHostDisease", "Chunk", "Circle", "citation", "Class1", 
		"ClassArtefact", "ClassComparisonAnalysis", "ClassComparisonAnalysisFinding", 
		"Classification", "ClassificationParameters", "ClassificationScheme", 
		"ClassificationSchemeItem", "ClassificationSchemeItemRelationship", 
		"ClassificationSchemeRelationship", "ClassMembership", 
		"ClassSchemeClassSchemeItem", "CLCY", "ClinicalAssessment", "ClinicalFinding", 
		"ClinicalInterpretation", "ClinicalMarker", "ClinicalObservation", 
		"ClinicalReport", "ClinicalResearchCoordinator", "ClinicalResearchStaff", 
		"ClinicalResult", "ClinicalTrial", "ClinicalTrialProtocol", 
		"ClinicalTrialSite", "ClinicalTrialSponsor", "ClinicalTrialSubject", "Clone", 
		"CloneClass", "CloneRelativeLocation", "Cluster", "CnatOutput", 
		"CnatPairedAnalysisInput", "CnatPairedAnalysisProbesetOutput", 
		"CnatParameters", "CnatUnpairedAnalysisInput", 
		"CnatUnpairedAnalysisProbesetOutput", "CnaValue", 
		"CNSAccessionCharacteristics", "CNSCarcinoma", "CNSHistologicGrade", 
		"CNSMargin", "CNSMarginLocation", "CNSNeoplasmHistologicType", 
		"CNSPathologyAnnotation", "CNSSpecimenCharacteristics", 
		"CNSSpecimenPathologyAnnotation", "CO", "Coagulation", "Code", 
		"CodedNodeGraph", "CodedNodeSet", "CodeExistence", "CodeRelationship", 
		"CodeSequence", "CodeState", "CodingScheme", "CodingSchemeCopyright", 
		"CodingSchemeIdentification", "CodingSchemeRendering", 
		"CodingSchemeRenderingList", "CodingSchemeSummary", "CodingSchemeSummaryList", 
		"CodingSchemeTagList", "CodingSchemeVersion", "CodingSchemeVersionList", 
		"CodingSchemeVersionOrTag", "CodingSchemeVersionStatus", "Coefficients", 
		"Cohort", "CohortObservation", "CoiledCoil", "CoiledCoilRegion", "Coils", 
		"COLL", "Collaboration", "CollaborativeStaging", "CollectionEventParameters", 
		"CollectionProtocol", "CollectionProtocolEvent", 
		"CollectionProtocolRegistration", "CollisionCell", "ColorDecompositionGeneral", 
		"ColorectalAccessionCharacteristics", "ColorectalCancerTNMFinding", 
		"ColorectalHistologicGrade", "ColorectalLocalExcisionMarginUninvolved", 
		"ColorectalPathologyAnnotation", "ColorectalResectedMarginUninvolved", 
		"ColorectalSpecimenCharacteristics", "ColorectalSpecimenPathologyAnnotation", 
		"CometScores", "Comment", "Comments", "CommonDataElement", "CommonLookup", 
		"CommunicationMechanism", "CommunicationStyle", "Comorbidity", "COMP", 
		"ComparativeMarkerSelectionParameterSet", 
		"ComparativeMarkerSelectionResultCollection", "ComplementActivation", 
		"Complex", "ComplexComponent", "ComplexComposition", "ComplexEntity", 
		"ComponentConcept", "ComponentLevel", "ComponentName", "ComposingElement", 
		"CompositeCompositeMap", "CompositeElement", "CompositeGroup", 
		"CompositePosition", "CompositeSequence", "CompositeSequenceDimension", 
		"CompositeSequenceSummary", "CompositionallyBiasedRegion", "Compound", 
		"CompoundMeasurement", "ConcentrationUnit", "Concept", "ConceptClassification", 
		"ConceptCodes", "ConceptDerivationRule", "ConceptDescriptor", 
		"ConceptIdentification", "ConceptProperty", "ConceptReference", 
		"ConceptReferenceList", "ConceptReferent", "ConceptReferentClassification", 
		"Concepts", "ConceptualDomain", "ConcomitantAgent", "ConcomitantMedication", 
		"ConcomitantMedicationAttribution", "ConcomitantMedicationDetail", 
		"ConcomitantProcedure", "ConcomitantProcedureDetail", "Condition", 
		"Conditional", "Conditionality", "ConditionGroup", "ConditionMessage", 
		"ConfidenceIndicator", "ConsensusClusteringParameterSet", 
		"ConsensusClusterResultCollection", "ConsensusIdentifierData", 
		"ConsensusMatrix", "ConsensusMatrixRow", "ConsentTier", "ConsentTierResponse", 
		"ConsentTierStatus", "ConstrainedRegion", "Constraint", "Contact", 
		"ContactCommunication", "ContactDetails", "ContactInfo", "ContactInformation", 
		"ContactMechanismBasedRecipient", "ContactMechanismType", "ContactPerson", 
		"Container", "ContainerInfo", "ContainerPosition", "ContainerType", "Context", 
		"ContextProperty", "Contour", "Control", "ControlGenes", 
		"ControlledVocabularyAnnotation", "Coordinate", "CopyNumberFinding", 
		"CopyNumberOutput", "CopyNumberOutputForSample", "CopyNumberStateAtProbe", 
		"CorrelationType", "CourseAgent", "CourseAgentAttribution", "CourseDate", 
		"CreatedInfo", "Credentials", "CrossLink", "CS", "Ctc", "CtcCategory", 
		"CtcGrade", "CtcTerm", "CtepStudyDisease", "Culture", "CultureProtocol", 
		"CurationData", "CurrentQuantity", "CustomProperties", "CutaneousMelanoma", 
		"CutaneousMelanomaAccessionCharacteristics", 
		"CutaneousMelanomaAdditionalFindings", 
		"CutaneousMelanomaNegativeSurgicalMargin", 
		"CutaneousMelanomaNeoplasmHistologicType", 
		"CutaneousMelanomaPositiveSurgicalMargin", 
		"CutaneousMelanomaSpecimenCharacteristics", 
		"CutaneousMelanomaSurgicalPathologySpecimen", "CutaneousMelanomaTNMFinding", 
		"CV", "Cycle", "Cytoband", "CytobandPhysicalLocation", "CytogeneticLocation", 
		"Cytogenetics", "CytokineInduction", "Cytotoxicity", "Data", "Database", 
		"DatabaseCrossReference", "DatabaseEntry", "DatabaseSearch", 
		"DatabaseSearchParameters", "DatabaseSearchParametersOntologyEntry", 
		"DataClassification", "DataCollectionInstrument", "DataCollectionModule", 
		"DataCollectionProvenance", "DataElement", "DataElementConcept", 
		"DataElementConceptRelationship", "DataElementDerivation", 
		"DataElementRelationship", "DataFile", "DataFileLimsFile", "DataItem", 
		"DataRetrievalRequest", "DataServiceInstance", "DataSet", 
		"DataSetGeneticElement", "DataSetRequest", "DataSetSample", "DataSetVersion", 
		"DataSource", "DataStatus", "DataType", "DataValue", "DataVersion", "Datum", 
		"dbxref", "DeathSummary", "DeepMelanomaMargin", "DefinedActivity", 
		"DefinedActivityStudySegmentRule", "DefinedAdministrativeActivity", 
		"DefinedArm", "DefinedEpoch", "DefinedObservation", "DefinedParameter", 
		"DefinedProcedure", "DefinedSpecimenCollection", "DefinedStudyAgentTransfer", 
		"DefinedStudyCell", "DefinedStudySegment", "DefinedStudySegmentStudyCellRule", 
		"DefinedSubstanceAdministration", "Definition", "DefinitionClassSchemeItem", 
		"DeidentifiedSurgicalPathologyReport", 
		"DeletedOverlappedChromosomalSegmentWithSample", 
		"DeletedOverlappedChromosomalSegmentWithSource", "DeliveryStatus", "Delta", 
		"Demographic", "DEMOGRAPHIC", "Demographics", "Dendrimer", 
		"DendrimerComposition", "Department", "DerivationType", "DerivedArrayData", 
		"DerivedBioAssay", "DerivedBioAssayData", "DerivedBioAssays", 
		"DerivedDataElement", "DerivedDataFile", "DerivedDatum", 
		"DerivedDNACopySegment", "DerivedSignal", "DerivedSpecimenOrderItem", 
		"DescLogicConcept", "DescLogicConceptVocabularyName", "Describable", 
		"Description", "Designation", "DesignationClassSchemeItem", "DesignElement", 
		"DesignElementDimension", "DesignElementGroup", "DesignElementList", 
		"DesignElementMap", "DesignElementMapping", "Details", "Detection", 
		"DevelopmentalStage", "DeviceAttribution", "DeviceOperator", "Diagnosis", 
		"DICOMImageReference", "DiffFoldChangeFinding", "Dige", "DigeGel", 
		"DigeSpotDatabaseSearch", "DigeSpotMassSpec", "Dimension", 
		"DirectedAcyclicGraph", "DirectExtensionOfTumor", "Direction", 
		"DirectionalAssociationIdentification", "Disease", "DiseaseAttribution", 
		"DiseaseCategory", "DiseaseEvaluation", "DiseaseEvaluationDetail", 
		"DiseaseExtent", "DiseaseHistory", "DiseaseModel", "DiseaseOntology", 
		"DiseaseOntologyRelationship", "DiseaseOutcome", "DiseaseResponse", 
		"DiseaseTerm", "DiseaseTerminology", "DisposalEventParameters", 
		"DistanceFromAnalVerge", "DistanceOfAdenoma", "DistanceOfInvasiveCarcinoma", 
		"DistanceUnit", "DistantMetastasis", 
		"DistantRecurrenceHealthExaminationAnnotation", "DistantSite", 
		"DistributedItem", "Distribution", "DistributionProtocol", 
		"DistributionProtocolAssignment", "DistributionProtocolOrganization", 
		"DistributionSpecimenRequirement", "DisulfideBond", "DMRService", "DNA", 
		"DnaAnalyte", "DNABindingRegion", "DNAcopyAssays", "DnaCopyInput", 
		"DnaCopyOutput", "DNAcopyParameter", "DnaCopyParameters", "DnaCopySample", 
		"DNASpecimen", "Document", "Documentation", "DocumentAuthor", 
		"DocumentContent", "DocumentFrame", "DocumentRelationship", 
		"DocumentStructure", "DocumentStructureRelationship", "Domain", 
		"DomainDescriptor", "DomainModel", "DomainName", "Donor", "DonorBlock", 
		"DonorInfo", "DonorRecipient", "Dose", "DoubleArrayComponent", "DoubleColumn", 
		"DoubleParameter", "DQSET", "Drug", "DrugSurgeryData", "DSET", "Duration", 
		"ED", "EDDOC", "EDDOCINLINE", "EDDOCREF", "EdgeProperties", "EDIMAGE", 
		"EditActionDate", "EDSIGNATURE", "EDSTRUCTUREDTEXT", "EDSTRUCTUREDTITLE", 
		"EDTEXT", "EIVL", "Electrospray", "Eligibility", "Ellipse", 
		"EmbeddedEventParameters", "Emulsion", "EmulsionComposition", "EN", 
		"Encapsulation", "Encounter", "Endpoint", "EndpointCode", "EndpointReference", 
		"EngineeredGene", "ENON", "ENPN", "EnsemblGene", "EnsemblPeptide", 
		"EnsemblTranscript", "Entity", "EntityAccession", "EntityDescription", 
		"EntityMap", "EntityName", "EntityVersion", "ENTN", "Entrapment", "EntrezGene", 
		"Entry", "EntryCategory", "EntryType", "EnucleationInvasiveProstateCarcinoma", 
		"EnucleationProstateSpecimenCharacteristics", 
		"EnucleationProstateSurgicalPathologySpecimen", "EnumeratedValueDomain", 
		"Enumeration", "EnvironmentalCondition", 
		"EnvironmentalExposuresHealthAnnotation", "EnvironmentalFactor", "ENXP", 
		"EnzymeInduction", "Epoch", "EpochDelta", "Equipment", "Error", "ErrorDetails", 
		"ErrorEstimatorMethod", "Evaluation", "Evaluator", "Event", "EventEntity", 
		"EventEntitySet", "EventParameters", "EventRecords", "Evidence", 
		"EvidenceCode", "EvidenceKind", "EVSDescLogicConceptSearchParams", 
		"EVSHistoryRecordsSearchParams", "EVSMetaThesaurusSearchParams", 
		"EVSSourceSearchParams", "Examination", "ExampleSearchCriteria", 
		"ExcionalBiopsyMarginUninvolved", 
		"ExcisionalBiopsyBasedColorectalPathologyAnnotation", 
		"ExcisionalBiopsyColorectalDeepMargin", 
		"ExcisionalBiopsyColorectalLateralOrMucosalMargin", 
		"ExcisionCutaneousMelanomaSpecimenCharacteristics", 
		"ExcisionCutaneousMelanomaSurgicalPathologySpecimen", "Execution", 
		"ExistingSpecimenArrayOrderItem", "ExistingSpecimenOrderItem", 
		"ExocrinePancreasAccessionCharacteristics", 
		"ExocrinePancreasSpecimenCharacteristics", 
		"ExocrinePancreaticCancerTNMFinding", "Exon", "ExonArrayReporter", 
		"ExonProbeAnnotation", "ExpectedValue", "ExpeditedAdverseEventReport", 
		"Experiment", "Experiment2DGelList", "Experiment2DLiquidChromatography", 
		"Experiment2DLiquidChromatography1stSetup", 
		"Experiment2DLiquidChromatography2ndSetup", "ExperimentalContact", 
		"ExperimentalData", "ExperimentalFactor", "ExperimentalFeatures", 
		"ExperimentalSampleControlSamplePair", "ExperimentalStructure", 
		"ExperimentConfig", "ExperimentContact", "ExperimentDesign", "ExperimentEvent", 
		"ExperimentObservation", "ExperimentRun", "ExperimentSearchCriteria", 
		"ExperimentTo2DGel", "ExperimentToDatabaseSearch", "ExperimentToDige", 
		"ExperimentToMassSpec", "Exponent", "ExportStatus", "EXPR", 
		"ExpressedSequenceTag", "ExpressionArrayReporter", "ExpressionData", 
		"ExpressionFeature", "ExpressionGeneValue", "ExpressionLevelDesc", 
		"ExpressionProbeAnnotation", "ExpressoParameter", "Extendable", 
		"ExtendingClass", "ExtensionDescription", "ExtensionDescriptionList", 
		"ExtensionIdentification", "ExternalIdentifier", "ExternalReference", 
		"Extract", "ExtraprostaticExtension", "ExtraprostaticExtensionTissueSites", 
		"Facility", "Factor", "FactorValue", "Failed", "FamilyHistory", 
		"FamilyHistoryAnnotation", "FamilyMember", "FastaFiles", "FastaSequences", 
		"Fault", "Feature", "FeatureData", "FeatureDefect", "FeatureDimension", 
		"FeatureExtraction", "FeatureGroup", "FeatureInformation", "FeatureLocation", 
		"FeatureReporterMap", "FeatureType", "FemaleReproductiveCharacteristic", 
		"Ffas", "Fiducial", "File", "FileContents", "FileExtension", 
		"FileFormatSpecification", "FileMetadata", "FileSearchCriteria", 
		"FileTransporter", "FileType", "FillPattern", "Filters", "Finding", 
		"FirstCourseRadiation", "FirstCourseTreatmentSummary", "FISHFinding", 
		"FixedEventParameters", "FloatColumn", "FloatingPointQuantity", 
		"FloatParameter", "FloatRow", "FluidSpecimen", "FluidSpecimenRequirement", 
		"FluidSpecimenReviewEventParameters", "Fold", "Folder", "Followup", "Form", 
		"FormatType", "FormElement", "Fraction", "FractionAnalyteSteps", "Fractions", 
		"FrozenEventParameters", "FuhrmanNuclearGrade", "Fullerene", 
		"FullereneComposition", "Function", "functionalCategory", 
		"FunctionalDNADomain", "FunctionalizingEntity", "FunctionalProteinDomain", 
		"FunctionalRole", "Funding", "FundingSource", "GbmDrugs", "GbmPathology", 
		"GbmSlide", "GbmSurgery", "Gel", "Gel2d", "GelElectrophoresisAnalysis", 
		"GelImage", "GelImageType", "GelPlug", "GelSpot", "GelSpotList", "GelStatus", 
		"Genbank", "GenBankAccession", "GenBankmRNA", "GenBankProtein", "Gene", 
		"gene_product", "GeneAgentAssociation", "GeneAlias", "GeneAnnotation", 
		"GeneBiomarker", "GeneCalculations", "GeneCategoryExport", 
		"GeneCategoryMatrix", "GeneCategoryMatrixRow", "GeneCategoryReportRow", 
		"GeneCytogeneticLocation", "GeneDelivery", "GeneDiseaseAssociation", 
		"GeneExprReporter", "GeneFunction", "GeneFunctionAssociation", 
		"GeneGenomicIdentifier", "GeneIdentifier", "GeneNeighborsParameterSet", 
		"GeneNeighborsService", "GeneOntology", "GeneOntologyRelationship", 
		"GenePatternCopyNumberAnalysisOutput", 
		"GenePatternCopyNumberAnalysisProbesetOutput", "GenePatternCopyNumberInput", 
		"GenePatternCopyNumberParameters", "GenePhysicalLocation", "GenePubmedSummary", 
		"GeneralHealthDiagnosis", "GeneRank", "GeneRankAnalysis", "GeneRankParameters", 
		"GeneRankResults", "GeneRegulation", "GeneRelativeLocation", 
		"GeneReporterAnnotation", "GenericArray", "GenericFeature", 
		"GenericImageFeatureSet", "GenericReporter", "GeneticAlteration", 
		"GeneticElement", "GeneticElementType", "GeneVersion", 
		"GenomeAnnotationInformation", "GenomeAssayProject", "GenomeCharacterization", 
		"GenomeEncodedEntity", "GenomicIdentifier", "GenomicIdentifierSet", 
		"GenomicIdentifierSolution", "GenomicSegment", "Genotype", "GenotypeDiagnosis", 
		"GenotypeFinding", "GenotypeSummary", "Genus", "GeometricShape", 
		"GisticResult", "GisticService", "GleasonHistologicGrade", 
		"GleasonHistopathologicGrade", "GleasonScore", "GLIST", 
		"GlobalGenomicAssayData", "GlycosylationSite", "GominerGene", "GominerTerm", 
		"GOTerm", "Grade", "GradientStep", "Graft", "GraftVersusHostDisease", 
		"GraftVersusHostDiseaseOutcome", "GraphResolutionPolicy", "Group", 
		"GroupRoleContext", "GSISecureConversation", "GSISecureMessage", 
		"GSITransport", "GTS", "GTSBOUNDEDPIVL", "Hap2Allele", "Haplotype", "Hardware", 
		"HardwareApplication", "Hazard", "header", "HealthCareProvider", 
		"HealthCareSite", "HealthcareSite", "HealthcareSiteInvestigator", 
		"HealthcareSiteParticipant", "HealthcareSiteParticipantRole", 
		"HealthExaminationAnnotation", "HealthGoals", "HEALTHGOALS", "Helix", 
		"HematologyChemistry", "Hemolysis", "HemTransplantEndocrineProcedure", 
		"Hexapole", "HierarchicalCluster", "HierarchicalClusteringParameter", 
		"HierarchicalClusterNode", "HierarchyIdentification", 
		"HierarchyPathResolveOption", "HierarchyResolutionPolicy", 
		"HighLevelGroupTerm", "HighLevelTerm", "HINTS", "HIST", "HistologicGrade", 
		"HistologicType", "HistologicVariantType", "Histology", "HistopathologicGrade", 
		"Histopathology", "HistopathologyGrade", "History", "HistoryRecord", "Hmmpfam", 
		"HomologAlignment", "HomologousAssociation", "HormoneTherapy", "HRVarType", 
		"HXIT", "Hybridization", "HybridizationData", "HybridizationDataGroup", 
		"HybridizationReference", "HybridizationSearchCriteria", "Hypothesis", 
		"Identifiable", "IdentificationScheme", "IdentifiedPathologyReport", 
		"IdentifiedPatient", "IdentifiedSection", "IdentifiedSurgicalPathologyReport", 
		"Identifier", "IHCFinding", "II", "Image", "ImageAcquisition", 
		"ImageAnnotation", "ImageContrastAgent", "ImageDataItem", "ImageProcessing", 
		"ImageProcessingOutput", "ImageQuery", "ImageQueryOutput", "ImageReference", 
		"ImageSeries", "ImageServer", "ImageSet", "ImageStudy", "ImageType", 
		"ImageView", "ImageViewModifier", "Imaging", "ImagingFunction", 
		"ImagingObservation", "ImagingObservationCharacteristic", "ImagingSession", 
		"ImagingSystem", "ImmuneCellFunction", "Immunologic", "ImmunoPhenotyping", 
		"Immunotherapy", "Immunotoxicity", "ImmunoToxicity", "INDHolder", "Individual", 
		"InducedMutation", "InitiatorMethionine", "Input", "InputFile", 
		"InputOutputObject", "InputParameter", "Instance", "InstanceProperty", 
		"Instances", "Institution", "Instruction", "Instrument", 
		"InstrumentConfiguration", "InstrumentType", "INT", "Integer", "IntegerColumn", 
		"IntegerParameter", "IntegerQuantity", "IntegrationType", "Interaction", 
		"InternationalDesignation", "InternetSource", "InterProScanInput", 
		"InterProScanInputParameters", "InterProScanJob", "InterProScanOutput", 
		"InterProScanService", "INTNONNEG", "INTPOS", "Intron", "Invasion", 
		"InvasiveBreastCarcinoma", "InvasiveBreastCarcinomaNeoplasmHistologicType", 
		"InvasiveColorectalCarcinoma", 
		"InvasiveColorectalCarcinomaNeoplasmHistologicType", 
		"InvasiveExocrinePancreaticCarcinoma", 
		"InvasiveExocrinePancreaticCarcinomaNeoplasmHistologicType", 
		"InvasiveKidneyCarcinoma", "InvasiveKidneyCarcinomaNeoplasmHistologicType", 
		"InvasiveLungCarcinoma", "InvasiveLungCarcinomaNeoplasmHistologicType", 
		"InvasiveProstateCarcinoma", "InvasiveProstateCarcinomaNeoplasmHistologicType", 
		"Investigation", "InvestigationalNewDrug", "Investigator", 
		"InvestigatorHeldIND", "InvitroCharacterization", "InvivoCharacterization", 
		"InvivoResult", "Invoker", "IonizationSource", "IonSource", "IonTrap", "IVL", 
		"IVL(PQ)", "IVLHIGH", "IVLLOW", "IVLTS", "IVLWIDTH", "JaxInfo", "Job", "JobId", 
		"JpegImage", "Jpred", "Keyword", "KeywordSearchCriteria", 
		"KidneyAccessionCharacteristics", "KidneyAdditionalFindings", 
		"KidneyBiopsyBasedPathologyAnnotation", "KidneyCancerTNMFinding", 
		"KidneyMarginLocation", "KidneyNephrectomyBasedPathologyAnnotation", 
		"KidneyNephrectomyMargin", "KidneyPathologyAnnotation", 
		"KidneySpecimenCharacteristics", "KidneySpecimenPathologyAnnotation", 
		"KNearestNeighborsService", "Lab", "LabAnnotation", "LabeledExtract", 
		"LabFile", "LabGeneral", "LabGroup", "LabMember", "Laboratory", 
		"LaboratoryEquipment", "LaboratoryFinding", "LaboratoryPersonnel", 
		"LaboratoryProject", "LaboratoryResult", "LaboratorySamplePlate", 
		"LaboratoryStorageDevice", "LaboratoryTest", "LabSpecial", "LabValue", 
		"Lambda", "LateralMelanomaMargin", "Layout", "LesionDescription", 
		"LesionEvaluation", "LeukocyteProliferation", "LevelOfExpressionIHCFinding", 
		"LexBIGService", "LexBIGServiceConvenienceMethods", "LexBIGServiceMetadata", 
		"Library", "LimitOffset", "LimsFile", "Lineage", "Linkage", "LinkType", 
		"LipidMoietyBindingRegion", "Liposome", "LiposomeComposition", 
		"LiquidChromatographyColumn", "List", "LIST", "ListProcessing", 
		"LiteratureRelationship", "LoadStatus", 
		"LocalExcisionBasedColorectalPathologyAnnotation", 
		"LocalExcisionColorectalDeepMargin", "LocalExcisionColorectalLateralMargin", 
		"LocalNameList", "LocalRecurrenceHealthExaminationAnnotation", "Location", 
		"Log", "LogEntry", "LogicalProbe", "LogLevel", "LOHFinding", "LongColumn", 
		"LongParameter", "LongTermFU", "LossOfExpressionIHCFinding", 
		"LowComplexityRegion", "LowLevelTerm", "Lsid", "LungAccessionCharacteristics", 
		"LungBiopsyPathologyAnnotation", "LungCancerTNMFinding", "LungDrugs", 
		"LungExam", "LungNeoplasm", "LungPathology", "LungPathologyAnnotation", 
		"LungResectionBasedPathologyAnnotation", "LungResectionMargin", 
		"LungResectionMarginsUninvolved", "LungSlide", "LungSpecimenCharacteristics", 
		"LungSpecimenPathologyAnnotation", "LungSurgery", "Macroprocess", 
		"MacroscopicExtentOfTumor", "MAGE", "MageTabFileSet", "Maldi", 
		"ManufacturedMaterial", "ManufactureLIMS", "ManufactureLIMSBiomaterial", 
		"Manufacturer", "Map", "Mapping", "Mappings", "Marker", "MarkerAlias", 
		"MarkerPhysicalLocation", "MarkerRelativeLocation", "MarkerResult", 
		"MarketingAuthorization", 
		"MarketingAuthorizationHolderManufacturerDistributor", "MascotScores", 
		"MassQuery", "MassSpecDatabaseSearch", "MassSpecExperiment", "MassSpecMachine", 
		"MassSpecMassSpecFraction", "MassSpectrometer", "MassSpectrometryAnalysis", 
		"MassSpectrometryAnalysisFragmentationMethod", "MassUnit", "MatchCriteria", 
		"MatchingParameters", "Material", "MaterialSource", "MaterialType", "MathFile", 
		"Matrix", "MeasuredBioAssay", "MeasuredBioAssayData", "MeasuredBioAssays", 
		"MeasuredSignal", "Measurement", "MeasurementCharacteristic", 
		"MeasurementFactorValue", "MeasurementParameterValue", "MeasurementProtocol", 
		"MeasurementValue", "MeasureUnit", "Meddra", "MeddraStudyDisease", 
		"MedicalDevice", "MedicalHistory", "MedicinalProduct", 
		"MelanomaPathologyAnnotation", "MelanomaSpecimenPathologyAnnotation", 
		"Message", "MessagePayload", "MessengerRNA", "MessengerRNAGenomicIdentifier", 
		"MetabolicStability", "Metadata", "MetadataProperty", "MetadataPropertyList", 
		"MetalIonBindingSite", "MetalParticle", "MetalParticleComposition", 
		"MetastasisSite", "MetastasisTissueSite", "MetastaticDiseaseSite", 
		"MetaThesaurusConcept", "Method", "MethodParameter", "Methylation", 
		"MethylationDnaSequence", "MethylationSite", "MethylationSiteMeasurement", 
		"Microarray", "MicroarrayArtifactDetectParameters", 
		"MicroarrayArtifactDetectResult", "MicroArrayData", "MicroarrayEventRecords", 
		"MicroarrayGeneCalculationsParameters", "MicroarrayGeneCalculationsResult", 
		"MicroarrayObservation", "MicroarrayQualityScoreParameters", 
		"MicroarrayQualityScoreResult", "MicroarraySet", 
		"MicroarrayVariationHeatmapParameters", "MicroarrayVariationHeatmapResult", 
		"Microcalcification", "MinimumReserveQuantity", "MismatchInformation", 
		"Missed", "MO", "MobilePhaseComponent", "Model", "Modeller", "ModelSection", 
		"ModelStructure", "Modifications", "ModificationType", "ModifiedResidue", 
		"Module", "ModuleDescription", "ModuleDescriptionList", 
		"MolecularSeparationProcess", "MolecularSequenceDatabase", 
		"MolecularSequenceRepresentation", "MolecularSpecimen", 
		"MolecularSpecimenRequirement", "MolecularSpecimenReviewParameters", 
		"MolecularWeight", "Molecule", "Morphology", "Mouse", "Movie", 
		"mRNAGenomicIdentifier", "MS2Runs", "Msi", "MultipleAlignment", "MultiPoint", 
		"MultiProcessParameters", "MutagenesisSite", "MutationIdentifier", 
		"MutationVariation", "MZAnalysis", "MzAssays", "MzSpectrum", "MZXMLSubFiles", 
		"Name", "NameAndValue", "NameAndValueList", "NameValueType", 
		"NamingConvention", "NanomaterialEntity", "Nanoparticle", 
		"NanoparticleDatabaseElement", "NanoparticleEntity", "NanoparticleSample", 
		"NanoparticleStudy", "NCBIBlastInput", "NCBIBlastInputParameters", 
		"NCBIBlastJob", "NCBIBlastOutput", "NCBIBlastService", "NCIChangeEvent", 
		"NCIChangeEventList", "NeedleBiopsyInvasiveProstateCarcinoma", 
		"NeedleBiopsyProstatePathologyAnnotation", 
		"NeedleBiopsyProstateSpecimenCharacteristics", 
		"NeedleBiopsyProstateSurgicalPathologySpecimen", "NegativeControl", "Neoplasm", 
		"NeoplasmHistologicType", "NeoplasmHistopathologicType", 
		"NewDiagnosisHealthAnnotation", "NewSpecimenArrayOrderItem", 
		"NewSpecimenOrderItem", "NKCellCytotoxicActivity", "Node", "NodeContents", 
		"NodeListPolicy", "NodeValue", "NoEvidentDiseaseHealthAnnotation", "Noise", 
		"Nomenclature", "NonCancerDirectedSurgery", "NonConsecutiveResidues", "None", 
		"NonenumeratedValueDomain", "NonTerminalResidue", "NonTherapeuticAgent", 
		"NonverifiedSamples", "NormalChromosome", "NormalizeInvariantSetParameter", 
		"NormalizeMethodParameter", "NormalizeQuantilesRobustParameter", 
		"NotApplicable", "Notes", "Notification", "NotificationBodyContent", 
		"NottinghamHistologicGrade", "NottinghamHistologicScore", 
		"NottinghamHistopathologicGrade", "NPPD", "NucleicAcid", 
		"NucleicAcidPhysicalLocation", "NucleicAcidSequence", 
		"NucleicAcidSequenceDatabase", "NucleicAcidSequenceRepresentation", 
		"NucleotideBindingRegion", "NucleotidePhosphateBindingRegion", "Null", 
		"NumericalRangeConstraint", "NumericMeasurement", "ObjectClass", 
		"ObjectClassRelationship", "Observation", "ObservationConcept", 
		"ObservationData", "ObservationProtocol", "ObservationRelationship", 
		"ObservationResult", "ObservationResultAssessmentRelationship", 
		"ObservationResultRelationship", "ObservationState", "ObservedPeptide", 
		"ObservedThing", "ObservedThingToObservationConnection", 
		"ObservedThingToObservedThingRelationship", 
		"ObservedThingToOntologyElementRelationship", "Occurred", "OctaveFile", 
		"Octet", "OddsRatio", "OffTreatment", "OID", "OMIM", "ON", "OnStudy", 
		"OntologyDimension", "OntologyElement", "OntologyElementSlot", 
		"OntologyElementSlotSet", "OntologyElementToOntologyElementRelationship", 
		"OntologyEntry", "OntologyGroup", "OntologyGroupToOntologyGroupRelationship", 
		"Operation", "Order", "OrderDetails", "OrderItem", "OrderOfNodeTraversal", 
		"OrderSet", "Organ", "Organelle", "Organism", "OrganismName", "OrganismStrain", 
		"Organization", "OrganizationAssignedIdentifier", "OrganizationHeldIND", 
		"OrganOntology", "OrganOntologyRelationship", "OrthologousGene", 
		"OtherAnalyte", "OtherAnalyteAnalyteProcessingSteps", 
		"OtherAnalyteOntologyEntry", "OtherAnalyteProcessingSteps", 
		"OtherAnalyteProcessingStepsOntologyEntry", "OtherBreastCancerHistologicGrade", 
		"OtherBreastCancerHistopathologicGrade", "OtherCause", "OtherCauseAttribution", 
		"OtherCharacterization", "OtherChemicalAssociation", "OtherFunction", 
		"OtherFunctionalizingEntity", "OtherIonisation", 
		"OtherIonisationOntologyEntry", "OtherMZAnalysis", 
		"OtherMZAnalysisOntologyEntry", "OtherNanomaterialEntity", 
		"OtherNanoparticleEntity", "OtherProcedure", "OtherResectedOrgans", 
		"OtherTarget", "OtherTherapy", "Outcome", "OutcomeType", "Output", 
		"OutputFile", "OutputImage", "OvarianDrugs", "OvarianExam", "OvarianNeoplasm", 
		"OvarianPathology", "OvarianSlide", "OvarianSurgery", 
		"OverlapFinderAcrossSamplesInput", "OverlapFinderAcrossSamplesOutput", 
		"OverlapFinderAcrossSourcesInput", "OverlapFinderAcrossSourcesOutput", 
		"OverlappedChromosomalSegmentWithSample", 
		"OverlappedChromosomalSegmentWithSource", "OxidativeBurst", "OxidativeStress", 
		"Package", "PancreasMargin", "PancreasMarginInvolvedByInvasiveCarcinoma", 
		"PancreasMarginUninvolvedByInvasiveCarcinoma", "PancreasPathologyAnnotation", 
		"PancreasSpecimenPathologyAnnotation", "PanelType", "Paper", "Parameter", 
		"Parameterizable", "ParameterizableApplication", "ParameterList", "Parameters", 
		"ParameterSet", "ParameterValue", "Participant", 
		"ParticipantEligibilityAnswer", "ParticipantHistory", 
		"ParticipantMedicalIdentifier", "Participation", "ParticleComposition", 
		"Party", "PartyRole", "Password", "PastCancerTreatmentHistory", 
		"PathologicalCaseOrderItem", "PathologicalStaging", "Pathology", 
		"PathologyEventRecords", "PathologyObservation", "PathologyReport", 
		"PathologyReportReviewParameter", "Pathway", "PathwayReference", "Patient", 
		"PatientIdentifier", "PatientLink", "PatientVisit", "PCAResult", "PCAService", 
		"PCRProduct", "Pdb", "Pdbblast", "Peak", "PeakDetectionParameters", "PeakList", 
		"PeakLocation", "PeakSpecificChromint", "Pedigree", "PedigreeGraph", 
		"PedigreeNode", "Peptide", "PeptideAtlasBuild", "PeptideHit", 
		"PeptideHitModifications", "PeptideHitOntologyEntry", "PeptideHitProteinHit", 
		"PeptideMapping", "PeptideMembers", "PeptidesBase", "Percentile", "PercentX", 
		"PerformedActivity", "PerformedActivityStudySegmentRule", 
		"PerformedAdministrativeActivity", "PerformedArm", "PerformedArmSoA", 
		"PerformedClinicalResult", "PerformedEpoch", "PerformedEpochRule", 
		"PerformedMedicalHistoryResult", "PerformedNonSubjectAdministrativeActivity", 
		"PerformedObservation", "PerformedObservationResult", "PerformedProcedure", 
		"PerformedSoACell", "PerformedSpecimenCollection", "PerformedStudy", 
		"PerformedStudyAgentTransfer", "PerformedStudyCell", "PerformedStudySegment", 
		"PerformedStudySegmentStudyCellRule", "PerformedStudySoA", 
		"PerformedSubjectMilestone", "PerformedSubjectStudyEncounter", 
		"PerformedSubjectStudyEncounterRule", "PerformedSubstanceAdministration", 
		"PerformingLaboratory", "PerformingParty", "Period", "PeriodDelta", 
		"PermissibleValue", "Person", "person", "PersonContact", "PersonName", 
		"Personnel", "PersonOccupation", "PETEvaluation", "PETEvaluationDetail", 
		"Phagocytosis", "PharmaceuticalOrder", "PharmaceuticalProduct", 
		"Pharmacokinetics", "Phase", "Phenomenon", "PhenomenonType", "Phenotype", 
		"PhenotypeDiagnosis", "PhenotypeStatus", "PhysicalArrayDesign", 
		"PhysicalBioAssay", "PhysicalCharacteristics", "PhysicalCharacterization", 
		"PhysicalEntity", "PhysicalExam", "PhysicalLocation", "PhysicalParticipant", 
		"PhysicalPosition", "PhysicalProbe", "PhysicalState", "Physician", 
		"PhysicoChemicalCharacterization", "PIVL", "PKDataSheet", "Place", 
		"PlannedActivity", "PlannedActivityDelta", "PlannedActivityStudySegmentRule", 
		"PlannedAdministrativeActivity", "PlannedArm", "PlannedArmSoA", 
		"PlannedCalendar", "PlannedCalendarDelta", "PlannedEmailNotification", 
		"PlannedEpoch", "PlannedEpochRule", "PlannedNotification", 
		"PlannedObservation", "PlannedObservationResult", "PlannedProcedure", 
		"PlannedSoACell", "PlannedSpecimenCollection", "PlannedStudy", 
		"PlannedStudyAgentTransfer", "PlannedStudyCell", "PlannedStudySegment", 
		"PlannedStudySegmentStudyCellRule", "PlannedStudySoA", 
		"PlannedSubjectStudyEncounter", "PlannedSubjectStudyEncounterRule", 
		"PlannedSubstanceAdministration", "PlasmaProteinBinding", "Plate", "Platelet", 
		"PlateletAggregation", "Platform", "PlatformDataType", "PloidyStruct", "PN", 
		"Point", "PointOfContact", "PolyAlleleFrequency", "PolyGenoFrequency", 
		"Polyline", "Polymer", "PolymerComposition", "Polymorphism", 
		"PolynomialDegree", "PolypConfiguration", "PolypectomySpecimenCharacteristics", 
		"PolypSize", "Population", "PopulationFrequency", "PopulationSample", 
		"Portion", "PortionSlide", "Position", "PositionDelta", "PositiveControl", 
		"PostAdverseEventStatus", "PPD", "PQ", "PQR", "PQTIME", "PQV", 
		"PredictionResult", "PredictionResultSet", "PreExistingCondition", 
		"PreferredTerm", "Prep", "PreprocessDatasetParameterSet", "PresentAbsent", 
		"Presentation", "PresentationThresholds", "PrimaryDiseasePresentation", 
		"PrimarySiteSurgery", "PrimaryTumorStage", "Primer", "PrimerPairs", 
		"PrincipleComponentAnalysis", "PrincipleComponentAnalysisFinding", 
		"PriorTherapy", "PriorTherapyAgent", "Privilege", "ProbabilityMap", "Probe", 
		"ProbeCopyNumberState", "ProbeGroup", "ProbesetsToIgnore", "Procedure", 
		"ProcedureDataFile", "ProcedureDataType", "ProcedureEventParameters", 
		"ProcedureSample", "ProcedureSampleType", "ProcedureType", 
		"ProcedureTypeProtocol", "ProcedureUnit", "Process", "ProcessDataFile", 
		"ProcessDetails", "ProcessEquipment", "ProcessLog", "ProcessLogLimsFile", 
		"PROcessParameter", "ProcessSample", "ProcessState", "ProcessStatus", 
		"ProcessType", "ProductInfused", "Profile", "Project", "Project2Gene", 
		"Project2Sample", "Project2SNP", "ProjectDataFile", "Projection", 
		"ProjectPersonnel", "ProjectProcedure", "ProjectReport", 
		"ProjectReportLimsFile", "ProjectSample", "Promoter", "Propeptide", 
		"Properties", "Property", "PropertyChange", "PropertyDescriptor", 
		"PropertyIdentification", "PropertyLink", "PropertyQualifier", "PropertyType", 
		"PropertyValue", "ProstateAccessionCharacteristics", 
		"ProstateAdditionalFindings", "ProstateCancerTNMFinding", 
		"ProstateMarginLocation", "ProstatePathologyAnnotation", 
		"ProstateSpecimenCharacteristics", "ProstateSpecimenGleasonScore", 
		"ProstateSpecimenPathologyAnnotation", "ProstateSurgicalPathologySpecimen", 
		"ProtectionElement", "ProtectionGroup", "ProtectionGroupRoleContext", 
		"Protein", "Protein2MMDB", "ProteinAlias", "ProteinBiomarker", "ProteinDige", 
		"ProteinDomain", "ProteinDomainIdentifier", "ProteinDomainLocation", 
		"ProteinDomainLocationStatistics", "ProteinDomainMatch", 
		"ProteinEncodingGeneFeature", "ProteinEntity", "ProteinFeature", 
		"ProteinGenomicIdentifier", "ProteinGroupMembers", "ProteinGroups", 
		"ProteinHit", "ProteinHomolog", "ProteinName", "ProteinProphetFiles", 
		"Proteins", "ProteinSequence", "ProteinSequenceDatabase", 
		"ProteinSequenceRepresentation", "ProteinSet", "ProteinSpotSet", 
		"ProteinStructure", "ProteinSubunit", "ProteomeAnalysis", "ProteomicsData", 
		"ProteomicsEventRecords", "ProteomicsObservation", "Protocol", 
		"Protocol_package", "ProtocolAction", "ProtocolApplication", 
		"ProtocolAssociation", "ProtocolDefinition", "ProtocolDeviation", 
		"ProtocolEndTreatment", "ProtocolFile", "ProtocolFollowUp", "ProtocolFormsSet", 
		"ProtocolFormsTemplate", "ProtocolLimsFile", "ProtocolStatus", "ProtocolStep", 
		"ProtSequences", "Provenance", "ProviderOrganization", "Publication", 
		"PublicationSource", "PublicationStatus", "PubMed", "Purity", "PValue", 
		"QaReport", "QSC", "QSD", "QSET", "QSETBoundedPIVL", "QSI", "QSP", "QSS", 
		"QSU", "QTY", "QTZ", "Quadrupole", "Qualifier", "QualitativeEvaluation", 
		"QualityScore", "Quantile", "QuantitationType", "QuantitationTypeDimension", 
		"QuantitationTypeMap", "QuantitationTypeMapping", 
		"QuantitationTypeSearchCriteria", "Quantity", "QuantityInCount", 
		"QuantityInGram", "QuantityInMicrogram", "QuantityInMilliliter", 
		"QuantityUnit", "QuantSummaries", "QuantumDot", "QuantumDotComposition", 
		"QuarantineEventParameter", "Query", "Question", "QuestionCondition", 
		"QuestionConditionComponents", "QuestionGroup", "QuestionRepetition", 
		"QuestionValue", "Race", "Radiation", "RadiationAdministration", 
		"RadiationAttribution", "RadiationIntervention", "RadiationTherapy", 
		"RadicalProstatectomyGleasonHistologicGrade", 
		"RadicalProstatectomyGleasonHistopathologicGrade", 
		"RadicalProstatectomyInvasiveProstateCarcinoma", "RadicalProstatectomyMargin", 
		"RadicalProstatectomyPathologyAnnotation", 
		"RadicalProstatectomyProstateNegativeSurgicalMargin", 
		"RadicalProstatectomyProstatePositiveSurgicalMargin", 
		"RadicalProstatectomyProstateSpecimenCharacteristics", 
		"RadicalProstatectomyProstateSurgicalMargin", 
		"RadicalProstatectomyProstateSurgicalPathologySpecimen", "RadRXAnnotationSet", 
		"Range", "Ratio", "RawArrayData", "RawSample", "RBC", "Reaction", "REAL", 
		"ReceivedEventParameters", "Receptor", "Recipient", "RecipientDisease", 
		"RecipientLaboratoryFinding", "Recurrence", 
		"RecurrenceHealthExaminationAnnotation", 
		"ReexcisionCutaneousMelanomaSpecimenCharacteristics", 
		"ReexcisionCutaneousMelanomaSurgicalPathologySpecimen", "Reference", 
		"ReferenceChemical", "ReferencedAnnotation", "ReferencedCalculation", 
		"ReferenceDocument", "ReferenceEntity", "ReferenceGene", "ReferenceLink", 
		"ReferenceProtein", "ReferenceRNA", "ReferenceSequence", 
		"ReferenceSpotInformation", "RefSeqmRNA", "RefSeqProtein", "RefseqProtein", 
		"RegionalDistantSurgery", "RegionalLymphNode", "RegionalLymphNodeSurgery", 
		"RegionalNodeLymphadenectomyCutaneousMelanomaSpecimenCharacteristics", 
		"RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen", 
		"Registration", "RegistrationAsPatient", "RegistrationOutput", 
		"RegulatedIndustry", "Regulation", "Regulator", "RegulatoryElement", 
		"RegulatoryElementType", "RelatedGelItem", "RelatedGelItemProteinHit", 
		"RelationContainerIdentification", "Relations", 
		"RelationshipDistanceBasedPolicy", "RelationshipObservation", 
		"RelationshipPolicy", "RelationshipType", "RelationshipTypeBasedPolicy", 
		"RelativeLocation", "RelativeRecurringBlackout", "Relaxivity", "Remove", 
		"RenderingDetail", "Reorder", "Repeat", "Report", "ReportContent", 
		"ReportDefinition", "ReportDelivery", "ReportDeliveryDefinition", 
		"ReportedProblem", "Reporter", "ReporterBasedAnalysis", "ReporterCompositeMap", 
		"ReporterDimension", "ReporterGroup", "ReporterPosition", 
		"ReportMandatoryFieldDefinition", "ReportPerson", "ReportSection", 
		"ReportStatus", "ReportVersion", "RepositoryInfo", "Representation", "Request", 
		"ResearchCenter", "ResearchCenterDescription", "ResearchCenterInfoType", 
		"ResearchInstitutionSource", "ResearchStaff", 
		"ResectionBasedColorectalPathologyAnnotation", 
		"ResectionColonSpecimenCharacteristics", "ResectionColorectalDistalMargin", 
		"ResectionColorectalMesentricMargin", "ResectionColorectalProximalMargin", 
		"ResectionColorectalRadialMargin", "Resolution", "ResolvedConceptReference", 
		"ResolvedConceptReferenceList", "Response", "ResponseAssessment", 
		"ResponseMessage", "ResponseMetadata", "Result", 
		"RetropubicEnucleationPathologyAnnotation", "ReturnedEventParameter", 
		"ReturnEventParameters", "Review", "ReviewableUnit", "ReviewEventParameters", 
		"RFile", "RIM", "RimClass", "RNA", "RnaAnalyte", "RnaAnnotation", "RNAEntity", 
		"RnaExperiment", "RnaReport", "RnaResult", "Role", "RoleBasedRecipient", 
		"RouteOfAdministration", "RoutineAdverseEventReport", "RoutineOption", "RTO", 
		"RUID", "Run", "RunProtocol", "RunSampleContainer", 
		"SAEReportPreExistingCondition", "SAEReportPriorTherapy", "SafetyCaution", 
		"Sample", "SampleAnalyteProcessingSteps", "SampleAndOverlapsSet", 
		"SampleCategory", "SampleComposition", "SampleContainer", "SampleData", 
		"SampleInformation", "SampleInformationWithGender", "SampleLocation", 
		"SampleLog", "SampleManagement", "SampleOrigin", "SamplePlate", 
		"SampleProvider", "SampleSampleOrigin", "SampleSet", 
		"SampleSetWithMeanAndMarkersOutput", "SampleSOP", "SampleSOPFile", 
		"SampleType", "SampleWithChromosomalSegmentMeanAndMarkers", 
		"SampleWithChromosomalSegmentSet", "SampleWithGender", 
		"SampleWithOverlappedChromosomalSegmentSet", "SatelliteNodule", "SC", "Scalar", 
		"ScannerProperties", "Scheduled", "ScheduledActivity", 
		"ScheduledActivityState", "ScheduledActivityStudySegmentRule", 
		"ScheduledAdministrativeActivity", "ScheduledArm", "ScheduledArmSoA", 
		"ScheduledCalendar", "ScheduledEmailNotification", "ScheduledEpoch", 
		"ScheduledEpochRule", "ScheduledEvent", "ScheduledNotification", 
		"ScheduledObservation", "ScheduledProcedure", "ScheduledSoACell", 
		"ScheduledSpecimenCollection", "ScheduledStudyAgentTransfer", 
		"ScheduledStudyCell", "ScheduledStudySegment", 
		"ScheduledStudySegmentStudyCellRule", "ScheduledStudySoA", 
		"ScheduledSubjectStudyEncounter", "ScheduledSubjectStudyEncounterRule", 
		"ScheduledSubstanceAdministration", "ScheduledTimeLineEvent", "SCNT", 
		"Screening", "SCREENING", "ScreeningResult", "SearchDesignationOption", 
		"SearchResult", "SecondaryParticipantIdentifier", "SecondarySpecimenIdentier", 
		"SecondarySpecimenIdentifier", "SecondaryStructure", "Security", 
		"SecurityGroup", "SEER17", "Seer17", "Seg", "Segmentation", 
		"SegmentationOutput", "SegmentType", "SelectedCelFiles", "Selenocysteine", 
		"SemanticMetadata", "SemanticType", "SeqFeature", "SeqFeatureLocation", 
		"Sequence", "SequenceAnnotation", "SequenceConflict", "SequenceFragment", 
		"SequencePosition", "SequenceSimilarity", "SequenceVariant", "SequestScores", 
		"Series", "Service", "ServiceContext", "ServiceMetadata", 
		"ServiceSecurityMetadata", "SET", "SET(HXIT(T))", "SetResolutionPolicy", 
		"Sets", "SexDistribution", "Shape", "ShapeDisplayParameter", "Shipment", 
		"ShipmentRequest", "ShortColumn", "ShortParameter", "ShortSequenceMotif", 
		"Signalp", "SignalPeptide", "Silo", "SimpleName", "SimpleTimePeriod", "Site", 
		"SiteInvestigator", "Size", "SizeOfInvasiveCarcinoma", "SizeOfSpecimen", 
		"SkyCase", "SkyCase_header", "skyCellHeader", "skyChromosome", "SkyDataFile", 
		"SkyDataFileSet", "skyFrag", "SkyMageMappings", "Slide", "SLIST", 
		"SmallMolecule", "SmallMoleculeEntity", "SMatrix", "SmokingHealthAnnotation", 
		"SNP", "SNP2Allele", "SNP2Gene", "SNPAnalysisGroup", "SnpAnalysisResult", 
		"SNPAnnotation", "SnpAnnotation", "SnpArrayExperiment", "SNPArrayReporter", 
		"SNPAssay", "SNPAssociationAnalysis", "SNPAssociationFinding", 
		"SNPCytogeneticLocation", "SNPExternalLink", "snpFinding", 
		"SNPFrequencyFinding", "SNPMapping", "SNPPanel", "SNPPhysicalLocation", 
		"SNPProbeAnnotation", "SnpResult", "SocialHistory", "Software", 
		"SoftwareApplication", "Solubility", "SomaticMutationFinding", "SomCluster", 
		"SomClusteringParameter", "SOP", "SortContext", "SortDescription", 
		"SortDescriptionList", "SortOption", "SortOptionList", "Source", "SourceCode", 
		"SourceMeasurement", "SourceMeasurementProtocol", "SourceReference", "Span", 
		"SpatialCoordinate", "SpecializedQuantitationType", "Species", "species", 
		"SpeciesType", "SpecificDateBlackout", "Specimen", "SpecimenAcquisition", 
		"SpecimenAdditionalFinding", "SpecimenArray", "SpecimenArrayContent", 
		"SpecimenArrayOrderItem", "SpecimenArrayType", "SpecimenBasedAnalysis", 
		"SpecimenBasedFinding", "SpecimenBasedMolecularFinding", 
		"SpecimenBaseSolidTissuePathologyAnnotation", "SpecimenCharacteristics", 
		"SpecimenCollection", "SpecimenCollectionGroup", 
		"SpecimenCollectionRequirementGroup", "SpecimenDetails", 
		"SpecimenEventParameters", "SpecimenGleasonScore", "SpecimenHistologicGrade", 
		"SpecimenHistologicType", "SpecimenHistologicVariantType", "SpecimenIntegrity", 
		"SpecimenInvasion", "SpecimenNottinghamHistologicScore", "SpecimenOrderItem", 
		"SpecimenPosition", "SpecimenProcessing", "SpecimenProcessingStep", 
		"SpecimenProtocol", "SpecimenRequirement", "SpecimenSize", "SpectraData", 
		"SpliceVariant", "SpontaneousMutation", "Spot", "SpotAnalyteProcessingSteps", 
		"SpotDetectionParameters", "SpotDige", "SpotImage", "SpotMap", "SpotMapGroup", 
		"SpotRatio", "SpotSet", "SpotSetSpot", "SpunEventParameters", "ST", "Stain", 
		"StainingMethod", "STANDARDPOP", "StandardPopulation", 
		"StandardQuantitationType", "StandardUnit", "StartingQuantity", 
		"StaticRelationship", "Status", "Sterility", "STNT", "StorageContainer", 
		"StorageContainerCapacity", "StorageContainerDetails", "StorageDevice", 
		"StorageElement", "StorageSpace", "StorageType", "Strain", "Strand", 
		"Strength", "String", "StringArrayComponent", "StringColumn", 
		"StringParameter", "StrucDocBase", "StrucDocBr", "StrucDocCaption", 
		"StrucDocCaptioned", "StrucDocCMContent", "StrucDocCMFootnotes", 
		"StrucDocCMGeneral", "StrucDocCMInline", "StrucDocCMTitle", "StrucDocCol", 
		"StrucDocColGroup", "StrucDocColItem", "StrucDocContent", "StrucDocFootnote", 
		"StrucDocFootnoteRef", "StrucDocItem", "StrucDocLinkHtml", "StrucDocList", 
		"StrucDocParagraph", "StrucDocRenderMultiMedia", "StrucDocSub", "StrucDocSup", 
		"StrucDocTable", "StrucDocTableItem", "StrucDocTCell", "StrucDocText", 
		"StrucDocTitle", "StrucDocTitleFootnote", "StrucDocTRow", "StrucDocTRowGroup", 
		"StrucDocTRowPart", "StructuralAlignment", "STS", "STSIMPLE", "Study", 
		"StudyAgent", "StudyAuthor", "StudyCommittee", "StudyCoordinatingCenter", 
		"StudyFundingSponsor", "StudyInvestigator", "StudyObective", 
		"StudyObservation", "StudyOrganization", "StudyParticipant", 
		"StudyParticipantAssignment", "StudyPersonnel", "StudyProtocol", 
		"StudyPublication", "StudyRelationship", "StudySegment", "StudySegmentDelta", 
		"StudySite", "StudySponsor", "StudyStressor", "StudySubject", 
		"StudySubjectAssignment", "StudyTherapy", "StudyTimePoint", "SubImage", 
		"Subject", "SubjectAssignment", "SubjectGroup", "Submission", "SubmissionUnit", 
		"SubmissionUnitRelationship", "SubProject", "SubstanceAdministration", 
		"Subsystem", "SubsystemCell", "Summary", "Summation", "SuperProject", 
		"SuppGenotype", "SupportedAssociation", "SupportedAssociationQualifier", 
		"SupportedCodingScheme", "SupportedConceptStatus", "SupportedContext", 
		"SupportedDegreeOfFidelity", "SupportedElement", "SupportedElementList", 
		"SupportedFormat", "SupportedHierarchy", "SupportedLanguage", 
		"SupportedProperty", "SupportedPropertyLink", "SupportedPropertyQualifier", 
		"SupportedRepresentationalForm", "SupportedSource", 
		"SupportVectorMachineService", "Surface", "SurfaceChemistry", "SurfaceGroup", 
		"Surgery", "SurgeryAttribution", "SurgeryIntervention", "SurgeryTreatment", 
		"SurgicalMargin", "SurgicalPathologyReport", "SurgicalPathologySpecimen", 
		"Swissprot", "SynopticSurgicalPathologyReport", "SystemAssignedIdentifier", 
		"SystemOrganClass", "SystemRelease", "SystemReleaseDetail", 
		"SystemReleaseList", "Table1", "TableDataStructure", "TaggingProcess", 
		"TandemSequenceData", "Target", "TargetedModification", "Targeting", 
		"TargetingFunction", "TargetResponseMessage", "Taxon", "Technique", "TEL", 
		"TELEMAIL", "TELPERSON", "TELPHONE", "TELURL", "Temperature", 
		"TemperatureUnit", "Term", "term", "term2term", "TermBasedCharacteristic", 
		"TermBasedFactorValue", "TermBasedParameterValue", 
		"TerminologyServiceInfoType", "TerminologyServiceMetadata", "TermSource", 
		"TermValue", "Text", "TextAnnotation", "TextContent", "TextMeasurement", 
		"ThawEventParameters", "TherapeuticAgent", "TherapeuticFunction", 
		"TherapeuticProcedure", "Therapy", "ThreeDimensionalSize", 
		"ThreeDimensionalTumorSize", "ThreeDimensionSpatialCoordinate", "Threshold", 
		"Tile", "TiledImage", "TimeCourse", "TimeOfFlight", "TimePeriod", "TimePoint", 
		"TimeRecord", "TimeScaleUnit", "TimeSeries", "TimeUnit", "Tissue", 
		"TissueBlock", "TissueMicroarray", "TissueSide", "TissueSlide", 
		"TissueSpecimen", "TissueSpecimenRequirement", 
		"TissueSpecimenReviewEventParameters", "TmaCylinder", "TmaData", 
		"TmaDataPanel", "TmaDisc", "TmaSlide", "TmaTextonPanel01", "TMatrix", "Tmhmm", 
		"TN", "TOBACCOTAX", "TopologicalDomain", "Topology", "TotalGenes", 
		"TotalProteinContent", "Toxicity", "ToxicityMedicalProblems", 
		"ToxicityStudyAssessment", "Toxicology", "Trace2Genotype", 
		"TrancheAnnotationTemplateInformation", "TrancheDataAnnotationSet", 
		"TransanalDiskExcisionSpecimenCharacteristics", "Transcript", 
		"TranscriptAnnotation", "TranscriptArrayReporter", 
		"TranscriptPhysicalLocation", "Transfection", "TransferEventParameters", 
		"Transformation", "Transgene", "TransientInterference", 
		"TransientInterferenceMethod", "TransitPeptide", "TransmembraneRegion", 
		"Transplantation", "TransurethralProstaticResectionPathologyAnnotation", 
		"TransurethralResectionInvasiveProstateCarcinoma", 
		"TransurethralResectionProstateSpecimenCharacteristics", 
		"TransurethralResectionProstateSurgicalPathologySpecimen", "TreatedAnalyte", 
		"TreatedAnalyteAnalyteProcessingSteps", "Treatment", "TreatmentAnnotation", 
		"TreatmentAssignment", "TreatmentInformation", "TreatmentOrder", 
		"TreatmentRegimen", "TreatmentSchedule", "TreeNode", "TrialDataProvenance", 
		"TriggerAction", "TS", "TSBIRTH", "TSDATE", "TSDATEFULL", "TSDATETIME", 
		"TSDATETIMEFULL", "TumorCode", "TumorPathology", "TumorSize", 
		"TumorSpecimenBiopsy", "TumorTissueSite", "Turn", 
		"TwoDimensionSpatialCoordinate", "TwoDLiquidChromatography1stDimension", 
		"TwoDLiquidChromatography2ndDimension", "TwoDSpotDatabaseSearch", 
		"TwoDSpotMassSpec", "Type", "TYPE", "TypeEnumerationMetadata", "UCUM", "Uid", 
		"UID", "UMatrix", "UMLAssociation", "UMLAssociationEdge", 
		"UMLAssociationMetadata", "UMLAttribute", "UMLAttributeMetadata", "UMLClass", 
		"UMLClassMetadata", "UMLClassReference", "UMLGeneralization", 
		"UMLGeneralizationMetadata", "UMLPackageMetadata", "UnclassifiedAgent", 
		"UnclassifiedAgentTarget", "UnclassifiedLinkage", "UniGene", 
		"UnigeneTissueSource", "UninvolvedMelanomaMargin", "UniprotAccession", 
		"UniProtKB", "UniprotkbAccession", "Unit", "UnitedStatesMortality", 
		"UnitedStatesPopulation", "UnsureResidue", "URG", "URGHIGH", "URGLOW", "Uri", 
		"URL", "URLSourceReference", "URNMap", "User", "UserComments", 
		"UserDefinedCharacteristic", "UserDefinedFactorValue", 
		"UserDefinedParameterValue", "UserDefinedValue", "UserGroup", 
		"UserOrganization", "UserProtectionElement", "UserRoleContext", "USMORTALITY", 
		"USPOPULATION", "UUID", "UVP", "ValidValue", "Value", "ValueDomain", 
		"ValueDomainPermissibleValue", "ValueDomainRelationship", "ValueMeaning", 
		"ValuesNearToCutoff", "VariationFinding", "VariationHeatmap", 
		"VariationReporter", "Versionable", "VersionableAndDescribable", "Vocabulary", 
		"VocabularyConstraints", "Volume", "VolumeUnit", "WebImageReference", 
		"WebServicesSourceReference", "WeekDayBlackout", "Well", "Window", "Word", 
		"Worksheet", "Xenograft", "XML", "XMLContent", "XmlReport", "XTandemScores", 
		"YeastModel", "ZincFingerRegion", "Zone", "ZoneDefect", "ZoneGroup", 
		"ZoneLayout"
	};
}
