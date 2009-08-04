package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.common;

import javax.xml.namespace.QName;


/**
 * This class is autogenerated, DO NOT EDIT
 *
 * @created by Introduce Toolkit version 1.3
 */
public interface NCBIBlastJobConstantsBase {
	public static final String SERVICE_NS = "http://www.mygrid.org.uk/2009/cagrid/servicewrapper/service/NCBIBlast/Job";
	public static final QName RESOURCE_KEY = new QName(SERVICE_NS, "NCBIBlastJobKey");
	public static final QName RESOURCE_PROPERTY_SET = new QName(SERVICE_NS, "NCBIBlastJobResourceProperties");
	public static final QName NCBIBLASTINPUT = new QName("gme://Taverna-caGrid.caBIG/1.0/uk.org.mygrid.cagrid.domain.ncbiblast", "NCBIBlastInput");
	public static final QName NCBIBLASTOUTPUT = new QName("gme://Taverna-caGrid.caBIG/1.0/uk.org.mygrid.cagrid.domain.ncbiblast", "NCBIBlastOutput");
	public static final QName JOBSTATUS = new QName("gme://Taverna-caGrid.caBIG/1.0/uk.org.mygrid.cagrid.domain.common", "JobStatus");
	public static final QName FAULT = new QName("gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.service", "Fault");
	public static final QName CURRENTTIME = new QName("http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", "CurrentTime");
	public static final QName TERMINATIONTIME = new QName("http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd", "TerminationTime");
	public static final QName JOBID = new QName("gme://Taverna-caGrid.caBIG/1.0/uk.org.mygrid.cagrid.domain.common", "JobID");
	public static final QName EBIAPPLICATIONRESULT = new QName("http://www.ebi.ac.uk/schema", "EBIApplicationResult");
	public static final QName EBIAPPLICATIONERROR = new QName("http://www.ebi.ac.uk/schema", "EBIApplicationError");
	
}
