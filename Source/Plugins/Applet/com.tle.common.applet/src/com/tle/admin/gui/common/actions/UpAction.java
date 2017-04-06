package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class UpAction extends TLEAction
{
	public UpAction()
	{
		setIcon(UpAction.class, "up.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.upaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.upaction.desc"));
	}
}
