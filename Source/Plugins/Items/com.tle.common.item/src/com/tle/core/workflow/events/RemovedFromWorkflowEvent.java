package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class RemovedFromWorkflowEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public RemovedFromWorkflowEvent(HistoryEvent event)
	{
		super(event);
	}
}
