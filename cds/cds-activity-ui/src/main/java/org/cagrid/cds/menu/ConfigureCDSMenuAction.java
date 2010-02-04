package org.cagrid.cds.menu;

import javax.swing.Action;

import org.cagrid.cds.actions.CDSActivityConfigurationAction;
import org.cagrid.cds.*;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class ConfigureCDSMenuAction extends
		AbstractConfigureActivityMenuAction<CDSActivity> {

	private static final String CONFIGURE_STRINGCONSTANT = "Edit Function";

	public ConfigureCDSMenuAction() {
		super(CDSActivity.class);
	}
	
	@Override
	protected Action createAction() {
		CDSActivityConfigurationAction configAction = new CDSActivityConfigurationAction(
				findActivity(), getParentFrame());
		configAction.putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}


}
