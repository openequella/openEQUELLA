/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class ApprovedEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;
	public static final String ICON = "icons/valid.gif"; //$NON-NLS-1$

	public ApprovedEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
