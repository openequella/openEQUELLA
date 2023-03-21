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

package com.tle.web.api.lti

import cats.data.Validated
import com.tle.beans.lti.{LtiCustomRole, LtiPlatform}
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
    unknownUserDefaultGroups: Set[String],
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
      unknownUserDefaultGroups = platform.unknownUserDefaultGroups.asScala.toSet,
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
  private def updateRoleMapping(oldMappings: java.util.Set[LtiCustomRole],
                                updates: Map[String, Set[String]]) =
    updates
      .map {
        case (ltiRole, newTarget) =>
          Option(oldMappings).map(_.asScala).flatMap(_.find(_.ltiRole == ltiRole)) match {
            case Some(oldMapping) =>
              oldMapping.oeqRoles = newTarget.asJava
              oldMapping
            case None =>
              val newMapping = new LtiCustomRole
              newMapping.ltiRole = ltiRole
              newMapping.oeqRoles = newTarget.asJava
              newMapping
          }
      }
      .toSet
      .asJava

  // Populate all the fields of LtiPlatform.
  private def updatePlatform(platform: LtiPlatform, params: LtiPlatformBean) = {
    platform.platformId = params.platformId
    platform.clientId = params.clientId
    platform.authUrl = params.authUrl
    platform.keysetUrl = params.keysetUrl
    platform.unknownUserHandling = params.unknownUserHandling

    platform.usernamePrefix = params.usernamePrefix.orNull
    platform.usernameSuffix = params.usernameSuffix.orNull
    platform.allowExpression = params.allowExpression.orNull

    platform.unknownRoles = params.unknownRoles.asJava
    platform.unknownUserDefaultGroups = params.unknownUserDefaultGroups.asJava
    platform.instructorRoles = params.instructorRoles.asJava

    platform.customRoles = updateRoleMapping(platform.customRoles, params.customRoles)

    platform
  }

  /**
    * Curried function to build a new instance of LtiPlatform based on the provided LtiPlatformBean.
    */
  val buildLtiPlatformFromParams: LtiPlatformBean => LtiPlatform = params => {
    val platform = updatePlatform(new LtiPlatform, params)
    platform.institution = CurrentInstitution.get()
    platform
  }

  /**
    * Curried function to update the provided LtiPlatform with an instance of LtiPlatformBean.
    */
  val updateLtiPlatformWithParams: (LtiPlatform) => (LtiPlatformBean) => LtiPlatform =
    platform => params => updatePlatform(platform, params)

  /**
    * Check values of mandatory fields for LtiPlatformBean and accumulate all the errors.
    * Return a ValidatedNel which is either a list of error messages or the checked LtiPlatformBean.
    */
  def validate(params: LtiPlatformBean): Validated[List[String], LtiPlatformBean] = {
    def checkIDs =
      Map(
        ("platform ID", params.platformId),
        ("client ID", params.clientId),
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
        ("AUTH URL", params.authUrl),
        ("Key set URL", params.keysetUrl)
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
        UnknownUserHandling.withName(params.unknownUserHandling)
      }.toEither
        .leftMap(err => s"Unknown handling for unknown users: ${err.getMessage}")
        .toValidatedNel

    (checkIDs, checkUrls, checkUnknownUserHandling).mapN((_, _, _) => params).leftMap(_.toList)
  }
}
