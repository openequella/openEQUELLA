package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
public abstract class ArchiveAction extends TLEAction
{
	private static final long serialVersionUID = 1L;

	public ArchiveAction()
	{
		setIcon("/icons/archive.gif"); //$NON-NLS-1$
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.archiveaction.name")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.archiveaction.desc")); //$NON-NLS-1$
	}
}
