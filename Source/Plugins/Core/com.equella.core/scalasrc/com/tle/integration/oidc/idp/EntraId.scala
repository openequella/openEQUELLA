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
import com.tle.integration.oidc.idp.IdentityProvider.{validateTextFields, validateUrlFields}

/**
  * Configuration for Microsoft Entra ID including the common details for OIDC and the details required
  * to interact the Graph REST API.
  */
final case class EntraId(
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
    apiClientId: String,
    apiClientSecret: Option[String],
) extends IdentityProvider
    with RestApi {
  override def platform: IdentityProviderPlatform.Value = IdentityProviderPlatform.ENTRA_ID

  override val apiUrl: String = "https://graph.microsoft.com/v1.0"

  override def validate: ValidatedNel[String, EntraId] =
    (super.validate, validateApiDetails).mapN((_, _) => this)
}
