package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.events.AbstractSectionEvent;

/**
 * @author Aaron
 */
public abstract class AbstractMyPagesEvent<L extends EventListener> extends AbstractSectionEvent<L>
{
	private final String sessionId;

	protected AbstractMyPagesEvent(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public String getSessionId()
	{
		return sessionId;
	}
}
