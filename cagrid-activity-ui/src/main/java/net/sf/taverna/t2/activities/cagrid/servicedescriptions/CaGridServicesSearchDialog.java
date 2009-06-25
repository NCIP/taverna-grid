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
package net.sf.taverna.t2.activities.cagrid.servicedescriptions;

import gov.nih.nci.cadsr.umlproject.domain.Project;
import gov.nih.nci.cadsr.umlproject.domain.UMLClassMetadata;
import gov.nih.nci.cadsr.umlproject.domain.UMLPackageMetadata;
import org.cagrid.cadsr.client.CaDSRUMLModelService;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.cagrid.config.CaGridConfiguration;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

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
	
	public CaGridServicesSearchDialog()  {
		super((Frame) null, "caGrid services search criteria", true, null); // create a modal dialog
		initComponents();
		setLocation(50,50);
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
        queryButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        queryButtonsPanel.setBorder(new EmptyBorder(0,0,0,25));
        final JButton updateCaDSRDataButton = new JButton("Update caDSR metadata");
        updateCaDSRDataButton.setToolTipText("Get an updated UML class list from caDSR Service. \n" +
		"This operation may take a few minutes depending on network status.");
        updateCaDSRDataButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				Thread updateCaDSRDataThread = new Thread("Updating caDSR metadata") {
					public void run() {
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
    							 //System.out.println("\n"+ project.getShortName());
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
    										 //System.out.println("\t-" + pack.getName());
    										 try {
    											 classes = cadsr.findClassesInPackage(project, pack.getName());
    										 }
    										 catch (Exception e) {
    											 e.printStackTrace();
    										 }
    										 if(classes !=null){
    											 for (int k=0;k<classes.length;k++){
    												 UMLClassMetadata clazz = classes [k];
    												 //System.out.println("\t\t-"+clazz.getName());
    												 if(!classNameList.contains(clazz.getName()))
    													 //classNameList is updated here!
    													 classNameList.add((String)clazz.getName());
    												 else {
    													 //System.out.println("Duplicated Class Name Found.");
    												 }
    											 }
    										 }
    									 }
    								 }
    							 }
    						 }
    					 }
    					 
    					 String [] clsNameArray;
    					 // If the retrieved class name list is not empty, update the static datatype classNameArray
    					 if(!classNameList.isEmpty()){
    						 clsNameArray = (String[]) classNameList.toArray(new String[0]);
    						 Arrays.sort(clsNameArray,String.CASE_INSENSITIVE_ORDER);		                					       
    						 logger.info("=========Class Names Without Duplications=============");
    						 for(int i=0;i<clsNameArray.length;i++){
    							 System.out.println(clsNameArray[i]);
    						 }
    						 classNameArray  = clsNameArray;
    						 JOptionPane.showMessageDialog(null, "caDSR metadata has been updated. \n Now there are " + classNameArray.length + " UML classes in the list.", null, JOptionPane.INFORMATION_MESSAGE);   
    					 }
    					 else{
    						 //the current value of the GT4ScavengerHelper.classNameArray is not updated
    						 //System.out.println("Empty class name list retrieved, so classNameArray is not updated!");
    						 JOptionPane.showMessageDialog(null,"Empty class name list retrieved from caDSR Service, so UML class names have not been updated!", null, JOptionPane.INFORMATION_MESSAGE);
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
 
        queryButtonsPanel.add(okButton);
        queryButtonsPanel.add(updateCaDSRDataButton);
        queryButtonsPanel.add(cancelButton);        
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
	// The data is retrieved at 8:30 am, June 16th, 2008.
	public static String []classNameArray = {
		"A2Conjugate", 
		"A2Experiment", "A2LP4Parameters", "A2Plate", "A2Sample", 
		"A2Spot42", "A2SpotData", "A2SpotSetup", "A2SpotsStatistics", 
		"A2StandardCurve", "A2Well", "AbsoluteCodingSchemeVersionReference", "AbsoluteCodingSchemeVersionReferenceList", 
		"AbsoluteNeutrophilCount", "AbstractAdverseEventTerm", "AbstractArrayData", "AbstractBioMaterial", 
		"AbstractCaArrayEntity", "AbstractCaArrayObject", "AbstractCancerModel", "AbstractCharacteristic", 
		"AbstractContact", "AbstractDataColumn", "AbstractDesignElement", "AbstractDomainObject", 
		"Abstraction", "AbstractMeddraDomain", "AbstractProbe", "AbstractProbeAnnotation", 
		"AbstractStudyDisease", "Accession", "AccessionCharacteristics", "AccessRights", 
		"AcquisitionProcedure", "ActionSuccessor", "ActivationMethod", "ActiveIngredient", 
		"ActiveObservation", "ActiveSite", "Activity", "ActivityRelationship", 
		"ActivitySummary", "ActivityType", "AcuteGraftVersusHostDisease", "Add", 
		"AdditionalFindings", "AdditionalInformation", "AdditionalOrganismName", "Address", 
		"AddToReferenceParameters", "AdjacencyMatrix", "AdministeredComponent", "AdministeredComponentClassSchemeItem", 
		"AdministeredComponentContact", "AdministeredDrug", "AdverseEvent", "AdverseEventAttribution", 
		"AdverseEventCtcTerm", "AdverseEventDetail", "AdverseEventMeddraLowLevelTerm", "AdverseEventNotification", 
		"AdverseEventResponseDescription", "AdverseEventTherapy", "AeTerminology", "Agent", 
		"AgentOccurrence", "AgentSynonym", "AgentTarget", "Alignment", 
		"Aliquot", "Allergy", "AlternateProteinHit", "Amendment", 
		"AmendmentApproval", "AnalysisGroup", "AnalysisGroupResult", "AnalysisParameters", 
		"AnalysisRecord", "AnalysisRoutine", "AnalysisRun", "AnalysisRunSet", 
		"AnalysisVariable", "Analyte", "AnalyteProcessingStep", "AnatomicEntity", 
		"AnatomicSite", "Animal", "AnimalAvailability", "AnimalDistributor", 
		"AnimalModel", "AnnotatableEntity", "AnnotatableEvent", "Annotation", 
		"AnnotationEventParameters", "AnnotationManager", "AnnotationOfAnnotation", "AnnotationSet", 
		"Anomaly", "Antibody", "Antigen", "Application", 
		"ApplicationContext", "AppliedParameter", "ApprovalStatus", "AracneParameter", 
		"Array", "ArrayDataType", "ArrayDesign", "ArrayDesignDetails", 
		"ArrayGroup", "ArrayManufacture", "ArrayManufactureDeviation", "ArrayReporter", 
		"ArrayReporterCytogeneticLocation", "ArrayReporterPhysicalLocation", "Assay", "AssayDataPoint", 
		"AssayType", "Assembly", "Assessment", "AssessmentRelationship", 
		"AssociatedElement", "AssociatedFile", "AssociatedObservationWrapper", "Association", 
		"AssociativeFunction", "Atom", "Attachment", "AttributeSetDescriptor", 
		"AttributeTypeMetadata", "Audit", "AuditEvent", "AuditEventDetails", 
		"AuditEventLog", "AuditEventQueryLog", "Availability", "AveragedSpotInformation", 
		"BACCloneReporter", "BaselineData", "BaselineHistoryPE", "BaselineStage", 
		"BasicHistologicGrade", "BehavioralMeasure", "BibliographicReference", "BindingSite", 
		"BioAssay", "BioAssayCreation", "BioAssayData", "BioAssayDataCluster", 
		"BioAssayDatum", "BioAssayDimension", "BioAssayMap", "BioAssayMapping", 
		"BioAssayTreatment", "BiocartaMap", "BiocartaReport", "BiocartaSource", 
		"BioDataCube", "BioDataTuples", "BioDataValues", "BioEvent", 
		"Biohazard", "BiologicalProcess", "BioMaterial", "BioMaterial_package", 
		"BioMaterialMeasurement", "Biopolymer", "BioSample", "BioSequence", 
		"BioSource", "BlackoutDate", "BloodContact", "BooleanColumn", 
		"BooleanParameter", "BreastCancerAccessionCharacteristics", "BreastCancerBiomarkers", "BreastCancerTNMFinding", 
		"BreastNegativeSurgicalMargin", "BreastPositiveSurgicalMargin", "BreastSpecimenCharacteristics", "BreastSurgicalPathologySpecimen", 
		"CaArrayFile", "CalciumBindingRegion", "CalculatedMeasurement", "CalculatedMeasurementProtocol", 
		"Calculation", "CalculationResult", "Canceled", "CancerModel", 
		"CancerResearchGroup", "CancerStage", "CancerTNMFinding", "Capacity", 
		"CarbonNanotube", "CarbonNanotubeComposition", "CarcinogenExposure", "CarcinogenicIntervention", 
		"caseDetail", "CaseReportForm", "Caspase3Activation", "CatalystActivity", 
		"Category", "CategoryObservation", "CategorySummaryExport", "CategorySummaryReportRow", 
		"CD", "CellLine", "CellLysateFinding", "cells", 
		"CellSpecimen", "CellSpecimenRequirement", "CellSpecimenReviewParameters", "CellViability", 
		"CentralLaboratory", "CFU_GM", "cghSamples", "Chain", 
		"Change", "ChangedGenes", "Channel", "Characterization", 
		"CharacterizationProtocol", "CharacterParameter", "CheckInCheckOutEventParameter", "ChemicalAssociation", 
		"ChemicalClass", "ChemicalStressor", "ChemicalStressorProtocol", "ChemicalTreatment", 
		"Chemotaxis", "Chemotherapy", "ChemotherapyData", "ChimerismSample", 
		"ChromatogramPoint", "Chromosome", "ChromosomeMap", "ChronicGraftVersusHostDisease", 
		"Chunk", "Circle", "citation", "ClassComparisonAnalysis", 
		"ClassComparisonAnalysisFinding", "ClassificationScheme", "ClassificationSchemeItem", "ClassificationSchemeItemRelationship", 
		"ClassificationSchemeRelationship", "ClassMembership", "ClassSchemeClassSchemeItem", "ClinicalAssessment", 
		"ClinicalFinding", "ClinicalMarker", "ClinicalReport", "ClinicalResult", 
		"ClinicalTrial", "ClinicalTrialProtocol", "ClinicalTrialSite", "ClinicalTrialSponsor", 
		"ClinicalTrialSubject", "Clone", "CloneRelativeLocation", "Cluster", 
		"CNSAccessionCharacteristics", "CNSCarcinoma", "CNSHistologicGrade", "CNSNeoplasmHistologicType", 
		"CNSSpecimenCharacteristics", "Coagulation", "codedContext", "codedEntry", 
		"CodedNodeSetImpl", "CodeSequence", "CodingSchemeRendering", "CodingSchemeRenderingList", 
		"CodingSchemeSummary", "CodingSchemeSummaryList", "CodingSchemeTag", "CodingSchemeTagList", 
		"CodingSchemeURNorName", "CodingSchemeVersionOrTag", "CodingSchemeVersionStatus", "Coefficients", 
		"Cohort", "CoiledCoil", "CoiledCoilRegion", "Coils", 
		"CollaborativeStaging", "CollectionEventParameters", "CollectionProtocol", "CollectionProtocolEvent", 
		"CollectionProtocolRegistration", "CollisionCell", "ColorectalAccessionCharacteristics", "ColorectalCancerTNMFinding", 
		"ColorectalHistologicGrade", "ColorectalSpecimenCharacteristics", "CometScores", "comment", 
		"Comment", "Comments", "CommonLookup", "Comorbidity", 
		"ComparativeMarkerSelectionParameterSet", "ComparativeMarkerSelectionResultCollection", "ComplementActivation", "Complex", 
		"ComplexComponent", "ComplexComposition", "ComponentConcept", "ComponentLevel", 
		"ComponentName", "ComposingElement", "CompositeCompositeMap", "CompositeGroup", 
		"CompositePosition", "CompositeSequence", "CompositeSequenceDimension", "CompositeSequenceSummary", 
		"CompositionallyBiasedRegion", "Compound", "CompoundMeasurement", "ConcentrationUnit", 
		"Concept", "ConceptClassification", "conceptCode", "ConceptCodes", 
		"ConceptDerivationRule", "ConceptDescriptor", "conceptProperty", "ConceptReference", 
		"ConceptReferenceList", "ConceptReferent", "concepts", "ConceptualDomain", 
		"ConcomitantMedication", "ConcomitantMedicationAttribution", "ConcomitantMedicationDetail", "ConcomitantProcedure", 
		"ConcomitantProcedureDetail", "Condition", "Conditional", "Conditionality", 
		"ConditionGroup", "ConditionMessage", "ConfidenceIndicator", "ConsensusClusteringParameterSet", 
		"ConsensusClusterResultCollection", "ConsensusIdentifierData", "ConsensusMatrix", "ConsensusMatrixRow", 
		"Constraint", "Contact", "ContactCommunication", "ContactDetails", 
		"ContactInfo", "ContactMechanismBasedRecipient", "ContactMechanismType", "Container", 
		"ContainerInfo", "ContainerType", "Context", "context", 
		"Contour", "Control", "ControlGenes", "ControlledVocabularyAnnotation", 
		"Coordinate", "CopyNumberFinding", "CourseAgent", "CourseAgentAttribution", 
		"CourseDate", "CrossLink", "Ctc", "CtcCategory", 
		"CtcGrade", "CtcTerm", "CtepStudyDisease", "Culture", 
		"CultureProtocol", "CurationData", "CustomProperties", "CutaneousMelanoma", 
		"CutaneousMelanomaAccessionCharacteristics", "CutaneousMelanomaAdditionalFindings", "CutaneousMelanomaNegativeSurgicalMargin", "CutaneousMelanomaNeoplasmHistologicType", 
		"CutaneousMelanomaPositiveSurgicalMargin", "CutaneousMelanomaSpecimenCharacteristics", "CutaneousMelanomaSurgicalPathologySpecimen", "CutaneousMelanomaTNMFinding", 
		"Cycle", "Cytoband", "CytobandPhysicalLocation", "CytogeneticLocation", 
		"CytokineInduction", "Cytotoxicity", "Data", "Database", 
		"DatabaseCrossReference", "DatabaseEntry", "DatabaseSearch", "DatabaseSearchParameters", 
		"DatabaseSearchParametersOntologyEntry", "DataElement", "DataElementConcept", "DataElementConceptRelationship", 
		"DataElementDerivation", "DataElementRelationship", "DataFile", "DataFileLimsFile", 
		"DataItem", "DataRetrievalRequest", "DataServiceInstance", "DataSet", 
		"DataSource", "DataStatus", "DataType", "Datum", 
		"dbxref", "dc", "DeathSummary", "DefinedParameter", 
		"Definition", "definition", "DefinitionClassSchemeItem", "DeliveryStatus", 
		"Delta", "Demographics", "Dendrimer", "DendrimerComposition", 
		"Department", "DerivationType", "DerivedArrayData", "DerivedBioAssay", 
		"DerivedBioAssayData", "DerivedBioAssays", "DerivedDataElement", "DerivedDataFile", 
		"DerivedDatum", "DerivedDNACopySegment", "DerivedSignal", "DescLogicConcept", 
		"DescLogicConceptVocabularyName", "describable", "Describable", "Description", 
		"Designation", "DesignationClassSchemeItem", "DesignElement", "DesignElementDimension", 
		"DesignElementGroup", "DesignElementList", "DesignElementMap", "DesignElementMapping", 
		"Detection", "DeviceAttribution", "DeviceOperator", "Diagnosis", 
		"DICOMImageReference", "DiffFoldChangeFinding", "Dige", "DigeGel", 
		"DigeSpotDatabaseSearch", "DigeSpotMassSpec", "Dimension", "DirectedAcyclicGraph", 
		"Disease", "DiseaseAttribution", "DiseaseCategory", "DiseaseEvaluation", 
		"DiseaseEvaluationDetail", "DiseaseExtent", "DiseaseHistory", "DiseaseOntology", 
		"DiseaseOntologyRelationship", "DiseaseOutcome", "DiseaseResponse", "DiseaseTerm", 
		"DiseaseTerminology", "DisposalEventParameters", "DistanceUnit", "DistantSite", 
		"DistributedItem", "Distribution", "DistributionProtocol", "DistributionProtocolAssignment", 
		"DistributionProtocolOrganization", "DisulfideBond", "DNA", "DNABindingRegion", 
		"DNAcopyAssays", "DNAcopyParameter", "DNASpecimen", "Documentation", 
		"Domain", "DomainDescriptor", "DomainName", "Donor", 
		"DonorInfo", "DonorRecipient", "Dose", "DoubleColumn", 
		"DoubleParameter", "Drug", "DrugSurgeryData", "Duration", 
		"EdgeProperties", "EditActionDate", "Electrospray", "Ellipse", 
		"EmbeddedEventParameters", "Emulsion", "EmulsionComposition", "Encapsulation", 
		"EndpointCode", "EngineeredGene", "EnsemblGene", "EnsemblPeptide", 
		"EnsemblTranscript", "EntityAccession", "entityDescription", "EntityMap", 
		"EntityName", "EntrezGene", "EnucleationInvasiveProstateCarcinoma", "EnucleationProstateSpecimenCharacteristics", 
		"EnucleationProstateSurgicalPathologySpecimen", "EnumeratedValueDomain", "EnvironmentalFactor", "EnzymeInduction", 
		"Epoch", "EpochDelta", "Equipment", "Error", 
		"Event", "EventEntity", "EventEntitySet", "EventParameters", 
		"EventRecords", "Evidence", "EvidenceCode", "EvidenceKind", 
		"EVSDescLogicConceptSearchParams", "EVSHistoryRecordsSearchParams", "EVSMetaThesaurusSearchParams", "EVSSourceSearchParams", 
		"Examination", "ExcisionCutaneousMelanomaSpecimenCharacteristics", "ExcisionCutaneousMelanomaSurgicalPathologySpecimen", "Execution", 
		"ExocrinePancreasAccessionCharacteristics", "ExocrinePancreasSpecimenCharacteristics", "ExocrinePancreaticCancerTNMFinding", "Exon", 
		"ExonArrayReporter", "ExonProbeAnnotation", "ExpectedValue", "ExpeditedAdverseEventReport", 
		"Experiment", "Experiment2DGelList", "Experiment2DLiquidChromatography", "Experiment2DLiquidChromatography1stSetup", 
		"Experiment2DLiquidChromatography2ndSetup", "ExperimentalFactor", "ExperimentalFeatures", "ExperimentalStructure", 
		"ExperimentContact", "ExperimentDesign", "ExperimentRun", "ExperimentTo2DGel", 
		"ExperimentToDatabaseSearch", "ExperimentToDige", "ExperimentToMassSpec", "Exponent", 
		"ExportStatus", "ExpressedSequenceTag", "ExpressionArrayReporter", "ExpressionData", 
		"ExpressionFeature", "ExpressionLevelDesc", "ExpressionProbeAnnotation", "ExpressoParameter", 
		"Extendable", "ExtensionDescription", "ExtensionDescriptionList", "ExternalIdentifier", 
		"ExternalReference", "Extract", "Facility", "Factor", 
		"FactorValue", "Failed", "FamilyHistory", "FastaFiles", 
		"FastaSequences", "Feature", "FeatureData", "FeatureDefect", 
		"FeatureDimension", "FeatureExtraction", "FeatureGroup", "FeatureInformation", 
		"FeatureLocation", "FeatureReporterMap", "FeatureType", "FemaleReproductiveCharacteristic", 
		"Ffas", "Fiducial", "File", "FileType", 
		"Filters", "Finding", "FirstCourseRadiation", "FirstCourseTreatmentSummary", 
		"FISHFinding", "FixedEventParameters", "FloatColumn", "FloatingPointQuantity", 
		"FloatParameter", "FluidSpecimen", "FluidSpecimenRequirement", "FluidSpecimenReviewEventParameters", 
		"Fold", "Folder", "Followup", "Form", 
		"FormatType", "FormElement", "Fraction", "FractionAnalyteSteps", 
		"Fractions", "FrozenEventParameters", "FuhrmanNuclearGrade", "Fullerene", 
		"FullereneComposition", "Function", "functionalCategory", "FunctionalDNADomain", 
		"FunctionalizingEntity", "FunctionalProteinDomain", "FunctionalRole", "GbmDrugs", 
		"GbmPathology", "GbmSlide", "GbmSurgery", "Gel", 
		"Gel2d", "GelImage", "GelImageType", "GelPlug", 
		"GelSpot", "GelSpotList", "GelStatus", "Genbank", 
		"GenBankAccession", "GenBankmRNA", "GenBankProtein", "Gene", 
		"gene_product", "GeneAgentAssociation", "GeneAlias", "GeneAnnotation", 
		"GeneBiomarker", "GeneCategoryExport", "GeneCategoryMatrix", "GeneCategoryMatrixRow", 
		"GeneCategoryReportRow", "GeneCytogeneticLocation", "GeneDelivery", "GeneDiseaseAssociation", 
		"GeneExprReporter", "GeneFunction", "GeneFunctionAssociation", "GeneGenomicIdentifier", 
		"GeneNeighborsParameterSet", "GeneOntology", "GeneOntologyRelationship", "GenePhysicalLocation", 
		"GenePubmedSummary", "GeneRelativeLocation", "GeneReporterAnnotation", "GenericArray", 
		"GenericReporter", "GeneticAlteration", "GeneVersion", "GenomeEncodedEntity", 
		"GenomicIdentifier", "GenomicIdentifierSet", "GenomicIdentifierSolution", "GenomicSegment", 
		"Genotype", "GenotypeDiagnosis", "GenotypeFinding", "GenotypeSummary", 
		"Genus", "GeometricShape", "GleasonHistologicGrade", "GleasonHistopathologicGrade", 
		"GlycosylationSite", "GominerGene", "GominerTerm", "GOTerm", 
		"Grade", "GradientStep", "Graft", "GraftVersusHostDisease", 
		"GraftVersusHostDiseaseOutcome", "Group", "GroupRoleContext", "Hap2Allele", 
		"Haplotype", "Hardware", "HardwareApplication", "header", 
		"HealthcareSite", "HealthCareSite", "HealthcareSiteInvestigator", "HealthcareSiteParticipant", 
		"HealthcareSiteParticipantRole", "Helix", "HematologyChemistry", "Hemolysis", 
		"HemTransplantEndocrineProcedure", "Hexapole", "HierarchicalCluster", "HierarchicalClusteringMage", 
		"HierarchicalClusteringParameter", "HierarchicalClusterNode", "HighLevelGroupTerm", "HighLevelTerm", 
		"HistologicGrade", "Histology", "HistopathologicGrade", "Histopathology", 
		"HistopathologyGrade", "History", "HistoryRecord", "Hmmpfam", 
		"HomologAlignment", "HomologousAssociation", "HormoneTherapy", "Hybridization", 
		"HybridizationData", "Hypothesis", "Identifiable", "IdentificationScheme", 
		"IdentifiedPathologyReport", "IdentifiedPatient", "IdentifiedSection", "Identifier", 
		"IHCFinding", "II", "Image", "ImageAcquisition", 
		"ImageAnnotation", "ImageContrastAgent", "ImageDataItem", "ImageReference", 
		"ImageType", "ImageView", "ImageViewModifier", "Imaging", 
		"ImagingFunction", "ImagingObservation", "ImagingObservationCharacteristic", "ImmuneCellFunction", 
		"Immunologic", "Immunotherapy", "Immunotoxicity", "ImmunoToxicity", 
		"INDHolder", "InducedMutation", "InitiatorMethionine", "Input", 
		"InputFile", "Instance", "Institution", "Instruction", 
		"instruction", "Instrument", "InstrumentConfiguration", "InstrumentType", 
		"IntegerColumn", "IntegerParameter", "IntegerQuantity", "IntegrationType", 
		"Interaction", "InternetSource", "Intron", "InvasiveBreastCarcinoma", 
		"InvasiveBreastCarcinomaNeoplasmHistologicType", "InvasiveColorectalCarcinoma", "InvasiveColorectalCarcinomaNeoplasmHistologicType", "InvasiveExocrinePancreaticCarcinoma", 
		"InvasiveExocrinePancreaticCarcinomaNeoplasmHistologicType", "InvasiveKidneyCarcinoma", "InvasiveKidneyCarcinomaNeoplasmHistologicType", "InvasiveLungCarcinoma", 
		"InvasiveLungCarcinomaNeoplasmHistologicType", "InvasiveProstateCarcinoma", "InvasiveProstateCarcinomaNeoplasmHistologicType", "Investigation", 
		"InvestigationalNewDrug", "Investigator", "InvestigatorHeldIND", "InvitroCharacterization", 
		"InvivoResult", "Invoker", "IonSource", "IonTrap", 
		"JaxInfo", "JpegImage", "Jpred", "Keyword", 
		"KidneyAccessionCharacteristics", "KidneyAdditionalFindings", "KidneyCancerTNMFinding", "KidneySpecimenCharacteristics", 
		"Lab", "LabeledExtract", "LabFile", "LabGeneral", 
		"LabGroup", "LabMember", "Laboratory", "LaboratoryEquipment", 
		"LaboratoryFinding", "LaboratoryPersonnel", "LaboratoryProject", "LaboratoryResult", 
		"LaboratorySamplePlate", "LaboratoryStorageDevice", "LaboratoryTest", "LabSpecial", 
		"LabValue", "Lambda", "LesionDescription", "LesionEvaluation", 
		"LeukocyteProliferation", "LevelOfExpressionIHCFinding", "LexBIGServiceImpl", "Library", 
		"LimsFile", "Lineage", "Linkage", "LinkType", 
		"LipidMoietyBindingRegion", "Liposome", "LiposomeComposition", "LiquidChromatographyColumn", 
		"List", "ListProcessing", "LiteratureRelationship", "LoadStatus", 
		"localId", "LocalNameList", "Location", "Log", 
		"LogEntry", "LogicalProbe", "LogLevel", "LOHFinding", 
		"LongColumn", "LongParameter", "LongTermFU", "LossOfExpressionIHCFinding", 
		"LowComplexityRegion", "LowLevelTerm", "Lsid", "LungAccessionCharacteristics", 
		"LungCancerTNMFinding", "LungDrugs", "LungExam", "LungNeoplasm", 
		"LungPathology", "LungSlide", "LungSpecimenCharacteristics", "LungSurgery", 
		"Macroprocess", "MAGE", "Maldi", "ManufactureLIMS", 
		"ManufactureLIMSBiomaterial", "Manufacturer", "Map", "Mapping", 
		"Marker", "MarkerAlias", "MarkerPhysicalLocation", "MarkerRelativeLocation", 
		"MarkerResult", "MarketingAuthorization", "MarketingAuthorizationHolderManufacturerDistributor", "MascotScores", 
		"MassQuery", "MassSpecDatabaseSearch", "MassSpecExperiment", "MassSpecMachine", 
		"MassSpecMassSpecFraction", "MassUnit", "MatchingParameters", "Material", 
		"MaterialSource", "MaterialType", "MathFile", "MeasuredBioAssay", 
		"MeasuredBioAssayData", "MeasuredBioAssays", "MeasuredSignal", "Measurement", 
		"MeasurementCharacteristic", "MeasurementProtocol", "MeasureUnit", "Meddra", 
		"MeddraStudyDisease", "MedicalDevice", "MedicinalProduct", "MessengerRNA", 
		"MetadataProperty", "MetadataPropertyList", "MetalIonBindingSite", "MetalParticle", 
		"MetalParticleComposition", "MetastasisSite", "MetastaticDiseaseSite", "MetaThesaurusConcept", 
		"Method", "MethodParameter", "Methylation", "MethylationDnaSequence", 
		"MethylationSite", "MethylationSiteMeasurement", "Microarray", "MicroArrayData", 
		"MicroarrayEventRecords", "MicroarraySet", "MismatchInformation", "Missed", 
		"MobilePhaseComponent", "Model", "ModelGroup1", "Modeller", 
		"ModelSection", "ModelStructure", "Modifications", "ModificationType", 
		"ModifiedResidue", "Module", "ModuleDescription", "ModuleDescriptionList", 
		"MolecularSpecimen", "MolecularSpecimenRequirement", "MolecularSpecimenReviewParameters", "MolecularWeight", 
		"Morpholino", "Morphology", "Mouse", "mRNAGenomicIdentifier", 
		"MS2Runs", "Msi", "MultiPoint", "MultiProcessParameters", 
		"MutagenesisSite", "MutationIdentifier", "MutationVariation", "MZAnalysis", 
		"MzAssays", "MzSpectrum", "MZXMLSubFiles", "Name", 
		"NameAndValue", "NameAndValueList", "NameValueType", "Nanoparticle", 
		"NanoparticleDatabaseElement", "NanoparticleEntity", "NanoparticleSample", "NanoparticleStudy", 
		"NeedleBiopsyInvasiveProstateCarcinoma", "NeedleBiopsyProstateSpecimenCharacteristics", "NeedleBiopsyProstateSurgicalPathologySpecimen", "NegativeControl", 
		"Neoplasm", "NeoplasmHistologicType", "NeoplasmHistopathologicType", "NKCellCytotoxicActivity", 
		"Node", "NodeContents", "NodeValue", "Noise", 
		"Nomenclature", "NonCancerDirectedSurgery", "NonConsecutiveResidues", "NonenumeratedValueDomain", 
		"NonTerminalResidue", "NonverifiedSamples", "NormalChromosome", "NormalizeInvariantSetParameter", 
		"NormalizeMethodParameter", "NormalizeQuantilesRobustParameter", "NotApplicable", "NotificationBodyContent", 
		"NottinghamHistologicGrade", "NottinghamHistopathologicGrade", "NucleicAcidPhysicalLocation", "NucleicAcidSequence", 
		"NucleotideBindingRegion", "NucleotidePhosphateBindingRegion", "Null", "NumericalRangeConstraint", 
		"NumericMeasurement", "NumericOID", "ObjectClass", "ObjectClassRelationship", 
		"Observation", "ObservationConcept", "ObservationData", "ObservationProtocol", 
		"ObservationRelationship", "ObservationState", "ObservedThing", "ObservedThingToObservationConnection", 
		"ObservedThingToObservedThingRelationship", "ObservedThingToOntologyElementRelationship", "Occurred", "OctaveFile", 
		"OddsRatio", "OffTreatment", "OMIM", "OnStudy", 
		"OntologyDimension", "OntologyElement", "OntologyElementSlot", "OntologyElementSlotSet", 
		"OntologyElementToOntologyElementRelationship", "OntologyEntry", "OntologyGroup", "OntologyGroupToOntologyGroupRelationship", 
		"OrderItem", "OrderOfNodeTraversal", "OrderSet", "Organ", 
		"Organelle", "Organism", "OrganismName", "Organization", 
		"OrganizationAssignedIdentifier", "OrganizationHeldIND", "OrganOntology", "OrganOntologyRelationship", 
		"OrthologousGene", "OtherAnalyte", "OtherAnalyteAnalyteProcessingSteps", "OtherAnalyteOntologyEntry", 
		"OtherAnalyteProcessingSteps", "OtherAnalyteProcessingStepsOntologyEntry", "OtherBreastCancerHistologicGrade", "OtherBreastCancerHistopathologicGrade", 
		"OtherCause", "OtherCauseAttribution", "OtherChemicalAssociation", "OtherFunction", 
		"OtherFunctionalizingEntity", "OtherIonisation", "OtherIonisationOntologyEntry", "OtherMZAnalysis", 
		"OtherMZAnalysisOntologyEntry", "OtherNanoparticleEntity", "OtherProcedure", "OtherTarget", 
		"OtherTherapy", "Outcome", "OutcomeType", "Output", 
		"OutputFile", "OvarianDrugs", "OvarianExam", "OvarianNeoplasm", 
		"OvarianPathology", "OvarianSlide", "OvarianSurgery", "OxidativeBurst", 
		"OxidativeStress", "Package", "Parameter", "Parameterizable", 
		"ParameterizableApplication", "ParameterList", "ParameterValue", "Participant", 
		"ParticipantEligibilityAnswer", "ParticipantHistory", "ParticipantMedicalIdentifier", "Participation", 
		"ParticleComposition", "Party", "PartyRole", "Password", 
		"Pathology", "PathologyEventRecords", "PathologyReport", "Pathway", 
		"Patient", "PatientIdentifier", "PatientVisit", "Pdb", 
		"Pdbblast", "Peak", "PeakDetectionParameters", "PeakList", 
		"PeakLocation", "PeakSpecificChromint", "PedigreeGraph", "PedigreeNode", 
		"Peptide", "PeptideHit", "PeptideHitModifications", "PeptideHitOntologyEntry", 
		"PeptideHitProteinHit", "PeptideMembers", "PeptidesBase", "Percentile", 
		"PercentX", "PerformingLaboratory", "Period", "PeriodDelta", 
		"PermissibleValue", "Person", "person", "PersonContact", 
		"PersonName", "Personnel", "PersonOccupation", "PETEvaluation", 
		"PETEvaluationDetail", "Phagocytosis", "PharmaceuticalProduct", "Pharmacokinetics", 
		"Phase", "Phenomenon", "PhenomenonType", "Phenotype", 
		"PhenotypeDiagnosis", "PhysicalArrayDesign", "PhysicalBioAssay", "PhysicalCharacterization", 
		"PhysicalEntity", "PhysicalExam", "PhysicalLocation", "PhysicalParticipant", 
		"PhysicalPosition", "PhysicalProbe", "PhysicalState", "Physician", 
		"Place", "PlannedActivity", "PlannedActivityDelta", "PlannedCalendar", 
		"PlannedCalendarDelta", "PlannedEmailNotification", "PlannedNotification", "PlasmaProteinBinding", 
		"Platelet", "PlateletAggregation", "PloidyStruct", "Point", 
		"PolyAlleleFrequency", "PolyGenoFrequency", "Polyline", "Polymer", 
		"PolymerComposition", "Polymorphism", "PolynomialDegree", "PolypectomySpecimenCharacteristics", 
		"Population", "PopulationFrequency", "Portion", "Position", 
		"PositionDelta", "PositiveControl", "PostAdverseEventStatus", "PreExistingCondition", 
		"PreferredTerm", "Prep", "PreprocessDatasetParameterSet", "PresentAbsent", 
		"presentation", "PresentationThresholds", "PrimaryDiseasePresentation", "PrimarySiteSurgery", 
		"Primer", "PrimerPairs", "PrincipleComponentAnalysis", "PrincipleComponentAnalysisFinding", 
		"PriorTherapy", "PriorTherapyAgent", "Privilege", "ProbabilityMap", 
		"Probe", "ProbeGroup", "Procedure", "ProcedureDataFile", 
		"ProcedureDataType", "ProcedureEventParameters", "ProcedureSample", "ProcedureSampleType", 
		"ProcedureType", "ProcedureTypeProtocol", "ProcedureUnit", "Process", 
		"ProcessDataFile", "ProcessDetails", "ProcessEquipment", "ProcessLog", 
		"ProcessLogLimsFile", "PROcessParameter", "ProcessSample", "ProcessState", 
		"ProcessStatus", "ProcessType", "ProductInfused", "Profile", 
		"Project", "Project2Sample", "Project2SNP", "ProjectDataFile", 
		"Projection", "ProjectPersonnel", "ProjectProcedure", "ProjectReport", 
		"ProjectReportLimsFile", "ProjectSample", "Promoter", "Propeptide", 
		"properties", "Property", "property", "PropertyChange", 
		"PropertyDescriptor", "propertyId", "propertyLink", "propertyQualifier", 
		"propertyQualifierId", "PropertyValue", "ProstateAccessionCharacteristics", "ProstateAdditionalFindings", 
		"ProstateCancerTNMFinding", "ProstateSpecimenCharacteristics", "ProstateSurgicalPathologySpecimen", "ProtectionElement", 
		"ProtectionGroup", "ProtectionGroupRoleContext", "Protein", "Protein2MMDB", 
		"ProteinAlias", "ProteinBiomarker", "ProteinDige", "ProteinDomain", 
		"ProteinEncodingGeneFeature", "ProteinFeature", "ProteinGenomicIdentifier", "ProteinGroupMembers", 
		"ProteinGroups", "ProteinHit", "ProteinHomolog", "ProteinName", 
		"ProteinProphetFiles", "Proteins", "ProteinSequence", "ProteinSpotSet", 
		"ProteinStructure", "ProteinSubunit", "ProteomicsEventRecords", "Protocol", 
		"Protocol_package", "ProtocolAction", "ProtocolApplication", "ProtocolAssociation", 
		"ProtocolDefinition", "ProtocolFile", "ProtocolFormsSet", "ProtocolFormsTemplate", 
		"ProtocolLimsFile", "ProtocolStatus", "ProtocolStep", "ProtSequences", 
		"Provenance", "Publication", "PublicationSource", "PublicationStatus", 
		"PubMed", "Purity", "PValue", "QaReport", 
		"Quadrupole", "Qualifier", "QualitativeEvaluation", "Quantile", 
		"QuantitationType", "QuantitationTypeDimension", "QuantitationTypeMap", "QuantitationTypeMapping", 
		"Quantity", "QuantityInCount", "QuantityInGram", "QuantityInMicrogram", 
		"QuantityInMilliliter", "QuantityUnit", "QuantSummaries", "QuantumDot", 
		"QuantumDotComposition", "Query", "Question", "QuestionCondition", 
		"QuestionConditionComponents", "QuestionRepetition", "Radiation", "RadiationAdministration", 
		"RadiationAttribution", "RadiationIntervention", "RadiationTherapy", "RadicalProstatectomyGleasonHistologicGrade", 
		"RadicalProstatectomyGleasonHistopathologicGrade", "RadicalProstatectomyInvasiveProstateCarcinoma", "RadicalProstatectomyProstateNegativeSurgicalMargin", "RadicalProstatectomyProstatePositiveSurgicalMargin", 
		"RadicalProstatectomyProstateSpecimenCharacteristics", "RadicalProstatectomyProstateSurgicalMargin", "RadicalProstatectomyProstateSurgicalPathologySpecimen", "Ratio", 
		"RawArrayData", "RawSample", "RBC", "Reaction", 
		"ReceivedEventParameters", "Receptor", "Recipient", "RecipientDisease", 
		"RecipientLaboratoryFinding", "Recurrence", "ReexcisionCutaneousMelanomaSpecimenCharacteristics", "ReexcisionCutaneousMelanomaSurgicalPathologySpecimen", 
		"Reference", "ReferenceChemical", "ReferencedAnnotation", "ReferencedCalculation", 
		"ReferenceDocument", "ReferenceEntity", "ReferenceGene", "ReferenceLink", 
		"ReferenceProtein", "ReferenceRNA", "ReferenceSequence", "ReferenceSpotInformation", 
		"RefSeqmRNA", "RefSeqProtein", "RefseqProtein", "RegionalDistantSurgery", 
		"RegionalLymphNodeSurgery", "RegionalNodeLymphadenectomyCutaneousMelanomaSpecimenCharacteristics", "RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen", "Registration", 
		"Regulation", "Regulator", "RegulatoryElement", "RegulatoryElementType", 
		"RelatedGelItem", "RelatedGelItemProteinHit", "RelationshipObservation", "RelationshipType", 
		"RelativeLocation", "RelativeRecurringBlackout", "Remove", "RenderingDetail", 
		"Reorder", "Repeat", "Report", "ReportDefinition", 
		"ReportDelivery", "ReportDeliveryDefinition", "Reporter", "ReporterBasedAnalysis", 
		"ReporterCompositeMap", "ReporterDimension", "ReporterGroup", "ReporterPosition", 
		"ReportMandatoryFieldDefinition", "ReportPerson", "ReportSection", "ReportStatus", 
		"ReportVersion", "RepositoryInfo", "Representation", "ResearchInstitutionSource", 
		"ResearchStaff", "ResectionColonSpecimenCharacteristics", "Resolution", "ResolvedConceptReference", 
		"ResolvedConceptReferenceList", "ResponseAssessment", "ReviewEventParameters", "RFile", 
		"RNA", "RnaAnnotation", "RnaExperiment", "RnaReport", 
		"RnaResult", "Role", "RoleBasedRecipient", "RouteOfAdministration", 
		"RoutineAdverseEventReport", "RoutineOption", "Run", "RunProtocol", 
		"RunSampleContainer", "SAEReportPreExistingCondition", "SAEReportPriorTherapy", "Sample", 
		"SampleAnalyteProcessingSteps", "SampleCategory", "SampleComposition", "SampleContainer", 
		"SampleLocation", "SampleLog", "SampleManagement", "SampleOrigin", 
		"SamplePlate", "SampleProvider", "SampleSampleOrigin", "SampleSOP", 
		"SampleSOPFile", "SampleType", "Scalar", "ScannerProperties", 
		"Scheduled", "ScheduledActivity", "ScheduledActivityState", "ScheduledCalendar", 
		"ScheduledEmailNotification", "ScheduledEvent", "ScheduledNotification", "ScheduledStudySegment", 
		"ScheduledTimeLineEvent", "ScreeningResult", "SecondaryParticipantIdentifier", "SecondarySpecimenIdentier", 
		"SecondarySpecimenIdentifier", "SecondaryStructure", "Security", "SecurityGroup", 
		"Seg", "SegmentType", "Selenocysteine", "SemanticMetadata", 
		"SemanticType", "SeqFeature", "SeqFeatureLocation", "Sequence", 
		"SequenceAnnotation", "SequenceConflict", "SequencePosition", "SequenceVariant", 
		"SequestScores", "Series", "Sets", "SexDistribution", 
		"Shape", "ShortColumn", "ShortParameter", "ShortSequenceMotif", 
		"Signalp", "SignalPeptide", "Silo", "SimpleName", 
		"SimpleTimePeriod", "Site", "SiteInvestigator", "Size", 
		"SkyCase", "SkyCase_header", "skyCellHeader", "skyChromosome", 
		"SkyDataFile", "SkyDataFileSet", "skyFrag", "SkyMageMappings", 
		"Slide", "SmallMolecule", "SmallMoleculeEntity", "SNP", 
		"SNP2Allele", "SNP2Gene", "SNPAnalysisGroup", "SnpAnalysisResult", 
		"SnpAnnotation", "SNPAnnotation", "SnpArrayExperiment", "SNPArrayReporter", 
		"SNPAssay", "SNPAssociationAnalysis", "SNPAssociationFinding", "SNPCytogeneticLocation", 
		"snpFinding", "SNPFrequencyFinding", "SNPMapping", "SNPPanel", 
		"SNPPhysicalLocation", "SNPProbeAnnotation", "SnpResult", "SocialHistory", 
		"Software", "SoftwareApplication", "Solubility", "SomaticMutationFinding", 
		"SomCluster", "SomClusteringParameter", "SortContext", "SortDescription", 
		"SortDescriptionList", "SortOption", "SortOptionList", "Source", 
		"source", "SourceMeasurement", "SourceMeasurementProtocol", "SourceReference", 
		"Span", "SpatialCoordinate", "SpecializedQuantitationType", "Species", 
		"species", "SpeciesType", "SpecificDateBlackout", "Specimen", 
		"SpecimenAcquisition", "SpecimenArray", "SpecimenArrayContent", "SpecimenArrayType", 
		"SpecimenBasedAnalysis", "SpecimenBasedFinding", "SpecimenBasedMolecularFinding", "SpecimenCharacteristics", 
		"SpecimenCollection", "SpecimenCollectionGroup", "SpecimenEventParameters", "SpecimenProtocol", 
		"SpecimenRequirement", "SpectraData", "SpliceVariant", "SpontaneousMutation", 
		"Spot", "SpotAnalyteProcessingSteps", "SpotDetectionParameters", "SpotDige", 
		"SpotImage", "SpotMap", "SpotMapGroup", "SpotRatio", 
		"SpotSet", "SpotSetSpot", "SpunEventParameters", "StainingMethod", 
		"StandardQuantitationType", "StaticRelationship", "Status", "StorageContainer", 
		"StorageContainerCapacity", "StorageContainerDetails", "StorageDevice", "StorageElement", 
		"StorageType", "Strain", "Strand", "Strength", 
		"StringColumn", "StringParameter", "StructuralAlignment", "STS", 
		"Study", "StudyAgent", "StudyCoordinatingCenter", "StudyFundingSponsor", 
		"StudyInvestigator", "StudyObservation", "StudyOrganization", "StudyParticipant", 
		"StudyParticipantAssignment", "StudyPersonnel", "StudyProtocol", "StudyPublication", 
		"StudySegment", "StudySegmentDelta", "StudySite", "StudyStressor", 
		"StudySubject", "StudySubjectAssignment", "StudyTherapy", "StudyTimePoint", 
		"Subject", "SubjectAssignment", "SubjectGroup", "SubProject", 
		"SubstanceAdministration", "Subsystem", "SubsystemCell", "Summation", 
		"SuppGenotype", "SupportedElement", "SupportedElementList", "Surface", 
		"SurfaceChemistry", "SurfaceGroup", "Surgery", "SurgeryAttribution", 
		"SurgeryIntervention", "SurgeryTreatment", "SurgicalMargin", "SurgicalPathologySpecimen", 
		"Swissprot", "SynopticSurgicalPathologyReport", "SystemAssignedIdentifier", "SystemOrganClass", 
		"TaggingProcess", "TandemSequenceData", "Target", "TargetedModification", 
		"TargetingFunction", "Taxon", "TemperatureUnit", "Term", 
		"term", "term2term", "TermBasedCharacteristic", "TermSource", 
		"text", "TextAnnotation", "TextMeasurement", "ThawEventParameters", 
		"TherapeuticFunction", "TherapeuticProcedure", "Therapy", "ThreeDimensionalSize", 
		"ThreeDimensionalTumorSize", "ThreeDSpatialCoordinate", "Threshold", "Tile", 
		"TimeCourse", "TimeOfFlight", "TimePeriod", "TimePoint", 
		"TimeRecord", "TimeScaleUnit", "TimeSeries", "TimeUnit", 
		"Tissue", "TissueSpecimen", "TissueSpecimenRequirement", "TissueSpecimenReviewEventParameters", 
		"Tmhmm", "TopologicalDomain", "Topology", "TotalGenes", 
		"TotalProteinContent", "Toxicity", "Trace2Genotype", "TransanalDiskExcisionSpecimenCharacteristics", 
		"Transcript", "TranscriptAnnotation", "TranscriptArrayReporter", "TranscriptPhysicalLocation", 
		"TransferEventParameters", "Transformation", "Transgene", "TransitPeptide", 
		"TransmembraneRegion", "TransurethralResectionInvasiveProstateCarcinoma", "TransurethralResectionProstateSpecimenCharacteristics", "TransurethralResectionProstateSurgicalPathologySpecimen", 
		"TreatedAnalyte", "TreatedAnalyteAnalyteProcessingSteps", "Treatment", "TreatmentAssignment", 
		"TreatmentInformation", "TreatmentSchedule", "TreeNode", "TrialDataProvenance", 
		"TriggerAction", "tsBoolean", "tsCaseIgnoreDirectoryString", "tsCaseIgnoreIA5String", 
		"tsCaseSensitiveDirectoryString", "tsCaseSensitiveIA5String", "tsInteger", "tsTimestamp", 
		"tsURN", "TumorCode", "TumorSpecimenBiopsy", "Turn", 
		"TwoDLiquidChromatography1stDimension", "TwoDLiquidChromatography2ndDimension", "TwoDSpatialCoordinate", "TwoDSpotDatabaseSearch", 
		"TwoDSpotMassSpec", "TypeEnumerationMetadata", "UCUM", "UMLAssociationMetadata", 
		"UMLAttributeMetadata", "UMLClassMetadata", "UMLGeneralizationMetadata", "UMLPackageMetadata", 
		"UnclassifiedAgent", "UnclassifiedAgentTarget", "UnclassifiedLinkage", "UniGene", 
		"UnigeneTissueSource", "UniprotAccession", "UniProtKB", "UniprotkbAccession", 
		"Unit", "UnsureResidue", "URL", "URLSourceReference", 
		"User", "UserComments", "UserGroup", "UserOrganization", 
		"UserProtectionElement", "UserRoleContext", "ValidValue", "Value", 
		"ValueDomain", "ValueDomainPermissibleValue", "ValueDomainRelationship", "ValueMeaning", 
		"ValuesNearToCutoff", "VariationFinding", "VariationReporter", "version", 
		"versionable", "versionableAndDescribable", "versionReference", "Vocabulary", 
		"Volume", "VolumeUnit", "WebImageReference", "WebServicesSourceReference", 
		"WeekDayBlackout", "Window", "Xenograft", "XmlReport", 
		"XTandemScores", "YeastModel", "ZincFingerRegion", "Zone", 
		"ZoneDefect", "ZoneGroup", "ZoneLayout"
	};
	
}
