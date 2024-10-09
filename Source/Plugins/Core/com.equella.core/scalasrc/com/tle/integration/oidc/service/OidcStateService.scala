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

package com.tle.integration.oidc.service

import com.tle.common.util.StringUtils.generateRandomHexString
import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

case class StateConfig(expiryInSeconds: Int, name: String)

/**
  * Manages the `state` values for OIDC authentication processes and stores the authentication
  * details in a replicated cache.
  */
@Bind
@Singleton
class OidcStateService[T <: Serializable](stateStorage: ReplicatedCache[T]) {

  def this(rcs: ReplicatedCacheService, config: StateConfig) =
    this(
      rcs.getCache[T](config.name, 1000, config.expiryInSeconds, TimeUnit.SECONDS),
    )

  @Inject def this(rcs: ReplicatedCacheService) = this(rcs, StateConfig(300, "oidc-state"))

  /**
    * Given the key details of an OIDC authentication Request, store those and create a 'state' value that can
    * later be used to retrieve these.
    *
    * @param details key information for an OIDC authentication request
    * @return a unique value representing this request which can be used as the `state` values
    *         in subsequent requests.
    */
  def createState(details: T): String = {
    val state = generateRandomHexString(16)
    stateStorage.put(state, details)

    state
  }

  /**
    * Given the state from a request, return what (if any) state details are stored against that
    * identifier.
    *
    * @param state the `state` value received from an OIDC authentication request.
    * @return The stored state details if available, or `None` if there are none - most likely
    *         indicating an invalid `state` value has been provided.
    */
  def getState(state: String): Option[T] = Option(stateStorage.get(state).orNull())

  /**
    * Given an `state` which is no longer required - already been used perhaps - invalidate it by
    * removing it from the state storage.
    *
    * @param state the `state` to be invalidated
    */
  def invalidateState(state: String): Unit = stateStorage.invalidate(state)
}
