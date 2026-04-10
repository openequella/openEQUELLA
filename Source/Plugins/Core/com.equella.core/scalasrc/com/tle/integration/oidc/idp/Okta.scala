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

package com.tle.integration.oidc.idp

import cats.data.ValidatedNel
import cats.implicits._
import com.fasterxml.jackson.annotation.JsonIgnore

/** Configuration for Okta including the common details for OIDC and the details required to
  * interact the Core Okta API.
  */
case class Okta(
    issuer: String,
    authCodeClientId: String,
    authCodeClientSecret: Option[String],
    authUrl: String,
    keysetUrl: String,
    tokenUrl: String,
    usernameClaim: Option[String],
    defaultRoles: Set[String],
    roleConfig: Option[RoleConfiguration],
    userIdAttribute: Option[String],
    enabled: Boolean,
    apiUrl: String,
    apiClientId: String
) extends IdentityProvider
    with RestApi {
  override def platform: IdentityProviderPlatform.Value = IdentityProviderPlatform.OKTA

  /** Okta doesn't support the request of a scoped access token through client secret. Instead, the
    * 'private_key_jwt' client authentication method is the only supported method. As a result, set
    * the client secret to None.
    *
    * Reference links:
    *   - https://developer.okta.com/docs/guides/implement-oauth-for-okta-serviceapp/main/#generate-the-jwk-using-the-api
    *   - https://oauth.net/private-key-jwt/
    */
  override val apiClientSecret: Option[String] = None

  override def validate: ValidatedNel[String, Okta] =
    (super.validate, validateApiDetails).mapN((_, _) => this)
}
