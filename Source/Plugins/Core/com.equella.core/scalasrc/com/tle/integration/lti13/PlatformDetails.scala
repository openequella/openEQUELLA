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

package com.tle.integration.lti13

import com.tle.core.lti13.bean.LtiPlatformBean

import java.net.{URI, URL}

/** Definition for how unknown users should be handled:
  *
  *   - `ERROR`: Report an authentication error
  *   - `GUEST`: Authenticate the user as a 'guest' user
  *   - `CREATE`: a new local oEQ user and authenticate as that user
  */
object UnknownUserHandling extends Enumeration {
  type Handling = Value

  val ERROR, GUEST, CREATE = Value
}

/** The details registered with oEQ regarding the platform identified with `platformId`. But with
  * some slightly more concete types (`URL` and `UnknownUserHandling`) compared to `LtiPlatformBean`
  * which needed to be looser for REST endpoints etc.
  *
  * @param platformId
  *   The issuer identifier identifying the learning platform.
  * @param name
  *   The name display in oEQ for the learning platform.
  * @param clientId
  *   The clientId for oEQ issued by the platform
  * @param authUrl
  *   The platform's authentication request URL
  * @param keysetUrl
  *   The platform's JWKS keyset URL for us to get the keys from
  * @param usernamePrefix
  *   a value to prefix the userId from the LTI request with
  * @param usernameSuffix
  *   value to concatenate to the userId from the LTI request
  * @param unknownUserHandling
  *   a tuple where the first value indicates how unknown users should be handled, and the second
  *   value is only present if the handling is to create user objects. In which case, the list of
  *   groups (either UUIDs or internal oEQ group names) will be added to the new user object.
  * @param instructorRoles
  *   a list of roles (either UUIDs or internal oEQ group names) to assign to the user's session if
  *   they have an LTI Instructor role.
  * @param unknownRoles
  *   a list of roles (either UUIDs or internal oEQ group names) to assign to the user's session for
  *   any roles which are not an instructor role or in the list of custom roles.
  * @param customRoles
  *   a mapping of LTI roles to oEQ roles (either UUIDs or internal oEQ group names) to assign to
  *   the user's session.
  * @param allowExpression
  *   an ACL Expression to control access from this platform - None is the same as an ACL Expression
  *   of 'Everyone' or '*'
  */
case class PlatformDetails(
    platformId: String,
    name: String,
    clientId: String,
    authUrl: URL,
    keysetUrl: URL,
    usernamePrefix: Option[String],
    usernameSuffix: Option[String],
    usernameClaim: Option[String],
    unknownUserHandling: (UnknownUserHandling.Handling, Option[Set[String]]),
    instructorRoles: Set[String],
    unknownRoles: Set[String],
    customRoles: Map[String, Set[String]],
    allowExpression: Option[String]
)

object PlatformDetails {
  def apply(bean: LtiPlatformBean): PlatformDetails =
    PlatformDetails(
      platformId = bean.platformId,
      name = bean.name,
      clientId = bean.clientId,
      authUrl = new URI(bean.authUrl).toURL,
      keysetUrl = new URI(bean.keysetUrl).toURL,
      usernamePrefix = bean.usernamePrefix,
      usernameSuffix = bean.usernameSuffix,
      usernameClaim = bean.usernameClaim,
      unknownUserHandling =
        (UnknownUserHandling.withName(bean.unknownUserHandling), bean.unknownUserDefaultGroups),
      instructorRoles = bean.instructorRoles,
      unknownRoles = bean.unknownRoles,
      customRoles = bean.customRoles,
      allowExpression = bean.allowExpression
    )
}
