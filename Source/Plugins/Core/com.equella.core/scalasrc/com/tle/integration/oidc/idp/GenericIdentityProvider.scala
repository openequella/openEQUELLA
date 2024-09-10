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
  * @param userListingUrl Url used to retrieve a list of users from the Identity Provider
  * @param clientCredClientId ID of an OAuth2 client which MUST support Client Credentials grant
  * @param clientCredClientSecret Secret key associated with the OAuth2 client
  */
case class GenericIdentityProvider(
    name: String,
    clientId: String,
    clientSecret: String,
    authUrl: String,
    keysetUrl: String,
    tokenUrl: String,
    usernameClaim: Option[String],
    roleClaim: Option[String],
    unknownRoles: Set[String],
    customRoles: Map[String, Set[String]],
    enabled: Boolean,
    userListingUrl: String,
    clientCredClientId: String,
    clientCredClientSecret: String,
) extends IdentityProvider {
  override def platform: IdentityProviderPlatform.Value = IdentityProviderPlatform.GENERIC
}

object GenericIdentityProvider {

  /**
    * Validate special fields configured for an GenericIdentityProvider, and return the
    * GenericIdentityProvider if the validation succeeds, or a list of errors captured
    * during the validation.
    */
  def validate(idp: GenericIdentityProvider): ValidatedNel[String, GenericIdentityProvider] = {
    val textFields = Map(
      ("Client Credentials Client ID", idp.clientCredClientId),
      ("Client Credentials Client secret", idp.clientCredClientSecret),
    )
    val urlField = Map(("User listing URL", idp.userListingUrl))

    (validateTextFields(textFields), validateUrlFields(urlField))
      .mapN((_, _) => idp)
  }
}
