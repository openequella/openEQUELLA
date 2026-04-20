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

package com.tle.core.institution;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;

/**
 * A container of caches specific to institutions, facilitating the caching of values partitioned to
 * an institution.
 *
 * @param <T> the type of the cache which will be stored, typically of a signature along the lines
 *     of {@code Cache<K, V>}.
 */
public interface InstitutionCache<T> {

  /**
   * Will attempt to return the cache of the current institution (with semantics based on {@code
   * CurrentInstitution.get()}), or otherwise use the place holder institution of {@code
   * Institution.FAKE}.
   *
   * @return a cache inline with the above contract.
   */
  T getCache();

  /**
   * Returns the cache for the specified {@code Institution}. Use instead of {@code getCache()} if
   * greater control is required.
   *
   * @param inst the institution who's cache is required
   * @return a cache for the specified institution
   */
  T getCache(Institution inst);

  /**
   * Completely clears all data in the current institution's (based on sematics like {@code
   * getCache()}) cache.
   */
  void clear();

  /**
   * Completely clears all data for the specified institutions cache. Use if more control over
   * institution determination is required than found in {@code clear()}.
   *
   * @param institution the institution of the target cache to clear
   */
  void clear(Institution institution);

  /**
   * Creates a cache invalidation callback that is safe to execute after transaction commit.
   *
   * <p>The current {@link Institution} is captured at the time this method is called, when {@link
   * CurrentInstitution#get()} is guaranteed to return the correct value. The returned callback uses
   * this captured reference directly, avoiding any dependency on {@code CurrentInstitution}
   * ThreadLocal state during execution.
   *
   * <p>This is important because the ThreadLocal context may have been cleared by the time the
   * callback runs, which could otherwise lead to incorrect cache invalidation (such as falling back
   * to {@link Institution#FAKE}).
   *
   * @return a {@link Runnable} that invalidates the cache for the captured institution.
   */
  Runnable createCacheInvalidationCallback();
}
