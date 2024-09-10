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
import com.tle.common.Check
import com.tle.common.settings.ConfigurationProperties
import java.net.{URI, URL}

/**
  * Supported Identity Provider platforms, including Azure, Cognito, Google, Okta.
  * Use Generic for other Platforms.
  */
object IdentityProviderPlatform extends Enumeration {
  val AZURE, COGNITO, GENERIC, GOOGLE, OKTA = Value
}

/**
  * Abstraction of an OIDC Identity Provider configuration to provide common fields.
  */
abstract class IdentityProvider extends ConfigurationProperties {

  /**
    * A non-empty string as the Identity Provider name
    */
  def name: String

  /**
    * One of the supported Identity Provider: [[IdentityProviderPlatform]]
    */
  def platform: IdentityProviderPlatform.Value

  /**
    * ID of an OAuth2 client registered in the selected Identity Provider
    */
  def clientId: String

  /**
    * Secret key associated with the OAuth2 client
    */
  def clientSecret: String

  /**
    * The URL used to initiate the OAuth2 authorisation process
    */
  def authUrl: String

  /**
    * The URL where the OAuth2 client's public keys are located
    */
  def keysetUrl: String

  /**
    * The URL used to obtain an access token from the selected Identity Provider
    */
  def tokenUrl: String

  /**
    * Custom claim used to retrieve a meaningful username from an ID token
    */
  def usernameClaim: Option[String]

  /**
    * Custom claim used to retrieve meaningful roles from an ID token
    */
  def roleClaim: Option[String]

  /**
    * A mapping between IdP roles and OEQ roles where one IdP role can map to multiple OEQ roles.
    * The mapped OEQ roles will be assigned to the user's session.
    */
  def customRoles: Map[String, Set[String]]

  /**
    * A list of OEQ roles to assign to the user's session for any IdP roles which are not in the list
    * of custom roles.
    */
  def unknownRoles: Set[String]

  /**
    * Whether the Identity Provider configuration is enabled
    */
  def enabled: Boolean
}

object IdentityProvider {

  /**
    * Helper function to accumulate errors captured during the validation for non-empty text fields.
    *
    * @param textFields A map of field names and their values
    */
  def validateTextFields(textFields: Map[String, String]): ValidatedNel[String, List[String]] =
    textFields
      .map {
        case (fieldName, value) =>
          Option
            .unless(Check.isEmpty(value))(value)
            .toValidNel(s"Missing value for required field: $fieldName")
      }
      .toList
      .sequence

  /**
    * Helper function to accumulate errors captured during the validation for URL fields.
    *
    * @param urlFields A map of field names and their values
    */
  def validateUrlFields(urlFields: Map[String, String]): ValidatedNel[String, List[URL]] =
    urlFields
      .map {
        case (fieldName, value) =>
          Either
            .catchNonFatal {
              new URI(value).toURL
            }
            .leftMap(err => s"Invalid value for $fieldName: ${err.getMessage}")
            .toValidatedNel
      }
      .toList
      .sequence

  /**
    * Validate the values of all the common fields configured for an IdentityProvider, and
    * return the IdentityProvider if the validation succeeds, or a list of errors captured
    * during the validation.
    */
  def validateCommonFields(idp: IdentityProvider): ValidatedNel[String, IdentityProvider] = {
    val textFields = Map(
      ("name", idp.name),
      ("client ID", idp.clientId),
      ("client Secret", idp.clientSecret),
    )

    val urlFields = Map(
      ("Auth URL", idp.authUrl),
      ("Key set URL", idp.keysetUrl),
      ("Token URL", idp.tokenUrl)
    )

    (validateTextFields(textFields), validateUrlFields(urlFields))
      .mapN((_, _) => idp)
  }
}
