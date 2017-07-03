package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author aholland
 */
public class CloneEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public CloneEvent(HistoryEvent event)
	{
		super(event);
	}
}
