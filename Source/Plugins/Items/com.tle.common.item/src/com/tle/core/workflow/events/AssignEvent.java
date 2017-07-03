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

	public AssignEvent(HistoryEvent event)
	{
		super(event);
	}
}
