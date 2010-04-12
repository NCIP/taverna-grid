package net.sf.taverna.cagrid.activity;

import java.util.List;

import org.apache.log4j.Logger;
import org.globus.gsi.GlobusCredential;
import org.ietf.jgss.GSSCredential;

import net.sf.taverna.cagrid.wsdl.parser.WSDLParser;

public class CaGridServiceWSDLSOAPInvoker extends CaGridWSDLSOAPInvoker{

	private static Logger logger = Logger.getLogger(CaGridServiceWSDLSOAPInvoker.class);
	private CaGridActivityConfigurationBean configurationBean;



	public CaGridServiceWSDLSOAPInvoker(WSDLParser parser,
			CaGridActivityConfigurationBean bean, List<String> outputNames,
			String wsrfEndpointReference) {
		super(parser, bean, outputNames, wsrfEndpointReference);
		this.configurationBean = bean;
	}
	
	public GSSCredential getGSSCredential(String cred) throws Exception{
		
		GlobusCredential proxy = new GlobusCredential(cred); 
		GSSCredential gss = null;
        try {
			gss = new org.globus.gsi.gssapi.GlobusGSSCredentialImpl(proxy,GSSCredential.INITIATE_AND_ACCEPT);
			logger.info("Created GSSCredential from the proxy for operation " + configurationBean.getOperation());
		} catch (org.ietf.jgss.GSSException ex) {
			logger
			.error("Error occured while creating GSSCredential from the user's proxy for invoking operation "
					+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl());
			ex.printStackTrace();
			throw new Exception("Error occured while creating GSSCredential from the user's proxy for invoking operation "
					+ configurationBean.getOperation() + " of service " + configurationBean.getWsdl(), ex);
		}
		return gss;

	}
	
	public CaGridServiceWSDLSOAPInvoker create()
	{
		return null;
		
	}

}
