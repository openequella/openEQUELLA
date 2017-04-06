/*
 * Created on Apr 22, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.events;

import com.tle.beans.item.HistoryEvent;

/**
 * @author jmaginnis
 */
public class ResetEvent extends WorkflowEvent
{
	private static final long serialVersionUID = 1L;

	public static final String ICON = "icons/promotion.gif"; //$NON-NLS-1$

	public ResetEvent(HistoryEvent event)
	{
		super(event);
	}

	@Override
	public String getIcon()
	{
		return ICON;
	}
}
