package com.tle.core.events;

import com.tle.core.events.listeners.UMPChangedListener;

/**
 * @author Nicholas Read
 */
public class UMPChangedEvent extends ApplicationEvent<UMPChangedListener>
{
	private static final long serialVersionUID = 1L;

	private final String purgeIdFromCaches;
	private final boolean groupPurge;

	public UMPChangedEvent()
	{
		this(null, false);
	}

	public UMPChangedEvent(String purgeIdFromCaches, boolean groupPurge)
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.purgeIdFromCaches = purgeIdFromCaches;
		this.groupPurge = groupPurge;
	}

	public String getPurgeIdFromCaches()
	{
		return purgeIdFromCaches;
	}

	public boolean isGroupPurge()
	{
		return groupPurge;
	}
	
	@Override
	public Class<UMPChangedListener> getListener()
	{
		return UMPChangedListener.class;
	}

	@Override
	public void postEvent(UMPChangedListener listener)
	{
		listener.umpChangedEvent(this);
	}
}
