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

package com.tle.admin.rest

import io.circe.Decoder
import io.circe.generic.semiauto._
import org.slf4j.{Logger, LoggerFactory}
import sttp.client3.basicRequest
import sttp.client3.circe.asJson

// Copied from Source/Plugins/Core/com.equella.core/scalasrc/com/tle/web/api/LegacyContentApi.scala
// We don't want to have a dependency on the core module, so we're copying the code here.
final case class MenuItem(
    title: String,
    href: Option[String],
    systemIcon: Option[String],
    route: Option[String],
    iconUrl: Option[String],
    newWindow: Boolean
)

// Copied from Source/Plugins/Core/com.equella.core/scalasrc/com/tle/web/api/LegacyContentApi.scala
// We don't want to have a dependency on the core module, so we're copying the code here.
final case class ItemCounts(tasks: Int, notifications: Int)

// Copied from Source/Plugins/Core/com.equella.core/scalasrc/com/tle/web/api/LegacyContentApi.scala
// We don't want to have a dependency on the core module, so we're copying the code here.
final case class CurrentUserDetails(
    id: String,
    username: String,
    firstName: String,
    lastName: String,
    emailAddress: String,
    accessibilityMode: Boolean,
    autoLoggedIn: Boolean,
    guest: Boolean,
    prefsEditable: Boolean,
    menuGroups: Iterable[Iterable[MenuItem]],
    counts: Option[ItemCounts],
    canDownloadSearchResult: Boolean,
    roles: Iterable[String],
    scrapbookEnabled: Boolean
)

/** The `LegacyContentApi` object provides a client for the /api/content endpoints of the
  * openEQUELLA REST API.
  */
object LegacyContentApi {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(LegacyContentApi.getClass)
  private val API_PATH                = "content"

  implicit val menuItemDecoder: Decoder[MenuItem]                     = deriveDecoder
  implicit val itemCountsDecoder: Decoder[ItemCounts]                 = deriveDecoder
  implicit val currentUserDetailsDecoder: Decoder[CurrentUserDetails] = deriveDecoder

  /** Retrieves the details of the currently authenticated user.
    */
  def currentUserDetails(implicit cfg: RestConfiguration): Either[RestError, CurrentUserDetails] = {
    val request = basicRequest
      .get(cfg.apiUrl().addPath(API_PATH, "currentuser"))
      .response(asJson[CurrentUserDetails])
    handleResult(extractAction(request), sendWithCookies(request)) { response =>
      response.body match {
        case Left(error) =>
          LOGGER.error(s"Failed to decode current user details: ${error.getMessage}")
          Left(UnexpectedResponseError(error.getMessage))
        case Right(userDetails) =>
          LOGGER.debug(s"Retrieved current user details: ${userDetails.username}")
          Right(userDetails)
      }
    }
  }
}
