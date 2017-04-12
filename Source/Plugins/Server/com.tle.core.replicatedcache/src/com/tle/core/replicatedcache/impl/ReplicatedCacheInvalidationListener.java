package com.tle.core.replicatedcache.impl;

import com.tle.annotation.NonNull;
import com.tle.core.events.listeners.ApplicationListener;

public interface ReplicatedCacheInvalidationListener extends ApplicationListener
{
	void invalidateCacheEntries(@NonNull String cacheId, @NonNull String... keys);
}
