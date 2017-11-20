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

package com.tle.core.replicatedcache;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;

/**
 * Caches changes in-memory only, and only pushes them to the "global" cache
 * when you call <code>flush()</code>. This works really well when you have a
 * cache that is updated very frequently and you don't care that the data isn't
 * replicated immediately, or that you only care about the last value of a key
 * that's been updated multiple times since the last flush. For example, this is
 * used when keeping track of user sessions access timestamps so that we don't
 * hit the DB for every access, but instead get batched up and push in one hit.
 * 
 * @author Nick Read
 */
public class BatchedCache<V extends Serializable> implements ReplicatedCache<V>
{
	private final ReplicatedCache<V> delegate;
	private final InstitutionCache<Map<String, V>> additions;
	private final InstitutionCache<Set<String>> removals;

	private long autoFlushDuration = -1;
	private Date lastFlush = new Date();

	public BatchedCache(InstitutionService service, ReplicatedCache<V> delegate)
	{
		this.delegate = delegate;

		additions = service.newInstitutionAwareCache(new CacheLoader<Institution, Map<String, V>>()
		{
			@Override
			public Map<String, V> load(Institution key) throws Exception
			{
				return Maps.newConcurrentMap();
			}
		});

		removals = service.newInstitutionAwareCache(new CacheLoader<Institution, Set<String>>()
		{
			@Override
			public Set<String> load(Institution key) throws Exception
			{
				return Sets.newConcurrentHashSet();
			}
		});
	}

	public synchronized void flush()
	{
		// Invalidate any removals first
		final Set<String> rs = removals.getCache();
		delegate.invalidate(rs.toArray(new String[rs.size()]));
		rs.clear();

		// Set the changes
		final Map<String, V> as = additions.getCache();
		for( Entry<String, V> entry : as.entrySet() )
		{
			delegate.put(entry.getKey(), entry.getValue());
		}
		as.clear();

		lastFlush = new Date();
	}

	/**
	 * Automatically flush when putting or invalidating if it's been more than
	 * the given time period.
	 */
	public synchronized void setAutoFlush(long duration, TimeUnit unit)
	{
		autoFlushDuration = unit.toMillis(duration);
	}

	private synchronized void checkAutoFlush()
	{
		if( autoFlushDuration < 0 )
		{
			return;
		}

		if( System.currentTimeMillis() > lastFlush.getTime() + autoFlushDuration )
		{
			flush();
		}

	}

	@Override
	public Optional<V> get(String key)
	{
		checkNotNull(key);

		V v = additions.getCache().get(key);
		if( v != null )
		{
			return Optional.of(v);
		}
		else if( removals.getCache().contains(key) )
		{
			return Optional.absent();
		}
		else
		{
			return delegate.get(key);
		}
	}

	@Override
	public void put(String key, V value)
	{
		checkNotNull(key);
		checkNotNull(value);

		removals.getCache().remove(key);
		additions.getCache().put(key, value);

		checkAutoFlush();
	}

	public void putAndReplicateNow(String key, V value)
	{
		delegate.put(key, value);
	}

	@Override
	public void invalidate(String... keys)
	{
		for( String key : keys )
		{
			removals.getCache().add(key);
			additions.getCache().remove(key);
		}

		checkAutoFlush();
	}

	@Override
	public Iterable<Pair<String, V>> iterate(final String keyPrefixFilter)
	{
		// First we iterate over the locally added stuff
		Iterable<Entry<String, V>> entrySet = additions.getCache().entrySet();

		// ...filtering out based on the key prefix...
		if( !Check.isEmpty(keyPrefixFilter) )
		{
			entrySet = Iterables.filter(entrySet, new Predicate<Entry<String, V>>()
			{
				@Override
				public boolean apply(@Nullable Entry<String, V> input)
				{
					return input.getKey().startsWith(keyPrefixFilter);
				}
			});
		}

		// ...and transform it into Pairs
		Iterable<Pair<String, V>> addIter = Iterables.transform(entrySet,
			new Function<Entry<String, V>, Pair<String, V>>()
			{
				@Override
				@Nullable
				public Pair<String, V> apply(@Nullable Entry<String, V> input)
				{
					return new Pair<>(input.getKey(), input.getValue());
				}
			});

		// Then another iterator over the delegate entries...
		Iterable<Pair<String, V>> delegateIter = delegate.iterate(keyPrefixFilter);

		// ...but filter out anything that we've removed or updated locally
		delegateIter = Iterables.filter(delegateIter, new Predicate<Pair<String, V>>()
		{
			final Set<String> rs = removals.getCache();
			final Map<String, V> as = additions.getCache();

			@Override
			public boolean apply(@Nullable Pair<String, V> input)
			{
				final String key = input.getFirst();
				return !rs.contains(key) && !as.containsKey(key);
			}
		});

		// Now add the two iterators together and...
		return Iterables.concat(addIter, delegateIter);
	}
}
