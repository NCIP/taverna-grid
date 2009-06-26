package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service;

import java.rmi.RemoteException;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceKey;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterProScanJobResourceHome;

/**
 * TODO:I am the service side implementation class. IMPLEMENT AND DOCUMENT ME
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public class InterProScanImpl extends InterProScanImplBase {

	public InterProScanImpl() throws RemoteException {
		super();
	}

  public uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.stubs.types.InterProScanJobReference interProScan(uk.org.mygrid.cagrid.domain.interproscan.InterProScanInput interProScanInput) throws RemoteException {
		
		// TODO: Submit job
		
		
		InterProScanJobResourceHome resourceHome;
		try {
			resourceHome = getInterProScanJobResourceHome();
			ResourceKey resourceKey = resourceHome.createResource();
			return resourceHome.getResourceReference(resourceKey);
		} catch (Exception e) {
			throw new RemoteException("Could not create job resource", e);
		}
	}

}
