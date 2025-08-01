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

package com.tle.core.usermanagement

import cats.implicits._
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.tle.common.usermanagement.user.valuebean.{DefaultUserBean, UserBean}
import com.tle.core.guice.Bind
import com.tle.core.oauthclient.AssertionTokenRequest
import com.tle.core.webkeyset.service.WebKeySetService
import com.tle.integration.oidc.OpenIDConnectParams
import com.tle.integration.oidc.idp.{IdentityProviderPlatform, OktaDetails}
import com.tle.web.oauth.OAuthWebConstants
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.model.Uri

import java.security.interfaces.RSAPrivateKey
import java.time.Instant
import javax.inject.Inject

/** Structure for the profile of a single user returned from Okta.
  */
final case class OktaUserProfile(
    login: String,
    firstName: Option[String],
    lastName: Option[String],
    email: Option[String]
)

object OktaUserProfile {
  implicit val decoder: Decoder[OktaUserProfile] = deriveDecoder[OktaUserProfile]
}

/** Structure for a single user returned from Okta, including the user ID and user profile.
  */
final case class OktaUser(id: String, profile: OktaUserProfile)

/** The target of this User Directory is Core Okta API.
  *
  * Reference link: https://developer.okta.com/docs/reference/core-okta-api/
  */
@Bind
class OktaUserDirectory @Inject() (webKeySetService: WebKeySetService) extends ApiUserDirectory {

  override protected type IDP = OktaDetails

  override protected val targetPlatform: IdentityProviderPlatform.Value =
    IdentityProviderPlatform.OKTA

  override protected type USER = OktaUser

  override protected type USERS = List[OktaUser]

  override protected implicit val userDecoder: Decoder[OktaUser] = deriveDecoder[OktaUser]

  override protected implicit val usersDecoder: Decoder[List[OktaUser]] =
    Decoder.decodeList[OktaUser]

  override protected def toUserList(users: USERS): List[USER] = users

  override protected def toUserBean(user: USER): UserBean = {
    val profile = user.profile
    new DefaultUserBean(
      user.id,
      profile.login,
      profile.firstName.getOrElse(""),
      profile.lastName.getOrElse(""),
      profile.email.orNull
    )
  }

  /** REST endpoint to get a single user with the provided ID.
    */
  override protected def userEndpoint(idp: OktaDetails, id: String): Uri =
    ApiUserDirectory.buildCommonUserEndpoint(idp.apiUrl, Seq("users"), id)

  /** REST endpoint to list users with the provided query.
    *
    *   1. Use param 'q' to list users whose first name, last name or email match the specified
    *      query. 2. 'q' may not provide the best performance but it is a good starting point. We
    *      can consider using the advanced 'search' param in the future if needed. 3. Wildcard is
    *      not supported so drop the prefix and suffix of the query.
    *
    * Reference:
    * https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/listUsers
    */
  override protected def userListEndpoint(idp: OktaDetails, query: String): Uri = {
    val baseEndpoint = Uri(idp.apiUrl.toURI).addPath("users")
    Option(query)
      .map(_.drop(1).dropRight(1)) // Remove the prefix and suffix of the query
      .filter(_.trim.nonEmpty)
      .map(baseEndpoint.addParam("q", _))
      .getOrElse(baseEndpoint)
  }

  /** According to the doco of Core Okta API, the request for a scoped access token through Client
    * Credentials must include the following parameters:
    *
    *   - 'scope', in this case, the value is "okta.users.read"
    *   - 'client_assertion_type' which must be
    *     "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
    *   - 'client_assertion' which is a signed JWT
    *   - 'grant_type' which must be "client_credentials"
    *
    * A JWT will be created with the IdP configuration and signed by the private key generated for
    * the configuration, and failing to find the key will result in a
    * [[com.auth0.jwt.exceptions.JWTCreationException]]
    *
    * The doco also states that only the org authorization server (Okta built-in auth server) can
    * mint access tokens that contain Okta API scopes. As a result, if user chooses to use the token
    * URL of a different auth server (e.g. the default custom auth server), a URL transformation is
    * required to make sure the access token request is sent to the org authorization server.
    *
    * Reference links:
    *   - https://developer.okta.com/docs/guides/implement-oauth-for-okta-serviceapp/main/#create-and-sign-the-jwt
    *   - https://developer.okta.com/docs/guides/implement-oauth-for-okta-serviceapp/main/#get-an-access-token
    *   - https://developer.okta.com/docs/concepts/auth-servers/#which-authorization-server-should-you-use
    */
  override protected def tokenRequest(idp: OktaDetails): AssertionTokenRequest = {
    val tokenUrl: String = {
      // The token URL of of a custom auth server always has the format of 'https://{domain}/oauth2/{authServerId}/{version}/token',
      // whereas that of the org auth server is similar but without the auth server ID in between path 'oauth2' and 'version'.
      // So we can have a regex of 3 groups to support the transformation:
      // First group contains everything up to '/oauth2';
      // Second group contains the auth server ID;
      // Third group contains everything after the auth server ID until '/token';
      // And combine the first group and the third group.
      val TokenRegex = "(.+/oauth2)/(.+)/(.+/token)".r
      val configured = idp.commonDetails.tokenUrl.toString

      configured match {
        case TokenRegex(group1, _, group3) => s"$group1/$group3"
        case _                             => configured
      }
    }

    def clientAssertion(okta: OktaDetails): String = {
      val keyId = okta.keyId

      Either.catchNonFatal {
        webKeySetService.getKeypairByKeyID(keyId)
      } match {
        case Right(Some(keyPair)) =>
          JWT
            .create()
            .withIssuer(okta.apiClientId)
            .withAudience(tokenUrl)
            .withSubject(okta.apiClientId)
            .withIssuedAt(Instant.now)
            .withNotBefore(Instant.now)
            .withExpiresAt(Instant.now.plusSeconds(30))
            .withKeyId(keyId)
            .sign(Algorithm.RSA256(keyPair.getPrivate.asInstanceOf[RSAPrivateKey]))
        case Right(None) =>
          throw new JWTCreationException(
            s"Failed to request token: key pair with ID $keyId not found",
            null
          )
        case Left(error) =>
          throw new JWTCreationException("Error occurred while getting key pair", error)
      }
    }

    AssertionTokenRequest(
      authTokenUrl = tokenUrl,
      clientId = idp.apiClientId,
      assertionType = OAuthWebConstants.PARAM_CLIENT_ASSERTION_TYPE_JWT_BEARER,
      assertion = clientAssertion(idp),
      data = Option(
        Map(
          OpenIDConnectParams.SCOPE -> "okta.users.read"
        )
      )
    )
  }

  /** Custom attributes are typically defined under field 'profile' in Okta. However, Okta does not
    * support specifying what fields to be included in the response through query parameters, so use
    * the default implementation to retrieve the full set of attributes which always includes
    * 'profile'.
    */
  override protected def customUserIdUrl(idp: IDP, stdId: String, attrs: Array[String]): Uri =
    super.customUserIdUrl(idp, stdId, attrs)

  override protected val customAttributeDelimiter: String = "\\."
}
