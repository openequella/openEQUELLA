package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class OkAction extends TLEAction
{
	public OkAction()
	{
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.okaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.okaction.desc"));
	}
}
