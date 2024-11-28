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

import cats.implicits._
import com.tle.core.encryption.EncryptionService
import com.tle.core.webkeyset.service.WebKeySetService

import java.net.{URI, URL}

/**
  * The common details configured for OIDC with an Identity Provider.
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
final case class CommonDetails(platform: IdentityProviderPlatform.Value,
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
  * Configuration details for Identity Provider where the way to request resources is through REST APIs.
  * The structure is similar to the concrete classes that extend [[IdentityProvider]] and [[RestApi]], but
  * with fully defined types used for storage and internal operations, where as IdentityProvider is more
  * a DTO for the REST API.
  *
  * Key differences from IdentityProvider:
  * 1. Slightly more strict types
  *    - secret values are mandatory
  *    - URL type values use `URL` instead of `String`
  * 2. Secret values are encrypted
  * 3. Details for OIDC are centralised into one field
  *
  * @param commonDetails Common details configured for OIDC
  * @param apiUrl The API endpoint for the Identity Provider, use for operations such as search for users
  * @param apiClientId Client ID used to get an Authorisation Token to use with the Identity Provider's API
  *                    (for user searching etc)
  * @param apiClientSecret Client Secret used with `apiClientId` to get an Authorization Token to use with
  *                        the Identity Provider's API (for user searching etc). The value will be encrypted on saving.
  */
final case class GenericIdentityProviderDetails(
    commonDetails: CommonDetails,
    apiUrl: URL,
    apiClientId: String,
    apiClientSecret: String,
) extends IdentityProviderDetails

/**
  * Configuration details for Okta and the intention of this class is the same as what [[GenericIdentityProviderDetails]]
  * is for.
  *
  * @param commonDetails Common details configured for OIDC
  * @param apiUrl The API endpoint for the Identity Provider, use for operations such as search for users
  * @param apiClientId Client ID used to get an Authorisation Token to use with the Identity Provider's API
  *                    (for user searching etc)
  * @param keyId ID of the key pair which is used to sign and verify the JWT used with `apiClientId` to get an access token
  */
final case class OktaDetails(
    commonDetails: CommonDetails,
    apiUrl: URL,
    apiClientId: String,
    keyId: String,
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
  )(implicit encryptionService: EncryptionService, webKeySetService: WebKeySetService)
    : Either[IllegalArgumentException, IdentityProviderDetails] = {
    this.encryptionService = encryptionService

    val result = idp match {
      case okta: Okta =>
        // Generate a new key pari when there isn't a config for Okta or the existing config is broken.
        val keyId = existingConfig
          .flatMap(c => Either.catchNonFatal(c.asInstanceOf[OktaDetails]).toOption)
          .map(_.keyId)
          .getOrElse(webKeySetService.generateKeyPair.keyId)

        commonDetails(okta, existingConfig)
          .map(
            commonDetails =>
              OktaDetails(commonDetails = commonDetails,
                          apiUrl = URI.create(okta.apiUrl).toURL,
                          apiClientId = okta.apiClientId,
                          keyId = keyId))
          .toValidatedNel
      case other: IdentityProvider with RestApi =>
        val existingApiSecret = existingConfig.collect {
          case details: GenericIdentityProviderDetails => details.apiClientSecret
        }
        val encryptedApiSecret =
          encrypt("API Client Secret", other.apiClientSecret, existingApiSecret)

        (commonDetails(idp, existingConfig).toValidatedNel, encryptedApiSecret.toValidatedNel)
          .mapN(
            (commonDetails, apiClientSecret) =>
              GenericIdentityProviderDetails(commonDetails = commonDetails,
                                             apiUrl = URI.create(other.apiUrl).toURL,
                                             apiClientId = other.apiClientId,
                                             apiClientSecret = apiClientSecret))
    }

    result.toEither
      .leftMap(errors => new IllegalArgumentException(errors.mkString_(",")))
  }
}
