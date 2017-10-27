/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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