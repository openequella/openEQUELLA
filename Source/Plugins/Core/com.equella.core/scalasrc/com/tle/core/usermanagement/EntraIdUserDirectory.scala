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

import com.tle.core.guice.Bind
import com.tle.core.oauthclient.ClientSecretTokenRequest
import com.tle.integration.oidc.OpenIDConnectParams
import com.tle.integration.oidc.idp.{GenericIdentityProviderDetails, IdentityProviderPlatform}
import io.circe.{ACursor, Decoder}
import sttp.model.{Header, Uri}

/** The target of this User Directory is the Microsoft Graph REST API.
  *
  * Reference link: https://learn.microsoft.com/en-us/graph/api/overview?view=graph-rest-1.0
  */
@Bind
class EntraIdUserDirectory extends ApiUserDirectory {

  override protected val targetPlatform: IdentityProviderPlatform.Value =
    IdentityProviderPlatform.ENTRA_ID

  override type IDP = GenericIdentityProviderDetails

  override protected implicit val userDecoder: Decoder[IdPUser] = Decoder.instance { cursor =>
    for {
      id        <- cursor.downField("id").as[String]
      username  <- cursor.downField("displayName").as[Option[String]]
      firstName <- cursor.downField("givenName").as[Option[String]]
      lastName  <- cursor.downField("surname").as[Option[String]]
      email     <- cursor.downField("mail").as[Option[String]]
    } yield IdPUser(id, username, firstName, lastName, email, cursor.value)
  }

  override protected implicit val usersDecoder: Decoder[List[IdPUser]] = Decoder.instance {
    cursor =>
      // Users are nested under the "value" field in the response from Microsoft Graph API.
      val values: ACursor = cursor.downField("value")
      Decoder.decodeList[IdPUser].tryDecode(values)
  }

  override protected def userEndpoint(idp: GenericIdentityProviderDetails, id: String): Uri =
    ApiUserDirectory.buildCommonUserEndpoint(idp.apiUrl, Seq("users"), id)

  /** There are three important things to determine the user listing endpoint in Microsoft Graph:
    *
    *   1. The Graph REST API V1 does NOT support wildcard search, and the way to restrict the
    *      search result is using their 'search' parameter; 2. The 'search' parameter does NOT query
    *      multiple fields so we need to specify the fields; 3. The query passed to this method
    *      ALWAYS has a prefix of '*' and a suffix of '*'. Check
    *      [[com.tle.core.services.user.impl.UserServiceImpl#fixQuery(java.lang.String)]] for the
    *      reason.
    *
    * As a result, this method will remove the prefix and suffix and then build a search param to
    * support querying 'displayName', 'mail', 'userPrincipalName and maybe more if required.
    *
    * References:
    * https://learn.microsoft.com/en-us/graph/query-parameters?tabs=http#search-parameter
    * https://learn.microsoft.com/en-us/graph/api/user-list?view=graph-rest-1.0&tabs=http#example-6-use-search-to-get-users-with-display-names-that-contain-the-letters-wa-including-a-count-of-returned-objects
    * https://learn.microsoft.com/en-us/graph/api/resources/user?view=graph-rest-1.0#properties
    */
  override protected def userListEndpoint(
      idp: GenericIdentityProviderDetails,
      query: String
  ): Uri = {
    val baseEndpoint = Uri(idp.apiUrl.toURI).addPath("users").addParams(includeFields(idp))

    def buildSearchCriteria(q: String) =
      List("displayName", "mail", "userPrincipalName")
        .map(_ + ":" + q)
        .map(criteria =>
          s"""\"$criteria\""""
        ) // The Advanced search syntax requires each criteria to be wrapped in double quotes
        .mkString(" OR ") // And separated by 'OR'

    Option(query)
      .map(_.drop(1).dropRight(1)) // Remove the prefix and suffix of the query
      .filter(_.trim.nonEmpty)     // If the remaining are spaces only, do not use it
      .map(buildSearchCriteria)
      .map(baseEndpoint.addParam("$search", _))
      .getOrElse(baseEndpoint)
  }

  /** Entra ID supports using query parameter `$filter` to filter users based on custom attributes,
    * but wildcard searches are not supported for custom attributes. Each filter must be in the
    * format of `attr eq 'value'`, and multiple filters can be combined using `OR` or `And`. The use
    * of `$filter` also requires including query parameter `$count` with value `true`.
    *
    * References:
    *   - https://learn.microsoft.com/en-us/graph/filter-query-parameter
    *   - https://learn.microsoft.com/en-us/graph/aad-advanced-queries?tabs=http#user-properties
    */
  override protected def userListByAttrsEndpoint(
      idp: GenericIdentityProviderDetails,
      value: String,
      attrs: List[String]
  ): Uri = {
    val filters = attrs.map(attr => s"$attr eq '$value'").mkString(" OR ")
    Uri(idp.apiUrl.toURI)
      .addPath("users")
      .addParams(includeFields(idp))
      .addParam("$filter", filters)
      .addParam("$count", "true")
  }

  /** According to the doco of OIDC scope, the scope `https://graph.microsoft.com/.default` must be
    * included in the request in order to obtain an access token that is permitted to use Graph REST
    * API.
    *
    * Reference link:
    * https://learn.microsoft.com/en-us/entra/identity-platform/scopes-oidc#the-default-scope
    */
  override protected def tokenRequest(idp: IDP): ClientSecretTokenRequest =
    ClientSecretTokenRequest(
      idp.commonDetails.tokenUrl.toString,
      idp.apiClientId,
      idp.apiClientSecret,
      Option(Map(OpenIDConnectParams.SCOPE -> "https://graph.microsoft.com/.default"))
    )

  /** According to the doco of Advanced query, the header `ConsistencyLevel` must be included with
    * value being `eventual`.
    *
    * Reference link: https://learn.microsoft.com/en-us/graph/aad-advanced-queries
    */
  override protected val requestHeaders: List[Header] = List(Header("ConsistencyLevel", "eventual"))

  /** Entra ID may not return certain custom attributes by default, so explicitly request the
    * configured attribute to be included by using the '$select' query parameter.
    *
    * If the attribute path is hierarchical (e.g.
    * 'onPremisesExtensionAttributes/extensionAttribute5'), only the top level attribute, namely
    * 'onPremisesExtensionAttributes', can be specified in the '$select' parameter.
    */
  override def customUserIdUrl(idp: IDP, stdId: String, attributePathSegments: Array[String]): Uri =
    userEndpoint(idp, stdId).addParam("$select", attributePathSegments.head)

  override val customAttributeDelimiter: Char = '/'

  // Builds the `$select` query parameter for Entra ID to specify which fields to include in the response.
  // Always includes the standard fields: `id`, `displayName`, `surname`, `givenName`, and `mail`. If a custom
  // user ID attribute is configured, includes its first segment as well.
  private def includeFields(idp: GenericIdentityProviderDetails): Map[String, String] = {
    val idAttrFirstSegment = idp.commonDetails.userIdAttribute
      .map(_.split(customAttributeDelimiter))
      .flatMap(_.headOption)
      .getOrElse("")

    Map("$select" -> s"id,displayName,surname,givenName,mail,$idAttrFirstSegment")
  }
}
