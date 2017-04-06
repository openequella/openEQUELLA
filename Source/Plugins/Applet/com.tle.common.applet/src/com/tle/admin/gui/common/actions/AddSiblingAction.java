package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class AddSiblingAction extends TLEAction
{
	@SuppressWarnings("nls")
	public AddSiblingAction()
	{
		setIcon("/icons/add.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.addsiblingaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.addsiblingaction.desc"));
	}
}
