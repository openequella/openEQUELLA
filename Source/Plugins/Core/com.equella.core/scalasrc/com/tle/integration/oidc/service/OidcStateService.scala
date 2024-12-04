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

import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.integration.oauth2.OAuth2StateService

import javax.inject.{Inject, Singleton}

/** Details of an entry stored in state for an OIDC authentication request.
  *
  * @param codeVerifier
  *   The code verifier used by an authorisation server to verify the code challenge
  * @param codeChallenge
  *   A transformed version of the code verifier sent to the authorisation server along with the
  *   transformation method being `S256`
  * @param targetPage
  *   OEQ page the user was attempting to access before the login process
  */
final case class OidcStateDetails(
    codeVerifier: String,
    codeChallenge: String,
    targetPage: Option[String]
)

/** Manages the `state` values for OIDC Authentication processes where a state is valid for 300
  * seconds.
  */
@Bind
@Singleton
class OidcStateService @Inject() (rcs: ReplicatedCacheService)
    extends OAuth2StateService[OidcStateDetails](rcs)
