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

package com.tle.web.api.dashboard.bean

import com.tle.core.dashboard.model.{
  FormattedTextPortlet,
  PortletBase,
  PortletColumn,
  PortletDetails,
  RecentContributionsPortlet,
  TaskStatisticsPortlet
}

/** DTO for the common details shared by all portlet types.
  */
final case class PortletBaseBean(
    uuid: String,
    name: String,
    isInstitutionWide: Boolean,
    isClosed: Boolean,
    isMinimised: Boolean,
    canClose: Boolean,
    canDelete: Boolean,
    canEdit: Boolean,
    canMinimise: Boolean,
    column: Int,
    order: Int
)

object PortletBaseBean {
  // Convert the portlet column to its integer representation.
  private def column(col: PortletColumn.Value) = col match {
    case PortletColumn.left  => 0
    case PortletColumn.right => 1
  }

  def apply(details: PortletBase): PortletBaseBean =
    PortletBaseBean(
      uuid = details.uuid,
      name = details.name,
      isInstitutionWide = details.isInstitutionWide,
      isClosed = details.isClosed,
      isMinimised = details.isMinimised,
      canClose = details.canClose,
      canDelete = details.canDelete,
      canEdit = details.canEdit,
      canMinimise = details.canMinimise,
      column = column(details.column),
      order = details.order
    )
}

/** Basic DTO structure for all portlets, including common details and type.
  */
sealed trait PortletResponse {
  def commonDetails: PortletBaseBean
  def portletType: String
}

/** DTO for portlets that do not have any additional configurations.
  */
final case class BasicPortletBean(
    commonDetails: PortletBaseBean,
    portletType: String
) extends PortletResponse

/** DTO for Formatted Text portlets, including the raw HTML content.
  */
final case class FormattedTextPortletBean(
    commonDetails: PortletBaseBean,
    portletType: String,
    rawHtml: String
) extends PortletResponse

/** DTO for Recent Contributions portlets, including all the configured Item search criteria.
  */
final case class RecentContributionsPortletBean(
    commonDetails: PortletBaseBean,
    portletType: String,
    collectionUuids: Option[List[String]],
    query: Option[String],
    maxAge: Option[Int],
    itemStatus: Option[String],
    isShowTitleOnly: Boolean
) extends PortletResponse

/** DTO for Task Statistics portlets, including the configured trend period.
  */
final case class TaskStatisticsPortletBean(
    commonDetails: PortletBaseBean,
    portletType: String,
    trend: String
) extends PortletResponse

object PortletResponse {
  def apply(details: PortletDetails): PortletResponse = details match {
    case p @ RecentContributionsPortlet(
          commonDetails,
          collectionUuids,
          query,
          maxAge,
          itemStatus,
          isShowTitleOnly
        ) =>
      RecentContributionsPortletBean(
        commonDetails = PortletBaseBean(commonDetails),
        portletType = p.portletType.toString,
        collectionUuids = collectionUuids,
        query = query,
        maxAge = maxAge,
        itemStatus = itemStatus.map(_.toString),
        isShowTitleOnly = isShowTitleOnly
      )
    case p @ TaskStatisticsPortlet(commonDetails, trend) =>
      TaskStatisticsPortletBean(
        commonDetails = PortletBaseBean(commonDetails),
        portletType = p.portletType.toString,
        trend = trend.toString
      )
    case p @ FormattedTextPortlet(commonDetails, rawHtml) =>
      FormattedTextPortletBean(
        commonDetails = PortletBaseBean(commonDetails),
        portletType = p.portletType.toString,
        rawHtml = rawHtml
      )
    case other =>
      BasicPortletBean(
        commonDetails = PortletBaseBean(other.commonDetails),
        portletType = other.portletType.toString
      )
  }
}
