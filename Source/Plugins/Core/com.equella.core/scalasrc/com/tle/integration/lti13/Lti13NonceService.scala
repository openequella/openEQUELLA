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

import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

object NonceExpiry {
  val inSeconds = 10
}

/**
  * Provides the concrete details behind a nonce to be used for validation.
  *
  * @param state the state representing the session for which this nonce can be used
  * @param timestamp the timestamp of when this was generated, so that it can be checked for currency
  */
@SerialVersionUID(1)
case class Lti13NonceDetails(state: String, timestamp: Instant) extends Serializable

/**
  * Manages the nonce values for LTI 1.3 Authentication processes.
  */
@Bind
@Singleton
class Lti13NonceService(nonceStorage: ReplicatedCache[Lti13NonceDetails]) {

  @Inject def this(rcs: ReplicatedCacheService) {
    // Not keen on the idea of having a limit on the number of cache entries, but that's the way
    // RCS works. And although we set a TTL of 10 seconds, that's only until first access. After
    // that it's been hard coded to last for 1 day!
    // (See com.tle.core.replicatedcache.impl.ReplicatedCacheServiceImpl.ReplicatedCacheImpl.ReplicatedCacheImpl)
    this(
      rcs
        .getCache[Lti13NonceDetails]("lti13-nonces", 1000, NonceExpiry.inSeconds, TimeUnit.SECONDS))
  }

  /**
    * Given an existing `state` value, will create a unique `nonce` value and store it in a
    * replicated datastore against the current timestamp. Allowing for both a time window and the
    * state (representing a session) to be used at validation time.
    *
    * @param state the `state` value of an existing session
    * @return a new unique nonce
    */
  def createNonce(state: String): String = {
    val nonce = generateRandomHexString(16)
    // TODO: Check if nonce already in use, if so generate another - but only do for x times before
    //       giving up and failing.
    nonceStorage.put(nonce, Lti13NonceDetails(state, Instant.now()))

    nonce
  }

  /**
    * Validates the provided `nonce` by checking:
    *
    * 1. That it still exists in the nonce store
    * 2. It is being used within a suitable time
    * 3. It was registered against the provided `state`
    *
    * At completion, if all is valid it will remove the `nonce` as it has effectively then been used
    * _once_.
    *
    * @param nonce the `nounce` value to validate
    * @param state the state to which the it is expected the `nonce` was registered against
    * @return true if valid, false otherwise
    */
  def validateNonce(nonce: String, state: String): Either[String, Boolean] = {
    def notExpired(ts: Instant) = {
      val validUntil = ts.plusSeconds(NonceExpiry.inSeconds)
      Instant
        .now()
        .isBefore(validUntil)
    }

    val valid = Option(nonceStorage.get(nonce).orNull()) match {
      // Could do a few matches to capture errors around expired, doesn't match state, etc.
      case Some(nonceDetails: Lti13NonceDetails)
          if nonceDetails.state == state && notExpired(nonceDetails.timestamp) =>
        nonceStorage.invalidate(nonce)
        Right(true)
      case Some(nonceDetails: Lti13NonceDetails) if nonceDetails.state == state =>
        // cases with matching timestamp would've been above, so this means it had expired
        Left("Provided nonce has expired")
      case Some(_: Lti13NonceDetails) =>
        // Lastly, this means we have found some the specified nonce - but it's for the wrong session
        Left("Provided nonce does not match the 'state' for this request")
      case None => Left("Provided nonce does not exist")
    }

    valid
  }
}
