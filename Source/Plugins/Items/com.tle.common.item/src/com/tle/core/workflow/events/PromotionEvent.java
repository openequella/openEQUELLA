/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class PromotionEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public PromotionEvent(HistoryEvent event)
	{
		super(event);
	}

	public boolean isApplies()
	{
		return event.isApplies();
	}

	public void setApplies(boolean applies)
	{
		event.setApplies(applies);
	}
}
