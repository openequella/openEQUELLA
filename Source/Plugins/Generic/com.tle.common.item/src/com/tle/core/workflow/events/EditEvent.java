/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class EditEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public static final String ICON = "icons/edit.gif"; //$NON-NLS-1$

	public EditEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
