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

package com.tle.integration.lti13

import com.tle.common.util.StringUtils.generateRandomHexString
import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache

import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

/**
  * Details for an entry being stored in state.
  *
  * @param platformId The ID of the platform which this state is linked to. (VALIDATION)
  * @param loginHint The initial login hint for the connection. (VALIDATION - maybe)
  * @param targetLinkUri The intended link to be launched as per the initial request. Once
  *                      authentication is complete, this link should be redirected to.
  */
@SerialVersionUID(1)
case class Lti13StateDetails(platformId: String, loginHint: String, targetLinkUri: URI)
    extends Serializable

/**
  * Manages the `state` values for LTI 1.3 Authentication processes.
  */
@Bind
@Singleton
class Lti13StateService(stateStorage: ReplicatedCache[Lti13StateDetails]) {

  @Inject def this(rcs: ReplicatedCacheService) = {
    // Not keen on the idea of having a limit on the number of cache entries, but that's the way
    // RCS works. And although we set a TTL of 10 seconds, that's only until first access. After
    // that it's been hard coded to last for 1 day!
    // (See com.tle.core.replicatedcache.impl.ReplicatedCacheServiceImpl.ReplicatedCacheImpl.ReplicatedCacheImpl)
    this(
      rcs
        .getCache[Lti13StateDetails]("lti13-state", 1000, 10, TimeUnit.SECONDS))
  }

  /**
    * Given the key details of an LTI Request, store those and create a 'state' value that can
    * later be used to retrieve these.
    *
    * @param details key information for an LTI request
    * @return a unique value representing this request which can be used as the `state` values
    *         in subsequent requests.
    */
  def createState(details: Lti13StateDetails): String = {
    val state = generateRandomHexString(16)
    stateStorage.put(state, details)

    state
  }

  /**
    * Given the state from a request, return what (if any) state details are stored against that
    * identifier.
    *
    * @param state the `state` value received from an LTI request.
    * @return The stored state details if available, or `None` if there are none - most likely
    *         indicating an invalid `state` value has been provided.
    */
  def getState(state: String): Option[Lti13StateDetails] = Option(stateStorage.get(state).orNull())

  /**
    * Given an `state` which is no longer required - already been used perhaps - invalidate it by
    * removing it from the state storage.
    *
    * @param state the `state` to be invalidated
    */
  def invalidateState(state: String): Unit = stateStorage.invalidate(state)
}
