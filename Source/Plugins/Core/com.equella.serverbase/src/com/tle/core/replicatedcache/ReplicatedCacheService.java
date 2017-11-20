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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.tle.annotation.NonNull;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Pair;

/**
 * Strongly recommended that you use immutable objects so that you don't
 * accidentally make changes to your local cache without them being propagated.
 * 
 * @author nick
 */
@NonNullByDefault
public interface ReplicatedCacheService
{
	/**
	 * Retrieves or creates a new cache for the current institution by an
	 * identifier. Caches will automatically be deleted for institutions that
	 * are disabled or deleted.
	 * 
	 * @param cacheId unique identifier for this replicated cache.
	 * @param maxLocalCacheSize maximum number of values to be cache in-memory.
	 * @param ttl Time-to-live for cache values.
	 * @param ttlUnit units for ttl value.
	 */
	<V extends Serializable> ReplicatedCache<V> getCache(@NonNull String cacheId, long maxLocalCacheSize, long ttl,
		@NonNull TimeUnit ttlUnit);

	@NonNullByDefault
	public interface ReplicatedCache<V extends Serializable>
	{
		Optional<V> get(@NonNull String key);

		void put(@NonNull String key, @NonNull V value);

		void invalidate(@NonNull String... keys);

		Iterable<Pair<String, V>> iterate(String keyPrefixFilter);
	}
}
