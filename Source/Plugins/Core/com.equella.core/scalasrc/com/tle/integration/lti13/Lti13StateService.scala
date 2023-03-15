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

import java.net.URI
import scala.collection.concurrent.TrieMap

/**
  * Details for an entry being stored in state.
  *
  * @param platformId The ID of the platform which this state is linked to. (VALIDATION)
  * @param loginHint The initial login hint for the connection. (VALIDATION - maybe)
  * @param targetLinkUri The intended link to be launched as per the initial request. Once
  *                      authentication is complete, this link should be redirected to.
  */
case class Lti13StateDetails(platformId: String, loginHint: String, targetLinkUri: URI)

/**
  * Manages the `state` values for LTI 1.3 Authentication processes.
  */
object Lti13StateService {
  // TODO: Replace with ReplicatedCacheService - this should have a short TTL too (perhaps 10s)
  private val stateStorage: TrieMap[String, Lti13StateDetails] = TrieMap()

  def createState(details: Lti13StateDetails): String = {
    val state = {
      // TODO: Lookup the recommendation in OAuth 2 course on creation of `state`
      // This is potentially not secure enough, but I don't really want to bind it to the host
      // with extra crypto unless necessary. (Although one option would be to use the private
      // from the server's JWK...)
      generateRandomHexString(16)
    }
    stateStorage.addOne((state, details))

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
  def getState(state: String): Option[Lti13StateDetails] = stateStorage.get(state)

  /**
    * Given an `state` which is no longer required - already been used perhaps - invalidate it by
    * removing it from the state storage.
    *
    * @param state the `state` to be invalidated
    */
  def invalidateState(state: String): Unit = stateStorage.remove(state)
}

object TestLti13StateService extends App {
  val state = Lti13StateService.createState(
    Lti13StateDetails("http://moodle.local", "1", new URI("http://moodle.local/auth.php")))

  println(s"State: ${state}")
  println(s"Details: ${Lti13StateService.getState(state)}")
  Lti13StateService.invalidateState(state)
  println(s"Details after invalidate: ${Lti13StateService.getState(state)}")
}
