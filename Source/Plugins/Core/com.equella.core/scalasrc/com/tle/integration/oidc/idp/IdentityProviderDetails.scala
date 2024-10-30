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

import cats.data.Validated.invalidNel
import cats.implicits._
import com.tle.core.encryption.EncryptionService

import java.net.{URI, URL}

/**
  * The common details configured for SSO with an Identity Provider, but with some slightly more strict types
  * (e.g. secret values are mandatory and use `URL` instead of `String` for URL type values) compared to `IdentityProvider`
  * which needed to be looser for REST endpoints etc. Also, secret values are encrypted.
  *
  * @param platform One of the supported Identity Provider: [[IdentityProviderPlatform]]
  * @param issuer The issuer identifier for the OpenID Connect provider. This value should match the 'iss'
  *               claim in the JWTs issued by this provider.
  * @param authCodeClientId ID of an OAuth2 client registered in the selected Identity Provider, used specifically in
  *                         the Authorization Code flow
  * @param authCodeClientSecret Secret key used with `authCodeClientId` specifically in the Authorization Code flow
  * @param authUrl The URL used to initiate the OAuth2 authorisation process
  * @param keysetUrl The URL used to initiate the OAuth2 authorisation process
  * @param tokenUrl The URL used to obtain an access token from the selected Identity Provider
  * @param usernameClaim Custom claim used to retrieve a meaningful username from an ID token
  * @param defaultRoles A list of default OEQ roles to assign to the user's session.
  * @param roleConfig Optional configuration for custom roles assigned to the user's session. If None, use the default roles.
  */
case class CommonDetails(platform: IdentityProviderPlatform.Value,
                         issuer: String,
                         authCodeClientId: String,
                         authCodeClientSecret: String,
                         authUrl: URL,
                         keysetUrl: URL,
                         tokenUrl: URL,
                         usernameClaim: Option[String],
                         defaultRoles: Set[String],
                         roleConfig: Option[RoleConfiguration],
                         enabled: Boolean,
)

sealed trait IdentityProviderDetails {
  val commonDetails: CommonDetails
}

/**
  * Configuration details for a generic Identity Provider. In addition to the common configuration for SSO,
  * the details of how to interact with the Identity Provider's APIs are also included.
  *
  * @param commonDetails Common details configured for SSO with a generic Identity Provider
  * @param apiUrl The API endpoint for the Identity Provider, use for operations such as search for users
  * @param apiClientId Client ID used to get an Authorisation Token to use with the Identity Provider's API
  *                    (for user searching etc)
  * @param apiClientSecret Client Secret used with `apiClientId` to get an Authorization Token to use with
  *                        the Identity Provider's API (for user searching etc). The value will be encrypted on saving.
  */
case class GenericIdentityProviderDetails(
    commonDetails: CommonDetails,
    apiUrl: URL,
    apiClientId: String,
    apiClientSecret: String,
) extends IdentityProviderDetails

object IdentityProviderDetails {

  private var encryptionService: EncryptionService = _

  private def encrypt(field: String,
                      newValue: Option[String],
                      existingConfig: Option[String]): Either[String, String] =
    newValue
      .filter(_.nonEmpty)
      .orElse(existingConfig)
      .map(encryptionService.encrypt)
      .toRight(s"Missing value for required field: $field")

  // If the existing config is the same type return an Option of the config; otherwise return None.
  private def existingIdP[T <: IdentityProviderDetails](
      idp: Option[IdentityProviderDetails]): Option[T] = {
    idp.flatMap(c => Either.catchNonFatal(c.asInstanceOf[T]).toOption)
  }

  private def commonDetails(
      idp: IdentityProvider,
      existingConfig: Option[IdentityProviderDetails]): Either[String, CommonDetails] =
    for {
      encryptedAuthCodeClientSecret <- encrypt(
        "Authorisation Code flow Client Secret",
        idp.authCodeClientSecret,
        existingConfig.map(_.commonDetails.authCodeClientSecret)
      )
    } yield
      CommonDetails(
        platform = idp.platform,
        issuer = idp.issuer,
        authCodeClientId = idp.authCodeClientId,
        authCodeClientSecret = encryptedAuthCodeClientSecret,
        authUrl = URI.create(idp.authUrl).toURL,
        keysetUrl = URI.create(idp.keysetUrl).toURL,
        tokenUrl = URI.create(idp.tokenUrl).toURL,
        usernameClaim = idp.usernameClaim,
        defaultRoles = idp.defaultRoles,
        roleConfig = idp.roleConfig,
        enabled = idp.enabled
      )

  /**
    * Create an instance of `IdentityProviderDetails` from the given `IdentityProvider`. During the conversion,
    * all sensitive values must be either provided or available in an existing configuration; otherwise, errors will
    * be returned for the missing values.
    *
    * @return An `IdentityProviderDetails` instance if the conversion succeeds, or an `IllegalArgumentException`
    *         including all the captured errors
    */
  def apply(idp: IdentityProvider, existingConfig: Option[IdentityProviderDetails],
  )(implicit encryptionService: EncryptionService)
    : Either[IllegalArgumentException, IdentityProviderDetails] = {
    this.encryptionService = encryptionService

    val result = idp match {
      case generic: GenericIdentityProvider =>
        val encryptedApiClientSecret = encrypt(
          "API Client Secret",
          generic.apiClientSecret,
          existingIdP[GenericIdentityProviderDetails](existingConfig).map(_.apiClientSecret))

        // Convert to ValidatedNel to collect all the errors
        (commonDetails(idp, existingConfig).toValidatedNel, encryptedApiClientSecret.toValidatedNel)
          .mapN(
            (commonDetails, apiClientSecret) =>
              GenericIdentityProviderDetails(commonDetails = commonDetails,
                                             apiUrl = URI.create(generic.apiUrl).toURL,
                                             apiClientId = generic.apiClientId,
                                             apiClientSecret = apiClientSecret))
      case other =>
        invalidNel(s"Unsupported Identity Provider: ${other.platform}")
    }

    result.toEither
      .leftMap(errors => new IllegalArgumentException(errors.mkString_(",")))
  }
}
