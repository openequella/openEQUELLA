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
import com.tle.beans.lti.{LtiPlatformCustomRole, LtiPlatform}
import com.tle.common.Check
import com.tle.common.institution.CurrentInstitution
import com.tle.integration.lti13.UnknownUserHandling
import cats.implicits._
import java.net.URL
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  * Data structure to represent LTI Platform details.
  *
  * @param platformId ID of the learning platform
  * @param clientId Client ID provided by the platform
  * @param authUrl The platform's authentication request URL
  * @param keysetUrl JWKS keyset URL where to get the keys
  * @param usernamePrefix Prefix added to the user ID from the LTI request
  * @param usernameSuffix Suffix added to the user ID from the LTI request
  * @param unknownUserHandling How to handle unknown users by one of the three options - ERROR, GUEST OR CREATE.
  * @param unknownUserDefaultGroups The list of groups to be added to the user object If the unknown user handling is CREATE.
  * @param instructorRoles A list of roles to be assigned to a LIT instructor role
  * @param unknownRoles  A list of roles to be assigned to a LTI role that is neither the instructor or in the list of custom roles
  * @param customRoles Mappings from LTI roles to OEQ roles
  * @param allowExpression The ACL Expression to control access from this platform
  */
case class LtiPlatformBean(
    platformId: String,
    clientId: String,
    authUrl: String,
    keysetUrl: String,
    usernamePrefix: Option[String],
    usernameSuffix: Option[String],
    unknownUserHandling: String,
    unknownUserDefaultGroups: Option[Set[String]],
    instructorRoles: Set[String],
    unknownRoles: Set[String],
    customRoles: Map[String, Set[String]],
    allowExpression: Option[String]
)

object LtiPlatformBean {
  def apply(platform: LtiPlatform): LtiPlatformBean = {
    LtiPlatformBean(
      platformId = platform.platformId,
      clientId = platform.clientId,
      authUrl = platform.authUrl,
      keysetUrl = platform.keysetUrl,
      usernamePrefix = Option(platform.usernamePrefix),
      usernameSuffix = Option(platform.usernameSuffix),
      unknownUserHandling = platform.unknownUserHandling,
      unknownUserDefaultGroups = Option(platform.unknownUserDefaultGroups).map(_.asScala.toSet),
      instructorRoles = platform.instructorRoles.asScala.toSet,
      unknownRoles = platform.unknownRoles.asScala.toSet,
      customRoles = platform.customRoles.asScala
        .map(mapping => mapping.ltiRole -> mapping.oeqRoles.asScala.toSet)
        .toMap,
      allowExpression = Option(platform.allowExpression)
    )
  }

  // Update the custom role Mapping. If the update has any LTI role that is already in the existing mapping, replace the target OEQ roles.
  // Otherwise, create a new mapping.
  private def updateRoleMapping(oldMappings: java.util.Set[LtiPlatformCustomRole],
                                updates: Map[String, Set[String]]) =
    updates
      .map {
        case (ltiRole, newTarget) =>
          Option(oldMappings).map(_.asScala).flatMap(_.find(_.ltiRole == ltiRole)) match {
            case Some(oldMapping) =>
              oldMapping.oeqRoles = newTarget.asJava
              oldMapping
            case None =>
              val newMapping = new LtiPlatformCustomRole
              newMapping.ltiRole = ltiRole
              newMapping.oeqRoles = newTarget.asJava
              newMapping
          }
      }
      .toSet
      .asJava

  // Populate all the fields of LtiPlatform.
  private def updatePlatform(platform: LtiPlatform, bean: LtiPlatformBean) = {
    platform.platformId = bean.platformId
    platform.clientId = bean.clientId
    platform.authUrl = bean.authUrl
    platform.keysetUrl = bean.keysetUrl
    platform.unknownUserHandling = bean.unknownUserHandling

    platform.usernamePrefix = bean.usernamePrefix.orNull
    platform.usernameSuffix = bean.usernameSuffix.orNull
    platform.allowExpression = bean.allowExpression.orNull

    platform.unknownRoles = bean.unknownRoles.asJava
    platform.unknownUserDefaultGroups =
      bean.unknownUserDefaultGroups.getOrElse(Set.empty[String]).asJava
    platform.instructorRoles = bean.instructorRoles.asJava

    platform.customRoles = updateRoleMapping(platform.customRoles, bean.customRoles)

    platform
  }

  /**
    * Function to build a new instance of LtiPlatform based on the provided LtiPlatformBean.
    */
  def buildLtiPlatformFromBean: LtiPlatformBean => LtiPlatform = bean => {
    val platform = updatePlatform(new LtiPlatform, bean)
    platform.institution = CurrentInstitution.get()
    platform
  }

  /**
    * Curried function to update the provided LtiPlatform with an instance of LtiPlatformBean.
    */
  def updateLtiPlatformWithBean: LtiPlatform => LtiPlatformBean => LtiPlatform =
    platform => bean => updatePlatform(platform, bean)

  /**
    * Check values of mandatory fields for LtiPlatformBean and accumulate all the errors.
    * Return a ValidatedNel which is either a list of error messages or the checked LtiPlatformBean.
    */
  def validate(bean: LtiPlatformBean): Validated[List[String], LtiPlatformBean] = {
    def checkIDs =
      Map(
        ("platform ID", bean.platformId),
        ("client ID", bean.clientId),
      ).map {
          case (key, value) =>
            Option
              .unless(Check.isEmpty(value))(value)
              .toValidNel(s"Missing value for required field $key")
        }
        .toList
        .sequence

    def checkUrls =
      Map(
        ("AUTH URL", bean.authUrl),
        ("Key set URL", bean.keysetUrl)
      ).map {
          case (key, value) =>
            Try {
              new URL(value)
            }.toEither.leftMap(err => s"Invalid value for $key : ${err.getMessage}")
        }
        .toList
        .traverse(_.toValidatedNel)

    def checkUnknownUserHandling =
      Try {
        UnknownUserHandling.withName(bean.unknownUserHandling)
      }.toEither
        .leftMap(err => s"Unknown handling for unknown users: ${err.getMessage}")
        .toValidatedNel

    (checkIDs, checkUrls, checkUnknownUserHandling).mapN((_, _, _) => bean).leftMap(_.toList)
  }
}
