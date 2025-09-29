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

package com.tle.web.template

import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid
import io.lemonlabs.uri.{QueryString, RelativeUrl}

/** Centralises the various routes for the New UI - especially useful when doing `forwardToUrl`.
  */
object NewUiRoutes {
  val PATH_DASHBOARD: String          = "page/home"
  val PATH_MYRESOURCES: String        = "page/myresources"
  val PATH_BROWSE_HIERARCHIES: String = "page/hierarchies"
  val PATH_HIERARCHY: String          = "page/hierarchy"

  /** Builds a relative URL for the New UI Hierarchy pages.
    *
    * @param legacyCompoundUuid
    *   The legacy UUID of the hierarchy to view. Example:
    *   "b3b3b6d8-fccd-47ef-b62a-8df05186a02c:A+James"
    */
  def hierarchy(legacyCompoundUuid: String): String =
    s"$PATH_HIERARCHY/${HierarchyCompoundUuid.applyWithLegacyFormat(legacyCompoundUuid).buildString()}"

  private def buildRelativeUrl(path: String, params: Vector[(String, Option[String])]): String =
    RelativeUrl.parse(path).withQueryString(QueryString(params)).toString()

  /** Builds a relative URL for the New UI My Resources pages, selecting which one by what is
    * specified in `myResourcesType`. Optionally, a 'sub status' can be specified to limit what
    * status to use on the 'Moderation queue' page, and/or a 'state ID' can be added to assist with
    * persisting state on the client side between calls that take a round trip into Legacy UI pages
    * \- such as Scrapbook creation and editing.
    *
    * @param myResourcesType
    *   should be one of the accepted `type` param values.
    * @param subStatus
    *   should be one of the standard ItemStatus values and will be passed in the `mstatus` param.
    * @param stateId
    *   Typically the UUID which was generated in an initial request to the server which is to then
    *   be returned in the `newUIStateId` param.
    * @return
    *   a relative URL in string form suitable for use with `forwardTo` type calls
    */
  def myResources(
      myResourcesType: String,
      subStatus: Option[String] = None,
      stateId: Option[String] = None
  ): String = {
    val params =
      Vector(("type", Some(myResourcesType)), ("mstatus", subStatus), ("newUIStateId", stateId))
        .filter({ case (_, v) =>
          v.isDefined
        })
    buildRelativeUrl(path = PATH_MYRESOURCES, params)
  }
}
