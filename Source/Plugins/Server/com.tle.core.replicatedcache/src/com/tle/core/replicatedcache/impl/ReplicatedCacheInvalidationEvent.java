package com.tle.core.replicatedcache.impl;

import com.tle.annotation.NonNull;
import com.tle.core.events.ApplicationEvent;

public final class ReplicatedCacheInvalidationEvent extends ApplicationEvent<ReplicatedCacheInvalidationListener>
{
	private final String cacheId;
	private final String[] keys;

	public ReplicatedCacheInvalidationEvent(@NonNull String cacheId, @NonNull String... keys)
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);

		this.cacheId = cacheId;
		this.keys = keys;
	}

	@Override
	public Class<ReplicatedCacheInvalidationListener> getListener()
	{
		return ReplicatedCacheInvalidationListener.class;
	}

	@Override
	public void postEvent(ReplicatedCacheInvalidationListener listener)
	{
		listener.invalidateCacheEntries(cacheId, keys);
	}

	@Override
	public boolean requiresInstitution()
	{
		return true;
	}
}
