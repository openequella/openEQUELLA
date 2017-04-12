package com.tle.web.template.section;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class AbstractCachedTopbarLink implements TopbarLink
{
	private Cache<String, Integer> countCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
		.softValues().build();

	public int getCachedValue()
	{
		Integer count = countCache.getIfPresent(getSessionKey());
		if( count == null )
		{
			count = getCount();
			countCache.put(getSessionKey(), new Integer(count));
		}
		return count;
	}

	public void clearCachedCount()
	{
		countCache.invalidate(getSessionKey());
	}

	public abstract int getCount();

	public abstract String getSessionKey();
}
