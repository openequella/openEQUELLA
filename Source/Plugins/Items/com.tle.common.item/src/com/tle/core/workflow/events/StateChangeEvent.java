/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.ItemStatus;

public class StateChangeEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public StateChangeEvent(HistoryEvent event)
	{
		super(event);
	}

	public ItemStatus getState()
	{
		return event.getState();
	}

	public void setState(ItemStatus state)
	{
		this.event.setState(state);
	}
}
