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
import com.tle.integration.oidc.service.OidcNonceService

import javax.inject.{Inject, Singleton}

object Lti13Nonce {
  val expiryInSeconds = 10
  val name            = "lti13-nonces"
}

/**
  * Manages the nonce values for LTI 1.3 Authentication processes where a nonce is valid for 10 seconds.
  */
@Bind
@Singleton
class Lti13NonceService @Inject()(rcs: ReplicatedCacheService)
    extends OidcNonceService(rcs, Lti13Nonce.name, Lti13Nonce.expiryInSeconds)
