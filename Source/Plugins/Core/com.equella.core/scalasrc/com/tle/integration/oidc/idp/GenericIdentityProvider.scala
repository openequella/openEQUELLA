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
  * Configuration for a generic Identity Provider. In addition to the common fields, it includes
  * the following special fields:
  *
  * @param apiUrl The API endpoint for the Identity Provider, use for operations such as search for users
  * @param apiClientId Client ID used to get an Authorisation Token to use with the Identity Provider's API
  *                           (for user searching etc)
  * @param apiClientSecret Client Secret used with `apiClientId` to get an Authorization Token
  *                               to use with the Identity Provider's API (for user searching etc)
  */
case class GenericIdentityProvider(
    name: String,
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
    apiClientSecret: Option[String],
) extends IdentityProvider {
  override def platform: IdentityProviderPlatform.Value = IdentityProviderPlatform.GENERIC

  /**
    * In additional to the validations for common fields (see [[IdentityProvider.validate]]), also
    * validate the additional fields configured for a GenericIdentityProvider.
    */
  override def validate: ValidatedNel[String, GenericIdentityProvider] = {
    val textFields = Map(("IdP API Client ID", apiClientId))
    val urlField   = Map(("IdP API URL", apiUrl))

    (super.validate, validateTextFields(textFields), validateUrlFields(urlField))
      .mapN((_, _, _) => this)
  }
}
