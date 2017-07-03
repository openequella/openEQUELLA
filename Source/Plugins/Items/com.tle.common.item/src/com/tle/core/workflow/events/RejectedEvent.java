/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class RejectedEvent extends CommentEvent
{
	private static final long serialVersionUID = 1L;

	public RejectedEvent(HistoryEvent event)
	{
		super(event);
	}
}
