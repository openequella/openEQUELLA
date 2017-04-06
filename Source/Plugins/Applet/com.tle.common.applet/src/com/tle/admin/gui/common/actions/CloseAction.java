package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class CloseAction extends TLEAction
{
	@SuppressWarnings("nls")
	public CloseAction()
	{
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.closeaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.closeaction.desc"));
	}
}
