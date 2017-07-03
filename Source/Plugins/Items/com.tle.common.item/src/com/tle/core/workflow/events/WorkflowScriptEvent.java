package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class WorkflowScriptEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public WorkflowScriptEvent(HistoryEvent event)
	{
		super(event);
	}
}
