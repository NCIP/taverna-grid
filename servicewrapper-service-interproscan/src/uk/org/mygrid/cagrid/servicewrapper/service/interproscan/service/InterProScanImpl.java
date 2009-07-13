package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service;

import java.rmi.RemoteException;

import org.globus.wsrf.ResourceKey;

import uk.ac.ebi.www.wsinterproscan.Data;
import uk.ac.ebi.www.wsinterproscan.InputParams;
import uk.org.mygrid.cagrid.domain.common.FASTANucleotideSequence;
import uk.org.mygrid.cagrid.domain.common.FASTAProteinSequence;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.NucleotideSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceIdentifier;
import uk.org.mygrid.cagrid.domain.common.ProteinSequenceRepresentation;
import uk.org.mygrid.cagrid.domain.common.SequenceRepresentation;
import uk.org.mygrid.cagrid.domain.interproscan.InterProScanInputParameters;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker.InvokerFactory;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResource;
import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResourceHome;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;
import uk.org.mygrid.cagrid.valuedomains.SignatureMethod;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanImpl extends InterProScanImplBase {

	private InterProScanInvoker invoker = InvokerFactory.getInvoker();
	
	public InterProScanImpl() throws RemoteException {
		super();
	}

  public uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference interProScan(uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput interProScanInput) throws RemoteException {

		// TODO: Submit job

		InterProScanInputParameters origParams = interProScanInput
				.getInterProScanInputParameters();

		uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput input = new uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInput();
		InputParams params = uk.ac.ebi.www.wsinterproscan.InputParams.Factory
				.newInstance();
		if (params == null) {
			throw new RemoteException("Parameters required");
		}
		if (origParams.getEmail() == null) {
			throw new RemoteException("Email required");
		}
		params.setEmail(origParams.getEmail());
		if (origParams.getUseCRC() != null) {
			params.setCrc(origParams.getUseCRC());
		}
		StringBuffer apps = new StringBuffer();
		if (origParams.getSignatureMethod() != null) {
			for (SignatureMethod signatureMethod : origParams
					.getSignatureMethod()) {
				if (apps.length() > 0) {
					apps.append(' ');
				}
				apps.append(signatureMethod.getValue());
			}
		}
		if (apps.length() > 0) {
			params.setApp(apps.toString());
		}
		input.setParams(params);

		SequenceRepresentation seqRep = interProScanInput
				.getSequenceRepresentation();
		if (seqRep == null) {
			throw new RemoteException("Sequence representation required");
		}
		if (seqRep instanceof ProteinSequenceRepresentation) {
			params.setSeqtype("P");
		} else if (seqRep instanceof NucleotideSequenceRepresentation) {
			params.setSeqtype("N");
		} else {
			throw new RemoteException(
					"Unsupported sequence representation type "
							+ seqRep.getClass());
		}

		Data[] content = new Data[1];
		content[0] = Data.Factory.newInstance();

		if (seqRep instanceof FASTANucleotideSequence) {
			content[0].setContent(((FASTANucleotideSequence) seqRep)
					.getSequence());
			content[0].setType("sequence");
		} else if (seqRep instanceof FASTAProteinSequence) {
			content[0]
					.setContent(((FASTAProteinSequence) seqRep).getSequence());
			content[0].setType("sequence");
		} else if (seqRep instanceof ProteinSequenceIdentifier) {
			content[0].setContent(((ProteinSequenceIdentifier) seqRep)
					.getSequenceId());
			content[0].setType("sequence"); // oddly enough..
		} else if (seqRep instanceof NucleotideSequenceIdentifier) {
			content[0].setContent(((NucleotideSequenceIdentifier) seqRep)
					.getSequenceId());
			content[0].setType("sequence"); // oddly enough..
		} else {
			throw new RemoteException(
					"Unsupported sequence representation type "
							+ seqRep.getClass());
		}
		input.setContent(content);

		String jobID;
		try {
			jobID = invoker.runJob(input);
		} catch (InvokerException e1) {
			throw new RemoteException("Can't submit job", e1);
		}
		System.out.println("Submitted " + jobID);

		InterProScanJobResourceHome resourceHome;
		try {
			resourceHome = getInterProScanJobResourceHome();
			ResourceKey resourceKey = resourceHome.createResource();
			final InterProScanJobResource resource = resourceHome
					.getResource(resourceKey);
			resource.setInterProScanInput(interProScanInput);
			resource.setJobID(jobID);
			return resourceHome.getResourceReference(resourceKey);
		} catch (Exception e) {
			throw new RemoteException("Could not create job resource", e);
		}
	}

}
