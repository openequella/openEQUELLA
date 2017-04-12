package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class AddAction extends TLEAction
{
	public AddAction()
	{
		setIcon(AddAction.class, "add.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.addaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.addaction.desc"));
	}
}