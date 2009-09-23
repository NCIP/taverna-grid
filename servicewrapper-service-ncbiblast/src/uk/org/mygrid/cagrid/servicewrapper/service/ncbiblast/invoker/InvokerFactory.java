package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.DummyNCBIBlastInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvokerImpl;

public class InvokerFactory {
	private static NCBIBlastInvoker invoker;

	public static NCBIBlastInvoker getInvoker() {
		if (invoker == null) {
			synchronized (InvokerFactory.class) {
				if (invoker == null) {
//					invoker = new DummyNCBIBlastInvoker();
					try {
						invoker = new NCBIBlastInvokerImpl();
					} catch (InvokerException e) {
						throw new RuntimeException("Can't instantiate NCBIBlast invoker", e);
					}
				}
			}
		}
		return invoker;
	}
}
