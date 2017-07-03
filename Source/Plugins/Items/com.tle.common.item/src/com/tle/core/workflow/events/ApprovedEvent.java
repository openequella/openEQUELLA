/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class ApprovedEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public ApprovedEvent(HistoryEvent event)
	{
		super(event);
	}
}
