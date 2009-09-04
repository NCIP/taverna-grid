package org.cagrid.transfer.menu;

import javax.swing.Action;

import org.cagrid.transfer.*;
import org.cagrid.transfer.actions.CaGridTransferActivityConfigurationAction;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class ConfigureCaGridTransferMenuAction extends
		AbstractConfigureActivityMenuAction<CaGridTransferActivity> {

	//private static final String CONFIGURE_STRINGCONSTANT = "Edit value";

	public ConfigureCaGridTransferMenuAction() {
		super(CaGridTransferActivity.class);
	}
	
	@Override
	protected Action createAction() {
		CaGridTransferActivityConfigurationAction configAction = new CaGridTransferActivityConfigurationAction(
				findActivity(), getParentFrame());
		//configAction.putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}


}
