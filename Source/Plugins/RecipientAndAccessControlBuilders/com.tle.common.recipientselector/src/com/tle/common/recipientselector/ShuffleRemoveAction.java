package com.tle.common.recipientselector;

import javax.swing.Action;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class ShuffleRemoveAction extends TLEAction
{
	@SuppressWarnings("nls")
	public ShuffleRemoveAction()
	{
		putValue(Action.NAME, "<");
		putValue(Action.SHORT_DESCRIPTION,
			CurrentLocale.get("com.tle.admin.recipients.actions.shuffleremoveaction.desc"));
	}
}
