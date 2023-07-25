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
}
