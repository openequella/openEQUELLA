package com.tle.core.institution.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.institution.InstitutionCache;

public class InstitutionCacheImpl<T> implements InstitutionCache<T>
{
	private final LoadingCache<Institution, T> cache;

	public InstitutionCacheImpl(CacheLoader<Institution, T> loader)
	{
		cache = CacheBuilder.newBuilder().build(loader);
	}

	@Override
	public synchronized T getCache()
	{
		return getCache(getInstitution());
	}

	@Override
	public synchronized T getCache(Institution inst)
	{
		Preconditions.checkNotNull(inst);
		return cache.getUnchecked(inst);
	}

	@Override
	public void clear()
	{
		clear(getInstitution());
	}

	@Override
	public void clear(Institution inst)
	{
		Preconditions.checkNotNull(inst);
		cache.invalidate(inst);
	}

	private Institution getInstitution()
	{
		Institution rv = CurrentInstitution.get();
		if( rv == null )
		{
			rv = Institution.FAKE;
		}
		return rv;
	}
}