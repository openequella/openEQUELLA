package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public abstract class UnarchiveAction extends TLEAction
{
	private static final long serialVersionUID = 1L;

	public UnarchiveAction()
	{
		setIcon("/icons/unarchive.gif"); //$NON-NLS-1$
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.name")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.desc")); //$NON-NLS-1$
	}
}