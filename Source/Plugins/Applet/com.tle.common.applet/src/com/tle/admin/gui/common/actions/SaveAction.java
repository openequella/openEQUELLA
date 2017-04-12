package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class SaveAction extends TLEAction
{
	@SuppressWarnings("nls")
	public SaveAction()
	{
		setIcon("/icons/save.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.saveaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.saveaction.desc"));
	}
}
