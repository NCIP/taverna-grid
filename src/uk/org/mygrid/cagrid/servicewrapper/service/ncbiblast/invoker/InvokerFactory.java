package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.DummyNCBIBlastInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvoker;

public class InvokerFactory {
	private static NCBIBlastInvoker invoker;

	public static NCBIBlastInvoker getInvoker() {
		if (invoker == null) {
			synchronized (InvokerFactory.class) {
				if (invoker == null) {
					invoker = new DummyNCBIBlastInvoker();
					//invoker = new NCBIBlastInvokerImpl();
				}
			}
		}
		return invoker;
	}
}
