package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

public abstract class RenameAction extends TLEAction
{
	public RenameAction()
	{
		setIcon("/icons/edit.gif"); //$NON-NLS-1$
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.renameaction.name")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.renameaction.desc")); //$NON-NLS-1$
	}
}
