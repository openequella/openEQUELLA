package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author aholland
 */
public class NewVersionEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public NewVersionEvent(HistoryEvent event)
	{
		super(event);
	}
}
