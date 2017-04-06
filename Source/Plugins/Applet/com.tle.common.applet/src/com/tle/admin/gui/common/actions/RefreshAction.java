package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class RefreshAction extends TLEAction
{
	@SuppressWarnings("nls")
	public RefreshAction()
	{
		setIcon("/icons/refresh.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.refreshaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.refreshaction.desc"));
	}
}
