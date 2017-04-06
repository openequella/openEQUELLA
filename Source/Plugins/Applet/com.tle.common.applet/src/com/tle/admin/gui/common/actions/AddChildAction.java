package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class AddChildAction extends TLEAction
{
	@SuppressWarnings("nls")
	public AddChildAction()
	{
		setIcon("/icons/add.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.addchildaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.addchildaction.desc"));
	}
}
