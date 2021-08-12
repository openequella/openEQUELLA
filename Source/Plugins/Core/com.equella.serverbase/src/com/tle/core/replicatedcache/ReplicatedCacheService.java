/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.google.common.base.Optional;
import com.tle.annotation.NonNull;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Pair;
import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Strongly recommended that you use immutable objects so that you don't accidentally make changes
 * to your local cache without them being propagated.
 *
 * @author nick
 */
@NonNullByDefault
public interface ReplicatedCacheService {
  /**
   * Retrieves or creates a new cache for the current institution by an identifier. Caches will
   * automatically be deleted for institutions that are disabled or deleted.
   *
   * @param cacheId unique identifier for this replicated cache.
   * @param maxLocalCacheSize maximum number of values to be cache in-memory.
   * @param ttl Time-to-live for cache values.
   * @param ttlUnit units for ttl value.
   */
  <V extends Serializable> ReplicatedCache<V> getCache(
      @NonNull String cacheId, long maxLocalCacheSize, long ttl, @NonNull TimeUnit ttlUnit);

  /**
   * Similar to {@link #getCache(String, long, long, TimeUnit)}, but also allow persisting cache
   * values in DB.
   *
   * @param cacheId unique identifier for this replicated cache.
   * @param maxLocalCacheSize maximum number of values to be cache in-memory.
   * @param ttl Time-to-live for cache values.
   * @param ttlUnit units for ttl value.
   * @param alwaysPersist `true` to persist cache values in Database.
   */
  <V extends Serializable> ReplicatedCache<V> getCache(
      @NonNull String cacheId,
      long maxLocalCacheSize,
      long ttl,
      @NonNull TimeUnit ttlUnit,
      boolean alwaysPersist);

  @NonNullByDefault
  interface ReplicatedCache<V extends Serializable> {
    Optional<V> get(@NonNull String key);

    void put(@NonNull String key, @NonNull V value);

    /**
     * Similar to { @link #put(String, Serializable)} }, but also allow specifying TTL of the value
     * saved in DB to support values that have longer or shorter TTL.
     *
     * @param key Key of the object
     * @param value Value of the object
     * @param ttl TTL of the value saved in DB
     */
    default void put(@NonNull String key, @NonNull V value, Instant ttl) {
      throw new UnsupportedOperationException();
    }

    void invalidate(@NonNull String... keys);

    Iterable<Pair<String, V>> iterate(String keyPrefixFilter);
  }
}
