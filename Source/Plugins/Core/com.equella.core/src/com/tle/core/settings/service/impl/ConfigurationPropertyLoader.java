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

package com.tle.core.settings.service.impl;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.cache.CacheLoader;
import com.tle.core.guice.Bind;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles transactional database access for configuration property loading.
 *
 * <p>This class exists as a separate Spring-managed bean so that its {@link Transactional} methods
 * are invoked through the Spring AOP proxy. Calling {@code @Transactional} methods from within the
 * same class instance bypasses the proxy, meaning no transaction is started. By extracting the
 * database-loading logic here and injecting this bean into {@link ConfigurationServiceImpl}, the
 * call crosses the proxy boundary and the transaction is correctly activated.
 */
@Bind
@Singleton
public class ConfigurationPropertyLoader {

  /**
   * Loads a value from the database within a Spring-managed transaction.
   *
   * @param property the property key (may be {@code null} when the loader is class-keyed)
   * @param loader the Guava {@link CacheLoader} that performs the actual database query
   * @param <T> the type of value to load
   * @return the loaded value, or {@code null} if none exists
   */
  @Transactional
  public <T> T loadFromDb(String property, CacheLoader<String, T> loader) {
    try {
      return loader.load(property);
    } catch (Exception e) {
      throw new RuntimeApplicationException("Could not create config object", e);
    }
  }
}
