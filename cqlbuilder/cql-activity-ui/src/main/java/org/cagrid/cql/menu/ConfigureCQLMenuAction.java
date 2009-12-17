package org.cagrid.cql.menu;

import javax.swing.Action;

import org.cagrid.cql.CQLActivity;
import org.cagrid.cql.actions.CQLActivityConfigurationAction;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class ConfigureCQLMenuAction extends
		AbstractConfigureActivityMenuAction<CQLActivity> {

	private static final String CONFIGURE_STRINGCONSTANT = "Edit value";

	public ConfigureCQLMenuAction() {
		super(CQLActivity.class);
	}
	
	@Override
	protected Action createAction() {
		CQLActivityConfigurationAction configAction = new CQLActivityConfigurationAction(
				findActivity(), getParentFrame());
		configAction.putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}


}
