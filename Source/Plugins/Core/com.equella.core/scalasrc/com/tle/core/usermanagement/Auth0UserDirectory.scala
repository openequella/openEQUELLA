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
import com.tle.common.Pair
import com.tle.common.usermanagement.user.valuebean.{DefaultUserBean, UserBean}
import com.tle.core.guice.Bind
import com.tle.core.oauthclient.{OAuthClientService, OAuthTokenState, TokenRequest}
import com.tle.core.usermanagement.GenericIdPUser.toUserBean
import com.tle.integration.oidc.idp.{GenericIdentityProviderDetails, IdentityProviderPlatform}
import com.tle.plugins.ump.UserDirectory
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.slf4j.LoggerFactory
import sttp.client.circe.asJson
import sttp.client.{UriContext, basicRequest}

import java.util
import scala.jdk.CollectionConverters._

/**
  * Structure for the information of a single user returned from a generic Identity Provider.
  */
case class GenericIdPUser(user_id: String,
                          name: String,
                          username: Option[String],
                          family_name: Option[String],
                          given_name: Option[String],
                          email: Option[String])

object GenericIdPUser {
  implicit val idpUserDecoder = deriveDecoder[GenericIdPUser]

  /**
    * Family name and given name are optional fields in Auth0's user data. If family name is absent, use the account name
    * as the family name since account name always exists and is more human-readable than the user ID. If given name is
    * absent, use an empty string as the given name. With this set up, those pages which use family name and given name will
    * not end up showing a blank string.
    */
  def toUserBean(user: GenericIdPUser): UserBean = {
    new DefaultUserBean(
      user.user_id,
      user.username.getOrElse(user.user_id),
      user.family_name.getOrElse(user.name),
      user.given_name.getOrElse(""),
      user.email.orNull
    )
  }
}

/**
  * The primary target of this User Directory is Auth0's User Management APIs. However, it can also be reused for other
  * platform's APIs as long as the OIDC configuration fits [[GenericIdentityProviderDetails]] and their APIs meet the
  * requirements listed below:
  *
  * 1. The returned data MUST fit the structure of `GenericIdPUser` for searching one user and `List[GenericIdPUser]` for
  *    searching multiple users;
  * 2. The APIs MUST have the same endpoints as Auth0's User Management APIs, as well as support the same query parameters;
  * 3. The APIs can't require any additional data in the POST request for an OAuth2 Access Token.
  *
  * Reference link: https://auth0.com/docs/api/management/v2/users/get-users
  */
@Bind
class Auth0UserDirectory extends OidcUserDirectory {
  private val LOGGER = LoggerFactory.getLogger(classOf[Auth0UserDirectory])

  override val targetPlatform: IdentityProviderPlatform.Value = IdentityProviderPlatform.AUTH0

  override type IDP = GenericIdentityProviderDetails

  override type AuthResult = OAuthTokenState

  override def searchUsers(
      query: String): Pair[UserDirectory.ChainResult, util.Collection[UserBean]] = {
    lazy val search: String => (IDP, OAuthTokenState) => Either[Throwable, List[GenericIdPUser]] =
      (query: String) =>
        (idp: IDP, tokenState: OAuthTokenState) => {
          val apiUrl   = idp.apiUrl.toString
          val endpoint = s"${apiUrl}users?q=$query"
          request[List[GenericIdPUser]](endpoint, tokenState)
      }

    val users = execute(search(query))
      .leftMap(LOGGER.error(s"Failed to search users", _))
      .getOrElse(List.empty)
      .map(toUserBean)
      .asJavaCollection

    new Pair(UserDirectory.ChainResult.CONTINUE, users)
  }

  override def getInformationForUser(userId: String): UserBean = {
    lazy val search: String => (IDP, OAuthTokenState) => Either[Throwable, GenericIdPUser] =
      (id: String) =>
        (idp: IDP, tokenState: OAuthTokenState) => {
          val apiUrl   = idp.apiUrl.toString
          val endpoint = s"${apiUrl}users/$id"
          request[GenericIdPUser](endpoint, tokenState)
      }

    execute(search(userId))
      .leftMap(LOGGER.error(s"Failed to get information for user $userId", _))
      .map(toUserBean)
      .toOption
      .orNull
  }

  override def getInformationForUsers(
      userIDs: util.Collection[String]): util.Map[String, UserBean] =
    userIDs.asScala
      .map(id => id -> Option(getInformationForUser(id)))
      .collect {
        case (id, Some(info)) => (id, info)
      }
      .toMap
      .asJava

  override protected def authenticate(idp: IDP): Either[Throwable, AuthResult] = {
    val tokenUrl = idp.commonDetails.tokenUrl.toString

    // To get more details of what params are required, please refer to https://auth0.com/docs/secure/tokens/access-tokens/get-access-tokens.
    val tokenRequest = TokenRequest(
      tokenUrl,
      idp.apiClientId,
      idp.apiClientSecret,
      Option(Map("audience" -> idp.apiUrl.toString))
    )

    Either.catchNonFatal(OAuthClientService.tokenForClient(tokenRequest))
  }

  // Send a GET request to the supplied endpoint with the supplied access token.
  private def request[T](endpoint: String, tokenState: OAuthTokenState)(
      implicit decoder: Decoder[T]): Either[Throwable, T] = {
    val req = basicRequest
      .get(uri"$endpoint")
      .response(asJson[T])

    for {
      resp <- Either.catchNonFatal(
        OAuthClientService.requestWithToken(req, tokenState.token, tokenState.tokenType))
      data <- resp.body
    } yield data
  }
}
