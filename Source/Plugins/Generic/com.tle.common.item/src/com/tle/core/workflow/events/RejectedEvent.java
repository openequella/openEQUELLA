/*
 * Created on Jun 28, 2004
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

public class RejectedEvent extends CommentEvent
{
	private static final long serialVersionUID = 1L;

	private static final String ICON = "icons/error.gif"; //$NON-NLS-1$

	public RejectedEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
