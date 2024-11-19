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
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.tle.common.Check
import com.tle.common.settings.ConfigurationProperties
import com.tle.integration.oidc.idp.IdentityProvider.{validateTextFields, validateUrlFields}
import java.net.{URI, URL}

/**
  * Supported Identity Provider platforms, including MS Entra ID, Okta and Generic for other Platforms.
  */
object IdentityProviderPlatform extends Enumeration {
  val ENTRA_ID, GENERIC, OKTA = Value
}

/**
  * Configuration for custom role claim and mappings between IdP roles and OEQ roles.
  *
  * @param roleClaim Custom claim used to retrieve meaningful roles from an ID token
  * @param customRoles A mapping between IdP roles and OEQ roles where one IdP role can map to multiple OEQ roles.
  *                    The mapped OEQ roles will be assigned to the user's session.
  */
case class RoleConfiguration(roleClaim: String, customRoles: Map[String, Set[String]])

/**
  * Abstraction of an OIDC Identity Provider configuration to provide common fields, typically used with REST endpoints
  * where a looser type is required to support de-serialisation. For example, Secret values are required in the integration,
  * but they are optional here because the values may not be provided by client for update.
  *
  * In order to support polymorphic deserialization of the IdentityProvider, Jackson annotations `@JsonTypeInfo`
  * and `@JsonSubTypes` are used to specify the type discriminator and subtypes.
  */
@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "platform")
@JsonSubTypes(
  Array(
    new Type(value = classOf[GenericIdentityProvider], name = "GENERIC"),
    new Type(value = classOf[EntraId], name = "ENTRA_ID"),
  ))
abstract class IdentityProvider extends ConfigurationProperties with Product {

  /**
    * One of the supported Identity Provider: [[IdentityProviderPlatform]]
    */
  def platform: IdentityProviderPlatform.Value

  /**
    * The issuer identifier for the OpenID Connect provider. This value should match the 'iss'
    * claim in the JWTs issued by this provider.
    */
  def issuer: String

  /**
    * ID of an OAuth2 client registered in the selected Identity Provider, used specifically in
    * the Authorization Code flow
    */
  def authCodeClientId: String

  /**
    * Secret key used specifically in the Authorization Code flow
    */
  def authCodeClientSecret: Option[String]

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
    * A list of default OEQ roles to assign to the user's session.
    */
  def defaultRoles: Set[String]

  /**
    * Optional configuration for custom roles assigned to the user's session. If None, use the default roles.
    */
  def roleConfig: Option[RoleConfiguration]

  /**
    * Whether the Identity Provider configuration is enabled
    */
  def enabled: Boolean

  /**
    * Validate the values of all the common fields configured for an IdentityProvider, and
    * return the IdentityProvider if the validation succeeds, or a list of errors captured
    * during the validation.
    */
  def validate: ValidatedNel[String, IdentityProvider] = {
    val textFields = Map(
      ("Issuer", issuer),
      ("Authorisation Code flow Client ID", authCodeClientId),
    )

    val urlFields = Map(
      ("Auth URL", authUrl),
      ("Key set URL", keysetUrl),
      ("Token URL", tokenUrl)
    )

    (validateTextFields(textFields), validateUrlFields(urlFields))
      .mapN((_, _) => this)
  }
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
}
