package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service;

import uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterproScanWrappedJobResource;
import  uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service.InterproScanWrappedConfiguration;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.apache.axis.MessageContext;
import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;


/** 
 * DO NOT EDIT:  This class is autogenerated!
 *
 * Provides some simple accessors for the Impl.
 * 
 * @created by Introduce Toolkit version 1.3
 * 
 */
public abstract class InterproScanWrappedJobImplBase {
	
	public InterproScanWrappedJobImplBase() throws RemoteException {
	
	}
	
	public InterproScanWrappedConfiguration getConfiguration() throws Exception {
		return InterproScanWrappedConfiguration.getConfiguration();
	}
	
	
	public uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterproScanWrappedJobResourceHome getResourceHome() throws Exception {
		ResourceHome resource = getResourceHome("home");
		return (uk.org.mygrid.cagrid.servicewrapper.service.interproscan.job.service.globus.resource.InterproScanWrappedJobResourceHome)resource;
	}

	
	
	
	public uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service.globus.resource.InterproScanWrappedResourceHome getInterproScanWrappedResourceHome() throws Exception {
		ResourceHome resource = getResourceHome("interproScanWrappedHome");
		return (uk.org.mygrid.cagrid.servicewrapper.service.interproscan.service.globus.resource.InterproScanWrappedResourceHome)resource;
	}
	
	
	protected ResourceHome getResourceHome(String resourceKey) throws Exception {
		MessageContext ctx = MessageContext.getCurrentContext();

		ResourceHome resourceHome = null;
		
		String servicePath = ctx.getTargetService();

		String jndiName = Constants.JNDI_SERVICES_BASE_NAME + servicePath + "/" + resourceKey;
		try {
			javax.naming.Context initialContext = new InitialContext();
			resourceHome = (ResourceHome) initialContext.lookup(jndiName);
		} catch (Exception e) {
			throw new Exception("Unable to instantiate resource home. : " + resourceKey, e);
		}

		return resourceHome;
	}


}
