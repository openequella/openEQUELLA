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

package com.tle.core.lti13.bean

import cats.data.Validated
import cats.implicits._
import com.tle.beans.lti.{LtiPlatform, LtiPlatformCustomRole}
import com.tle.common.Check
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.security.impl.AclExpressionEvaluator
import com.tle.integration.lti13.UnknownUserHandling
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.net.URL
import scala.jdk.CollectionConverters._

/** Data structure to represent LTI Platform details.
  *
  * @param platformId
  *   ID of the learning platform
  * @param name
  *   The name of the platform
  * @param clientId
  *   Client ID provided by the platform
  * @param authUrl
  *   The platform's authentication request URL
  * @param keysetUrl
  *   JWKS keyset URL where to get the keys
  * @param usernamePrefix
  *   Prefix added to the user ID from the LTI request
  * @param usernameSuffix
  *   Suffix added to the user ID from the LTI request
  * @param usernameClaim
  *   Optional value which specifies the claim to be used to retrieve username from the LTI request
  * @param unknownUserHandling
  *   How to handle unknown users by one of the three options - ERROR, GUEST OR CREATE.
  * @param unknownUserDefaultGroups
  *   The list of groups to be added to the user object If the unknown user handling is CREATE.
  * @param instructorRoles
  *   A list of roles to be assigned to a LTI instructor role
  * @param unknownRoles
  *   A list of roles to be assigned to a LTI role that is neither the instructor or in the list of
  *   custom roles
  * @param customRoles
  *   Mappings from LTI roles to OEQ roles
  * @param allowExpression
  *   The ACL Expression to control access from this platform
  * @param kid
  *   The activated key pair key ID (Readonly)
  * @param enabled
  *   `true` if the platform is enabled
  */
case class LtiPlatformBean(
    platformId: String,
    name: String,
    clientId: String,
    authUrl: String,
    keysetUrl: String,
    usernamePrefix: Option[String],
    usernameSuffix: Option[String],
    usernameClaim: Option[String],
    unknownUserHandling: String,
    unknownUserDefaultGroups: Option[Set[String]],
    instructorRoles: Set[String],
    unknownRoles: Set[String],
    customRoles: Map[String, Set[String]],
    allowExpression: Option[String],
    kid: Option[String],
    enabled: Boolean
)

object LtiPlatformBean {
  implicit val encoder = deriveEncoder[LtiPlatformBean]
  implicit val decoder = deriveDecoder[LtiPlatformBean]

  def apply(platform: LtiPlatform): LtiPlatformBean = {
    new LtiPlatformBean(
      platformId = platform.platformId,
      name = platform.name,
      clientId = platform.clientId,
      authUrl = platform.authUrl,
      keysetUrl = platform.keysetUrl,
      usernamePrefix = Option(platform.usernamePrefix),
      usernameSuffix = Option(platform.usernameSuffix),
      usernameClaim = Option(platform.usernameClaim),
      unknownUserHandling = platform.unknownUserHandling,
      unknownUserDefaultGroups = Option(platform.unknownUserDefaultGroups).map(_.asScala.toSet),
      instructorRoles = platform.instructorRoles.asScala.toSet,
      unknownRoles = platform.unknownRoles.asScala.toSet,
      customRoles = platform.customRoles.asScala
        .map(mapping => mapping.ltiRole -> mapping.oeqRoles.asScala.toSet)
        .toMap,
      allowExpression = Option(platform.allowExpression),
      kid = platform.keyPairs.asScala
        .filter(k => Option(k.deactivated).isEmpty)
        .headOption
        .map(_.keyId),
      enabled = platform.enabled
    )
  }

  // Populate all the fields of LtiPlatform.
  def populatePlatform(platform: LtiPlatform, bean: LtiPlatformBean): LtiPlatform = {
    platform.platformId = bean.platformId
    platform.name = bean.name
    platform.clientId = bean.clientId
    platform.authUrl = bean.authUrl
    platform.keysetUrl = bean.keysetUrl
    platform.unknownUserHandling = bean.unknownUserHandling

    platform.usernamePrefix = bean.usernamePrefix.orNull
    platform.usernameSuffix = bean.usernameSuffix.orNull
    platform.usernameClaim = bean.usernameClaim.orNull
    platform.allowExpression = bean.allowExpression.orNull

    platform.unknownRoles = bean.unknownRoles.asJava
    platform.unknownUserDefaultGroups =
      bean.unknownUserDefaultGroups.getOrElse(Set.empty[String]).asJava
    platform.instructorRoles = bean.instructorRoles.asJava

    platform.institution = CurrentInstitution.get()
    platform.enabled = bean.enabled

    val roleMappingUpdate = bean.customRoles
      .map { case (ltiRole, newTarget) =>
        LtiPlatformCustomRole(ltiRole, newTarget.asJava)
      }
      .toSet
      .asJava

    Option(platform.customRoles) match {
      case Some(mappings) =>
        mappings.clear()
        mappings.addAll(roleMappingUpdate)
      case None =>
        platform.customRoles = roleMappingUpdate
    }

    platform
  }

  /** Check values of mandatory fields and those having special requirements (e.g. maximum length)
    * for LtiPlatformBean and accumulate all the errors. Return a ValidatedNel which is either a
    * list of error messages or the checked LtiPlatformBean.
    */
  def validateLtiPlatformBean(bean: LtiPlatformBean): Validated[List[String], LtiPlatformBean] = {
    def checkIDs =
      Map(
        ("platform ID", bean.platformId),
        ("client ID", bean.clientId)
      ).map { case (fieldName, value) =>
        Option
          .unless(Check.isEmpty(value))(value)
          .toValidNel(s"Missing value for required field $fieldName")
      }.toList
        .sequence

    def checkUrls =
      Map(
        ("Auth URL", bean.authUrl),
        ("Key set URL", bean.keysetUrl)
      ).map { case (fieldName, value) =>
        Either
          .catchNonFatal {
            new URL(value)
          }
          .leftMap(err => s"Invalid value for $fieldName : ${err.getMessage}")
          .toValidatedNel
      }.toList
        .sequence

    def checkUnknownUserHandling =
      Either
        .catchNonFatal(UnknownUserHandling.withName(bean.unknownUserHandling))
        .leftMap(err => s"Unknown handling for unknown users: ${err.getMessage}")
        .toValidatedNel

    def checkACLExpression =
      bean.allowExpression match {
        case Some(expression) =>
          Either
            .cond(
              expression.length <= 255,
              expression,
              "ACL expression is too long (maximum 255 characters allowed)"
            )
            .flatMap { exp =>
              val evaluator = new AclExpressionEvaluator
              Either
                // We only check whether the provided ACl Expression is valid so what User State to be used
                // and whether the user is owner do not really matter.
                .catchNonFatal(evaluator.evaluate(exp, CurrentUser.getUserState, false))
                .leftMap(err => s"Invalid value for ACL expression: ${err.getMessage}")
            }
            .toValidatedNel
        case None => Validated.valid()
      }

    (checkIDs, checkUrls, checkUnknownUserHandling, checkACLExpression)
      .mapN((_, _, _, _) => bean)
      .leftMap(_.toList)
  }
}
