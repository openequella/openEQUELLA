package com.tle.admin.gui.common.actions;

import javax.swing.Action;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class PreviewAction extends TLEAction
{
	@SuppressWarnings("nls")
	public PreviewAction()
	{
		putValue(Action.NAME, CurrentLocale.get("com.tle.admin.gui.common.actions.previewaction.name"));
		putValue(Action.SHORT_DESCRIPTION, CurrentLocale.get("com.tle.admin.gui.common.actions.previewaction.desc"));
	}
}
