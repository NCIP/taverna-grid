package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.invoker;

import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.DummyInterProScanInvoker;
import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.interproscan.InterProScanInvoker;

public class InvokerFactory {
	private static InterProScanInvoker invoker;

	public static InterProScanInvoker getInvoker() {
		if (invoker == null) {
			synchronized (InvokerFactory.class) {
				if (invoker == null) {
					invoker = new DummyInterProScanInvoker();
					//invoker = new InterProScanInvoker();
				}
			}
		}
		return invoker;
	}
}
