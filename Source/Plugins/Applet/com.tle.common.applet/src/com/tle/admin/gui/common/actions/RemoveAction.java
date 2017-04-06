package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class RemoveAction extends TLEAction
{
	public RemoveAction()
	{
		setIcon(RemoveAction.class, "remove.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.removeaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.removeaction.desc"));
	}
}
