package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast;

import java.util.List;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.Invoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;

public interface NCBIBlastInvoker extends Invoker<NCBIBlastInput, org.jdom.Document> {

	public List<SequenceDatabase> getDatabases() throws InvokerException;
	
}
