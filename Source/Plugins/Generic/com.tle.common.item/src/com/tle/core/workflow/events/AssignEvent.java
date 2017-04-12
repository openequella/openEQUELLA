/*
 * Created on Apr 21, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author jmaginnis
 */
public class AssignEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	private static final String ICON = "icons/comment.gif"; //$NON-NLS-1$

	public AssignEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
