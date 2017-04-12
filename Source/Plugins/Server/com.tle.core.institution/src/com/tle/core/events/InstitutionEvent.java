package com.tle.core.events;

import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.core.events.listeners.InstitutionListener;

/**
 * @author Nicholas Read
 */
public class InstitutionEvent extends ApplicationEvent<InstitutionListener>
{
	public enum InstitutionEventType
	{
		AVAILABLE, UNAVAILABLE, DELETED, EDITED, STATUS
	}

	private static final long serialVersionUID = 1L;
	private Multimap<Long, Institution> changes;
	private final InstitutionEventType eventType;

	public InstitutionEvent(InstitutionEventType eventType, Multimap<Long, Institution> changes)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.eventType = eventType;
		this.changes = changes;
	}

	@Override
	public Class<InstitutionListener> getListener()
	{
		return InstitutionListener.class;
	}

	@Override
	public void postEvent(InstitutionListener listener)
	{
		listener.institutionEvent(this);
	}

	/**
	 * @return A map of database schema IDs to modified institutions
	 */
	public Multimap<Long, Institution> getChanges()
	{
		return changes;
	}

	public void setChanges(Multimap<Long, Institution> changes)
	{
		this.changes = changes;
	}

	public InstitutionEventType getEventType()
	{
		return eventType;
	}
}
