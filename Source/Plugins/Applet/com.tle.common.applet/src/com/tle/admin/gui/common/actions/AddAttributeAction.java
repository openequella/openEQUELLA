package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class AddAttributeAction extends TLEAction
{
	@SuppressWarnings("nls")
	public AddAttributeAction()
	{
		setIcon("/icons/add.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.addattributeaction.name"));
		putValue(Action.SHORT_DESCRIPTION,
			CurrentLocale.get("com.tle.admin.gui.common.actions.addattributeaction.desc"));
	}
}
