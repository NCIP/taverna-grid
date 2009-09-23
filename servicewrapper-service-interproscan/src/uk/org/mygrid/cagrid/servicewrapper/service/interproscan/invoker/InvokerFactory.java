package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvokerImpl;

public class InvokerFactory {
	private static InterProScanInvoker invoker;

	public static InterProScanInvoker getInvoker() {
		if (invoker == null) {
			synchronized (InvokerFactory.class) {
				if (invoker == null) {
					//invoker = new DummyInterProScanInvoker();
					try {
						invoker = new InterProScanInvokerImpl();
					} catch (InvokerException e) {
						throw new RuntimeException("Can't instantiate InterProScan invoker", e);
					}
				}
			}
		}
		return invoker;
	}
}
