package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class CloneAction extends TLEAction
{
	@SuppressWarnings("nls")
	public CloneAction()
	{
		setIcon("/icons/clone.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.cloneaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.cloneaction.desc"));
	}
}
