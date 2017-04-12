package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class EditAction extends TLEAction
{
	@SuppressWarnings("nls")
	public EditAction()
	{
		setIcon("/icons/edit.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.editaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.editaction.desc"));
	}
}
