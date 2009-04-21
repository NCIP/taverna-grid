package net.sf.taverna.t2.activities.cagrid.views;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class CaGridActivityViewFactory implements ContextualViewFactory<CaGridActivity>{

	public boolean canHandle(Object object) {
		return object instanceof CaGridActivity;
	}

	public ActivityContextualView<?> getView(CaGridActivity activity) {
		return new CaGridActivityContextualView(activity);
	}
	
}
