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

/** Configuration for Auth0 including the common details for OIDC and the details required to
  * interact the Auth0 Management API V2.
  */
final case class Auth0(
    issuer: String,
    authCodeClientId: String,
    authCodeClientSecret: Option[String],
    authUrl: String,
    keysetUrl: String,
    tokenUrl: String,
    usernameClaim: Option[String],
    defaultRoles: Set[String],
    roleConfig: Option[RoleConfiguration],
    enabled: Boolean,
    apiUrl: String,
    apiClientId: String,
    apiClientSecret: Option[String]
) extends IdentityProvider
    with RestApi {
  override def platform: IdentityProviderPlatform.Value = IdentityProviderPlatform.AUTH0

  // todo: move to the case class parameter list when the work for Auth0 starts
  @JsonIgnore
  override val userIdAttribute: Option[String] = None

  /** In additional to the validations for common fields (see [[IdentityProvider.validate]]), also
    * validate the additional fields configured for a GenericIdentityProvider.
    */
  override def validate: ValidatedNel[String, Auth0] =
    (super.validate, validateApiDetails).mapN((_, _) => this)
}
