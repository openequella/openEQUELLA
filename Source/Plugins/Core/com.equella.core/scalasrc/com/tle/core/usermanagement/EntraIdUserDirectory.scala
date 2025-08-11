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

import com.tle.common.usermanagement.user.valuebean.{DefaultUserBean, UserBean}
import com.tle.core.guice.Bind
import com.tle.core.oauthclient.ClientSecretTokenRequest
import com.tle.integration.oidc.OpenIDConnectParams
import com.tle.integration.oidc.idp.{GenericIdentityProviderDetails, IdentityProviderPlatform}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.model.{Header, Uri}

/** Structure for the information of a single user returned from Entra ID.
  */
final case class EntraIdUser(
    id: String,
    displayName: Option[String],
    surname: Option[String],
    givenName: Option[String],
    mail: Option[String]
)

/** Structure for response returning multiple users - e.g. user search.
  */
final case class EntraIdUserList(value: List[EntraIdUser])

/** The target of this User Directory is the Microsoft Graph REST API.
  *
  * Reference link: https://learn.microsoft.com/en-us/graph/api/overview?view=graph-rest-1.0
  */
@Bind
class EntraIdUserDirectory extends ApiUserDirectory {

  override protected val targetPlatform: IdentityProviderPlatform.Value =
    IdentityProviderPlatform.ENTRA_ID

  override type IDP = GenericIdentityProviderDetails

  override protected type USER = EntraIdUser

  override protected type USERS = EntraIdUserList

  override protected implicit val userDecoder: Decoder[EntraIdUser] = deriveDecoder[EntraIdUser]

  override protected implicit val usersDecoder: Decoder[EntraIdUserList] =
    deriveDecoder[EntraIdUserList]

  override protected def toUserList(users: USERS): List[EntraIdUser] = users.value

  override protected def toUserBean(user: EntraIdUser): UserBean = {
    val username = user.displayName.getOrElse(user.id)
    new DefaultUserBean(
      user.id,
      username,
      user.givenName.getOrElse(""),
      user.surname.getOrElse(username),
      user.mail.orNull
    )
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
    val baseEndpoint = Uri(idp.apiUrl.toURI).addPath("users")

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
}
