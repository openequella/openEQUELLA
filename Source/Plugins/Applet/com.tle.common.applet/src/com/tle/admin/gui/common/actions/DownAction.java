package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class DownAction extends TLEAction
{
	public DownAction()
	{
		setIcon(DownAction.class, "down.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.downaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.downaction.desc"));
	}
}
