package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author aholland
 */
public class MoveEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public MoveEvent(HistoryEvent event)
	{
		super(event);
	}
}