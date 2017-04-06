package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class SearchAction extends TLEAction
{
	@SuppressWarnings("nls")
	public SearchAction()
	{
		setIcon("/icons/search2.gif");
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.searchaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.searchaction.desc"));
	}
}
