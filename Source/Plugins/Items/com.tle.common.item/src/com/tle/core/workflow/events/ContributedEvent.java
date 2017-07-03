package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author aholland
 */
public class ContributedEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public ContributedEvent(HistoryEvent event)
	{
		super(event);
	}
}
