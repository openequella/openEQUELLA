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
  PortletDetails,
  RecentContributionsPortlet,
  TaskStatisticsPortlet
}

/** Basic DTO structure for all portlets, including common details and type.
  */
sealed trait PortletResponse {
  def commonDetails: PortletBase
  def `type`: String
}

/** DTO for portlets that do not have any additional configurations.
  */
final case class BasicPortletBean(
    commonDetails: PortletBase,
    `type`: String
) extends PortletResponse

/** DTO for Formatted Text portlets, including the raw HTML content.
  */
final case class FormattedTextPortletBean(
    commonDetails: PortletBase,
    `type`: String,
    rawHtml: String
) extends PortletResponse

/** DTO for Recent Contributions portlets, including all the configured Item search criteria.
  */
final case class RecentContributionsPortletBean(
    commonDetails: PortletBase,
    `type`: String,
    collectionUuids: Option[List[String]],
    query: Option[String],
    maxAge: Option[Int],
    itemStatus: Option[String],
    isShowTitleOnly: Boolean
) extends PortletResponse

/** DTO for Task Statistics portlets, including the configured trend period.
  */
final case class TaskStatisticsPortletBean(
    commonDetails: PortletBase,
    `type`: String,
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
        commonDetails = commonDetails,
        `type` = p.portletType.toString,
        collectionUuids = collectionUuids,
        query = query,
        maxAge = maxAge,
        itemStatus = itemStatus.map(_.toString),
        isShowTitleOnly = isShowTitleOnly
      )
    case p @ TaskStatisticsPortlet(commonDetails, trend) =>
      TaskStatisticsPortletBean(
        commonDetails = commonDetails,
        `type` = p.portletType.toString,
        trend = trend.toString
      )
    case p @ FormattedTextPortlet(commonDetails, rawHtml) =>
      FormattedTextPortletBean(
        commonDetails = commonDetails,
        `type` = p.portletType.toString,
        rawHtml = rawHtml
      )
    case other =>
      BasicPortletBean(
        commonDetails = other.commonDetails,
        `type` = other.portletType.toString
      )
  }
}
