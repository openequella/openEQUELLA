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
import com.tle.core.oauthclient.{OAuthClientService, OAuthTokenState, TokenRequest}
import com.tle.plugins.ump.UserDirectory
import io.circe.{ACursor, Decoder, Json}
import org.slf4j.LoggerFactory
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import sttp.model.{Header, Uri}

import java.net.URL
import java.util
import javax.inject.{Inject, Named}
import javax.ws.rs.NotFoundException
import scala.jdk.CollectionConverters._

/** Represents a single user returned from an IdP.
  *
  * @param id
  *   Unique identifier of the user which may be the IdP's standard user ID or a custom user ID.
  * @param username
  *   Username of the user
  * @param firstName
  *   First name of the user
  * @param lastName
  *   Last name of the user
  * @param email
  *   Email address of the user
  * @param raw
  *   Raw JSON object containing the full user data.
  */
final case class IdPUser(
    id: String,
    username: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    email: Option[String],
    private val raw: Json
) {

  /** Attempt to retrieve an attribute from the raw JSON object.
    *
    * @param attrPathSegments
    *   Segments of the attribute path to traverse in the JSON object.
    */
  def getAttribute(attrPathSegments: Array[String]): Either[Throwable, String] = {
    attrPathSegments
      .foldLeft[ACursor](raw.hcursor) { (cursor, attr) =>
        cursor.downField(attr)
      }
      .as[String]
  }
}

/** On top of [[OidcUserDirectory]], this class provides the abstraction of retrieving user
  * information through REST APIs and OAuth2 Access Token.
  */
abstract class ApiUserDirectory extends OidcUserDirectory {
  @Inject
  @Named("enable.oidc.token.logging")
  private var tokenLoggingEnabled: Boolean = _

  private val LOGGER = LoggerFactory.getLogger(classOf[ApiUserDirectory])

  /** Each IdP's API returns the user details in a proprietary structure, therefore each IdP
    * UserDirectory needs to provide a custom Circe decoder to map from those structures to the
    * common IdPUser structure.
    */
  implicit val userDecoder: Decoder[IdPUser]

  /** Circe decoder for mapping a list of IdP-specific user structures to a list of the common
    * IdPUser structure.
    */
  implicit val usersDecoder: Decoder[List[IdPUser]]

  override protected type AuthResult = OAuthTokenState

  /** Use the provided Identity Provider details and user ID to build a full URL that points to the
    * REST Endpoint that returns a single user.
    */
  protected def userEndpoint(idp: IDP, id: String): Uri

  /** Use the provided Identity Provider details and search query to build a full URL that points to
    * the REST Endpoint that returns a list of users.
    */
  protected def userListEndpoint(idp: IDP, query: String): Uri

  /** A variation of [[userListEndpoint]] that supports searching for users using a list of specific
    * attributes.
    *
    * This method is needed because many Identity Providers have limitations when searching by
    * custom attributes. Examples include:
    *   - Requiring a different query parameter with a different syntax.
    *   - Not supporting wildcard searches.
    */
  protected def userListByAttrsEndpoint(idp: IDP, value: String, attrs: List[String]): Uri

  /** Build an instance of TokenRequest to be used in the authentication process.
    */
  protected def tokenRequest(idp: IDP): TokenRequest

  override protected def authenticate(idp: IDP): Either[Throwable, OAuthTokenState] =
    for {
      result <- Either.catchNonFatal(OAuthClientService.tokenForClient(tokenRequest(idp)))
      _ = if (tokenLoggingEnabled) LOGGER.debug(s"Retrieved Access Token: ${result.token}")
    } yield result

  /** Send a GET request to access resources from the Identity Provider with the provided access
    * token.
    *
    * @param endpoint
    *   Endpoint of the resources to request.
    * @param tokenState
    *   An OAuth2 access token issued by the Identity Provider.
    * @param headers
    *   Additional headers to include in the request.
    * @param decoder
    *   Circe Decoder that transforms the obtained resource to the target type.
    * @tparam T
    *   Type of the resources to request.
    *
    * @return
    *   Either an error describing why the request failed or the requested resources in the target
    *   type.
    */
  protected def requestWithToken[T](
      endpoint: Uri,
      tokenState: OAuthTokenState,
      headers: List[Header] = List.empty
  )(implicit decoder: Decoder[T]): Either[Throwable, T] = {
    val req = basicRequest
      .get(endpoint)
      .headers(headers: _*)
      .response(asJson[T])

    for {
      resp <- Either.catchNonFatal(
        OAuthClientService.requestWithToken(req, tokenState.token, tokenState.tokenType)
      )
      data <- resp.body
    } yield data
  }

  /** Additional headers to include in the user search request. Override if target IdP's API
    * requires anything additional/different.
    */
  protected val requestHeaders: List[Header] = List.empty

  /** Search for users using a free-text query against standard user fields defined by each
    * platform. The fields searched and whether the search is an exact match or allows wildcard are
    * determined by each platform.
    *
    * @param query
    *   a string representing the free-text query.
    */
  override def searchUsers(
      query: String
  ): Pair[UserDirectory.ChainResult, util.Collection[UserBean]] = {
    val users = execute { (idp, tokenState) =>
      val endpoint = userListEndpoint(idp, query)
      searchUsers(endpoint, tokenState, idp)
    }
      .leftMap(LOGGER.error(s"Failed to search users", _))
      .getOrElse(List.empty)
      .asJavaCollection

    new Pair(UserDirectory.ChainResult.CONTINUE, users)
  }

  override def getInformationForUser(userId: String): UserBean = {

    // Searching users by an attribute may return multiple users. In that case, only use the first user and log a warning.
    def getFirstUserByAttr(
        attr: String,
        value: String,
        tokenState: OAuthTokenState,
        idp: IDP
    ): Either[Throwable, UserBean] = {
      val endpoint = userListByAttrsEndpoint(idp, value, List(attr))
      for {
        users <- searchUsers(endpoint, tokenState, idp)
        _ = if (users.size > 1)
          LOGGER.warn(s"More than one user found by attribute {}: {}", attr, value)
        user <- users.headOption.toRight(
          new NotFoundException(s"No users found by $attr: $userId")
        )
      } yield user
    }

    lazy val search: String => (IDP, OAuthTokenState) => Either[Throwable, UserBean] =
      id =>
        (idp, tokenState) =>
          idp.commonDetails.userIdAttribute match {
            case Some(attr) =>
              getFirstUserByAttr(attr, id, tokenState, idp)
            case None =>
              val endpoint = userEndpoint(idp, id)
              val result   = requestWithToken[IdPUser](endpoint, tokenState, requestHeaders)
              result.map(toUserBean(_, None))
          }

    // If the provider user ID is null or empty, return null.
    Option(userId)
      .filter(_.nonEmpty)
      .flatMap(id =>
        execute(search(id))
          .leftMap(LOGGER.error(s"Failed to get information for user $userId", _))
          .toOption
      )
      .orNull
  }

  /** Warning: The current implementation does not do a batch operation to return multiple users,
    * and this is because some IdPs do not have an API to support this. Hence, multiple requests
    * will be made to retrieve the information of each user, which may hit the API rate limit.
    */
  override def getInformationForUsers(
      userIDs: util.Collection[String]
  ): util.Map[String, UserBean] =
    userIDs.asScala
      .map(id => id -> Option(getInformationForUser(id)))
      .collect { case (id, Some(info)) =>
        (id, info)
      }
      .toMap
      .asJava

  /** Constructs a URL used to retrieve the custom user identifier for a user. Default to the
    * standard single-user endpoint without any query parameters.
    *
    * @param idp
    *   Target Identity Provider
    * @param stdId
    *   IdP standard user ID (e.g. Entrd ID 'oid')
    * @param attributePathSegments
    *   A non-empty array of strings representing the segments of the attribute path
    */
  protected def customUserIdUrl(
      idp: IDP,
      stdId: String,
      attributePathSegments: Array[String]
  ): Uri =
    userEndpoint(idp, stdId)

  /** Most Identity Providers support defining custom attributes, but their APIs do not allow
    * retrieving the value of a nested attribute by specifying its full path in a query parameter.
    * Instead,the request must use only the first segment of the path, and the response will include
    * the entire nested structure, which can be traversed to extract the desired value. As a result,
    * the delimiter used by an IdP must be specified to help transform the full attribute path into
    * an array.
    */
  protected val customAttributeDelimiter: Char

  /** Retrieve the custom user ID for an IdP user via an API request, using the configured custom
    * user ID attribute. On success, extract the value from the response body by traversing the
    * response structure with each level of the attribute.
    *
    * Example for Entra ID:
    *   - Configured attribute: `onPremisesExtensionAttributes/extensionAttribute2`,
    *   - Request URL:
    *     https://graph.microsoft.com/v1.0/users/11815cb1-4bcf-4ded-94cc-989117c1383f?$select=onPremisesExtensionAttributes
    *   - Response body (assuming there are 3 nested attributes under
    *     `onPremisesExtensionAttributes`):
    *     {{{
    *  {
    *    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users(displayName,onPremisesExtensionAttributes)/$entity",
    *    "onPremisesExtensionAttributes": {
    *       "extensionAttribute1":null,
    *       "extensionAttribute2":"adfcaf58-241b-4eca-9740-6a26d1c3dd58",
    *       "extensionAttribute3": null
    *     }
    *   }
    *     }}}
    */
  override def getCustomUserId(
      stdId: String,
      userIdAttribute: String
  ): Either[Throwable, String] = {
    lazy val search: (IDP, OAuthTokenState) => Either[Throwable, String] = (idp, tokenState) => {
      val attrs = userIdAttribute.split(customAttributeDelimiter)
      val url   = customUserIdUrl(idp, stdId, attrs)

      requestWithToken[Json](url, tokenState, requestHeaders)
        .flatMap(json =>
          attrs
            .foldLeft[ACursor](json.hcursor) { (cursor, attr) =>
              cursor.downField(attr)
            }
            .as[String]
        )
    }

    execute(search)
  }

  /** Converts an IdPUser to a UserBean.
    *
    * If a user ID attribute is provided, attempts to retrieve the custom user ID from this
    * attribute. If the retrieval fails, falls back to the IdP's standard user ID.
    *
    * Username defaults to the user ID if absent. First and last names default to empty strings if
    * absent. Email defaults to `null` if absent.
    */
  private def toUserBean(user: IdPUser, userIdAttribute: Option[String]): UserBean = {
    val customId: Option[String] = for {
      attr <- userIdAttribute
      attrSegments = attr.split(customAttributeDelimiter)
      value <- user
        .getAttribute(attrSegments)
        .leftMap(LOGGER.warn(s"Failed to get custom user ID from $attr for user ${user.id}", _))
        .toOption
    } yield value

    val id = customId.getOrElse(user.id)
    new DefaultUserBean(
      id,
      user.username.getOrElse(id),
      user.firstName.getOrElse(""),
      user.lastName.getOrElse(""),
      user.email.orNull
    )
  }

  // Execute a GET request to the specified endpoint using the provided Access token, and the result is converted
  // into a list of UserBean.
  private def searchUsers(
      endpoint: Uri,
      token: OAuthTokenState,
      idp: IDP
  ): Either[Throwable, List[UserBean]] =
    requestWithToken[List[IdPUser]](endpoint, token, requestHeaders)
      .map(users => users.map(toUserBean(_, idp.commonDetails.userIdAttribute)))
}

object ApiUserDirectory {

  /** Helper function to create common user endpoint. Automatically handles path joining and segment
    * encoding, since user ID may contain special characters.
    *
    * Example:
    * {{{
    *   val uri = buildCommonUserEndpoint(
    *     apiUrl = new URI("https://idp.example.com/api".toURL,
    *     userPath = Seq("account", "users"),
    *     id = "google-oauth2|123456789"
    *   )
    *   // Result: https://idp.example.com/api/account/users/google-oauth2%7C123456789
    * }}}
    *
    * @param apiUrl
    *   The base URL of the Identity Provider’s API.
    * @param userPath
    *   A sequence of relative path to the user endpoint.
    * @param id
    *   The user ID to be searched.
    */
  def buildCommonUserEndpoint(apiUrl: URL, userPath: Seq[String], id: String): Uri =
    Uri(apiUrl.toURI)
      .addPath(userPath :+ id)
}
