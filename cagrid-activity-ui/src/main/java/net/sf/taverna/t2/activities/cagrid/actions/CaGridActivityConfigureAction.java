package net.sf.taverna.t2.activities.cagrid.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.cagrid.CaGridActivity;
import net.sf.taverna.t2.activities.cagrid.CaGridActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class CaGridActivityConfigureAction extends ActivityConfigurationAction<CaGridActivity,CaGridActivityConfigurationBean> {

	private final Frame owner;
	private static Logger logger = Logger
			.getLogger(CaGridActivityConfigureAction.class);

	public CaGridActivityConfigureAction(CaGridActivity activity,Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		CaGridActivityConfigurationBean configurationBean = getActivity().getConfiguration();
	}

}
