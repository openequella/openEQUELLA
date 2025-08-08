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
import com.tle.integration.oidc.idp.{GenericIdentityProviderDetails, IdentityProviderPlatform}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.model.Uri

import java.net.URI

/** Structure for the information of a single user returned from Auth0.
  */
case class Auth0User(
    user_id: String,
    name: String,
    username: Option[String],
    family_name: Option[String],
    given_name: Option[String],
    email: Option[String]
)

/** The primary target of this User Directory is Auth0's User Management APIs. However, it can also
  * be reused for other platform's APIs as long as the OIDC configuration fits
  * [[GenericIdentityProviderDetails]] and their APIs meet the requirements listed below:
  *
  *   1. The returned data MUST fit the structure of `Auth0User` for searching one user and
  *      `List[Auth0User]` for searching multiple users; 2. The APIs MUST have the same endpoints as
  *      Auth0's User Management APIs, as well as support the same query parameters; 3. The APIs
  *      can't require any additional data in the POST request for an OAuth2 Access Token.
  *
  * Reference link: https://auth0.com/docs/api/management/v2/users/get-users
  */
@Bind
class Auth0UserDirectory extends ApiUserDirectory {

  override val targetPlatform: IdentityProviderPlatform.Value = IdentityProviderPlatform.AUTH0

  override type IDP = GenericIdentityProviderDetails

  override protected type USER = Auth0User

  override protected implicit val userDecoder: Decoder[Auth0User] = deriveDecoder[Auth0User]

  override protected type USERS = List[Auth0User]

  override protected implicit val usersDecoder: Decoder[List[Auth0User]] =
    Decoder.decodeList[Auth0User]

  override protected def toUserList(users: USERS): List[Auth0User] = users

  /** Family name and given name are optional fields in Auth0's user data. If family name is absent,
    * use the account name as the family name since account name always exists and is more
    * human-readable than the user ID. If given name is absent, use an empty string as the given
    * name. With this set up, those pages which use family name and given name will not end up
    * showing a blank string.
    */
  override protected def toUserBean(user: Auth0User): UserBean = {
    new DefaultUserBean(
      user.user_id,
      user.username.getOrElse(user.user_id),
      user.given_name.getOrElse(""),
      user.family_name.getOrElse(user.name),
      user.email.orNull
    )
  }

  override protected def userEndpoint(idp: GenericIdentityProviderDetails, id: String): Uri =
    ApiUserDirectory.buildCommonUserEndpoint(idp.apiUrl, Seq("users"), id)

  override protected def userListEndpoint(idp: GenericIdentityProviderDetails, query: String): Uri =
    Uri(idp.apiUrl.toURI)
      .addPath("users")
      .addParam("q", query)

  /** According to the doco of Auth0, 'audience' is required in the request and the value should be
    * the API URL.
    *
    * Reference link: https://auth0.com/docs/secure/tokens/access-tokens/get-access-tokens.
    */
  override protected def tokenRequest(idp: IDP): ClientSecretTokenRequest =
    ClientSecretTokenRequest(
      idp.commonDetails.tokenUrl.toString,
      idp.apiClientId,
      idp.apiClientSecret,
      Option(Map("audience" -> idp.apiUrl.toString))
    )

  /** Auth0 always returns a full set of attributes by default, including 'user_metadata' and
    * 'app_metadata' where custom attributes are typically defined. Therefore, adding the
    * 'include_fields' and 'fields' parameters to exclude unnecessary data in the response.
    *
    * Regardless of whether the attribute is defined under 'user_metadata', 'app_metadata' or
    * another field, if the attribute path is hierarchical (e.g. 'user_metadata.custom_attr'), only
    * the top level attribute, namely 'user_metadata', can be specified in the 'fields' parameter.
    */
  override protected def customUserIdUrl(
      idp: IDP,
      stdId: String,
      attributePathSegments: Array[String]
  ): Uri =
    userEndpoint(idp, stdId)
      .addParam("include_fields", "true")
      .addParam("fields", attributePathSegments.head)

  override protected val customAttributeDelimiter: Char = '.'
}
