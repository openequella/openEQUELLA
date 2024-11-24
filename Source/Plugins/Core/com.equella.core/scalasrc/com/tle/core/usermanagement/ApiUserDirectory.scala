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
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.core.oauthclient.{OAuthClientService, OAuthTokenState, TokenRequest}
import com.tle.integration.oidc.idp.GenericIdentityProviderDetails
import com.tle.plugins.ump.UserDirectory
import io.circe.Decoder
import org.slf4j.LoggerFactory
import sttp.client.circe.asJson
import sttp.client.{UriContext, basicRequest}
import sttp.model.Header
import java.util
import scala.jdk.CollectionConverters._

/**
  * On top of [[OidcUserDirectory]], this class provides the abstraction of retrieving user information
  * through REST APIs and OAuth2 Access Token.
  */
abstract class ApiUserDirectory extends OidcUserDirectory {
  private val LOGGER = LoggerFactory.getLogger(classOf[ApiUserDirectory])

  /**
    * Type alias for the information of a single user returned from the Identity Provider.
    */
  protected type USER

  /**
    * Type alias for the information of multiple users returned from the Identity Provider.
    *
    */
  protected type USERS

  protected implicit val userDecoder: Decoder[USER]

  protected implicit val usersDecoder: Decoder[USERS]

  /**
    * While the structure of `USERS` may be equal to `List[USER]` in some IdPs' responses, it may not in others'. So
    * override this function to provide the correct type transformation.
    */
  protected def toUserList(users: USERS): List[USER]

  protected def toUserBean(user: USER): UserBean

  override type IDP = GenericIdentityProviderDetails

  override protected type AuthResult = OAuthTokenState

  /**
    * REST Endpoint that returns a single user.
    */
  protected def userEndpoint(idp: IDP, id: String): String

  /**
    * REST Endpoint that returns a list of users.
    */
  protected def userListEndpoint(idp: IDP, query: String): String

  /**
    * Build an instance of TokenRequest to be used in the authentication process.
    */
  protected def tokenRequest(idp: IDP): TokenRequest

  override protected def authenticate(idp: IDP): Either[Throwable, OAuthTokenState] =
    Either.catchNonFatal(OAuthClientService.tokenForClient(tokenRequest(idp)))

  /**
    * Send a GET request to access resources from the Identity Provider with the provided access token.
    *
    * @param endpoint Endpoint of the resources to request.
    * @param tokenState An OAuth2 access token issued by the Identity Provider.
    * @param headers Additional headers to include in the request.
    * @param decoder Circe Decoder that transforms the obtained resource to the target type.
    * @tparam T Type of the resources to request.
    *
    * @return Either an error describing why the request failed or the requested resources in the target type.
    */
  protected def requestWithToken[T](
      endpoint: String,
      tokenState: OAuthTokenState,
      headers: List[Header] = List.empty)(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    val req = basicRequest
      .get(uri"$endpoint")
      .headers(headers: _*)
      .response(asJson[T])

    for {
      resp <- Either.catchNonFatal(
        OAuthClientService.requestWithToken(req, tokenState.token, tokenState.tokenType))
      data <- resp.body
    } yield data
  }

  /**
    * Additional headers to include in the user search request.
    */
  protected val requestHeaders: List[Header] = List.empty

  override def searchUsers(
      query: String): Pair[UserDirectory.ChainResult, util.Collection[UserBean]] = {
    lazy val search: String => (IDP, OAuthTokenState) => Either[Throwable, USERS] =
      query =>
        (idp, tokenState) =>
          requestWithToken[USERS](userListEndpoint(idp, query), tokenState, requestHeaders)

    val users = execute(search(query))
      .leftMap(LOGGER.error(s"Failed to search users", _))
      .map(toUserList)
      .getOrElse(List.empty)
      .map(toUserBean)
      .asJavaCollection

    new Pair(UserDirectory.ChainResult.CONTINUE, users)
  }

  override def getInformationForUser(userId: String): UserBean = {
    lazy val search: String => (IDP, OAuthTokenState) => Either[Throwable, USER] =
      id =>
        (idp, tokenState) =>
          requestWithToken[USER](userEndpoint(idp, id), tokenState, requestHeaders)

    // If the provider user ID is null or empty, return null.
    Option(userId)
      .filter(_.nonEmpty)
      .flatMap(
        id =>
          execute(search(id))
            .leftMap(LOGGER.error(s"Failed to get information for user $userId", _))
            .toOption)
      .map(toUserBean)
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
}
