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

package com.tle.core.institution.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;

/**
 * An implementation of {@link InstitutionCache} which is backed by a {@link LoadingCache} instance
 * so that when a cache for an institution is requested for the first time it is created as per the
 * {@link CacheLoader} provided in the constructor. Through this {@code CacheLoader} much
 * customisation is possible - especially the type of keys and values stored. (Note, the {@code
 * CacheLoader} provided is typically - perhaps always - expected to actually return a {@link
 * com.google.common.cache.Cache} instance. It is not a loader of the values for the keys you are
 * planning to use in a cache.)
 *
 * <p>An example use is:
 *
 * <pre>{@code
 * InstitutionCache<Cache<String,String>> instCache = new InstitutionCacheImpl(
 *   CacheLoader.from(
 *     (inst) =>
 *       CacheBuilder
 *         .newBuilder()
 *         .maximumSize(100)
 *         .expireAfterAccess(1, TimeUnit.DAYS)
 *         .build()));
 *
 *  // Then to use it
 *  String someKey = ???
 *  String someValue = instCache.getCache().get(someKey, () => loader to load missing values);
 * }</pre>
 *
 * Note that from the {@code instCache} first you must get the backing cache with {@code getCache}
 * and then you use that cache as you normally would.
 *
 * <p>Note that the typical way to instantiate these is via {@link
 * InstitutionService#newInstitutionAwareCache(CacheLoader)}.
 *
 * <p>(The reason for using a {@code Cache} instance as the storage for this instead of a simple
 * ConcurrentHashMap - which is effectively what is used here seeing there is no configuration on
 * the cache - is lost as the original creator has long since moved on. So the guess is it was done
 * to leverage the additional configuration options that are possible with the loader. But that's
 * only a guess unfortunately.)
 *
 * <p>
 *
 * @param <T> The type for the backing cache, such as {@code Cache<K, V>} (with {@code K} and {@code
 *     V} properly substituted for the types you wish to cache.
 */
public class InstitutionCacheImpl<T> implements InstitutionCache<T> {
  private final LoadingCache<Institution, T> cache;

  public InstitutionCacheImpl(CacheLoader<Institution, T> loader) {
    cache = CacheBuilder.newBuilder().build(loader);
  }

  @Override
  public synchronized T getCache() {
    return getCache(getInstitution());
  }

  @Override
  public synchronized T getCache(Institution inst) {
    Preconditions.checkNotNull(inst);
    return cache.getUnchecked(inst);
  }

  @Override
  public void clear() {
    clear(getInstitution());
  }

  @Override
  public void clear(Institution inst) {
    Preconditions.checkNotNull(inst);
    cache.invalidate(inst);
  }

  private Institution getInstitution() {
    Institution rv = CurrentInstitution.get();
    if (rv == null) {
      rv = Institution.FAKE;
    }
    return rv;
  }
}
