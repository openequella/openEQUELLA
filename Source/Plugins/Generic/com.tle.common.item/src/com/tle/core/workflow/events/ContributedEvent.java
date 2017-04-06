package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author aholland
 */
public class ContributedEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;
	private static final String ICON = "icons/edit.gif"; //$NON-NLS-1$

	public ContributedEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
