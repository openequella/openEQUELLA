package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class CancelAction extends TLEAction
{
	public CancelAction()
	{
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.cancelaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.cancelaction.desc"));
	}
}
