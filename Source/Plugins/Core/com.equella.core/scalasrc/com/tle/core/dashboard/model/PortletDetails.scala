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

package com.tle.core.dashboard.model

import cats.implicits._
import com.tle.beans.item.ItemStatus
import com.tle.common.portal.entity.Portlet
import com.tle.common.portal.entity.impl.PortletRecentContrib
import com.tle.common.workflow.Trend
import com.tle.web.portal.standard.editor.RecentContribPortletEditorSection.{
  KEY_TITLEONLY,
  ITEM_STATUS
}
import com.tle.web.workflow.portal.TaskStatisticsPortletEditor.KEY_DEFAULT_TREND

import scala.jdk.CollectionConverters._

/** Enum for all the Portlet types defined in Legacy Portlet Sections.
  *
  * Examples:
  *   - com.tle.web.favourites.portal.FavouritesPortletEditor#TYPE
  *   - com.tle.web.hierarchy.portlet.editor.BrowsePortletEditorSection#TYPE
  */
object PortletType extends Enumeration {
  val browse, favourites, freemarker, html, iframe, myresources, recent, rss, search, tasks,
      taskstatistics = Value
}

/** Common details shared by all portlet types.
  *
  * @param uuid
  *   UUID of the portlet
  * @param name
  *   Display name of the portlet
  * @param isInstitutionWide
  *   Whether the portlet is institution-wide
  * @param isClosed
  *   Whether the portlet is closed
  * @param isMinimised
  *   Whether the portlet is minimised
  * @param canClose
  *   Whether the portlet can be closed
  * @param canDelete
  *   Whether the portlet can be deleted
  * @param canEdit
  *   Whether the portlet can be edited
  * @param canMinimise
  *   Whether the portlet can be minimised
  * @param column
  *   The column the portlet is in
  * @param order
  *   The order of the portlet in the column
  */
final case class PortletBase(
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

/** Details of a portlet, including its type and common details.
  */
sealed trait PortletDetails {
  val commonDetails: PortletBase
  val portletType: PortletType.Value
}

final case class BrowsePortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.browse
}

final case class FavouritesPortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.favourites
}

final case class ScriptedPortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.freemarker
}

final case class FormattedTextPortlet(commonDetails: PortletBase, rawHtml: String)
    extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.html
}

final case class MyResourcesPortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.myresources
}

final case class SearchPortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.search
}

final case class TasksPortlet(commonDetails: PortletBase) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.tasks
}

/** Details for Task Statistics Portlet, including the common details and the configured trend
  * period.
  *
  * @param commonDetails
  *   Common details of the portlet.
  * @param trend
  *   Trend period used to calculate task statistics.
  */
final case class TaskStatisticsPortlet(commonDetails: PortletBase, trend: Trend)
    extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.taskstatistics
}

object TaskStatisticsPortlet {
  private def getTrend(portlet: Portlet): Either[String, Trend] =
    Option(portlet.getAttribute(KEY_DEFAULT_TREND)).filter(_.nonEmpty) match {
      case Some(trend) =>
        Either
          .catchNonFatal(Trend.valueOf(trend))
          .leftMap(_ => s"Unknown trend '$trend' configured for Portlet ${portlet.getUuid}")
      case None =>
        Left(s"No trend configured for Portlet ${portlet.getUuid}.")
    }

  def apply(portlet: Portlet, commonConfig: PortletBase): Either[String, TaskStatisticsPortlet] = {
    getTrend(portlet).map(TaskStatisticsPortlet(commonConfig, _))
  }
}

/** Details for Recent Contributions Portlet, including the common details and the configured Item
  * search criteria.
  *
  * @param commonDetails
  *   Common details of the portlet.
  * @param collectionUuids
  *   Optional list of Collection UUIDs used to filter the Items.
  * @param query
  *   Optional query string used to filter Items.
  * @param maxAge
  *   Optional maximum age (in days) used to restrict Items by their last modified date.
  * @param itemStatus
  *   Optional Item status value used to filter Items.
  * @param isShowTitleOnly
  *   Whether to show title only or both title and description for each Item in the portlet.
  */
final case class RecentContributionsPortlet(
    commonDetails: PortletBase,
    collectionUuids: Option[List[String]],
    query: Option[String],
    maxAge: Option[Int],
    itemStatus: Option[ItemStatus],
    isShowTitleOnly: Boolean
) extends PortletDetails {
  override val portletType: PortletType.Value = PortletType.recent
}

object RecentContributionsPortlet {
  private def getCollectionUuids(recentConfig: PortletRecentContrib): Option[List[String]] =
    Option(recentConfig.getCollections) map { collections =>
      collections.asScala.toList.map(_.getUuid)
    }

  private def getItemStatus(portlet: Portlet): Either[String, Option[ItemStatus]] = {
    Option(portlet.getAttribute(ITEM_STATUS))
      .filter(_.nonEmpty)
      .map(_.toUpperCase) match {
      case Some(status) =>
        Either
          .catchNonFatal(ItemStatus.valueOf(status))
          .map(Some(_))
          .leftMap(_ =>
            s"Unknown Item status '$status' configured for Recent Contribution Portlet '${portlet.getUuid}''"
          )
      case None => Right(None)
    }
  }

  private def getConfig(portlet: Portlet): Either[String, PortletRecentContrib] = {
    Option(portlet.getExtraData) match {
      case Some(data) =>
        Either
          .catchNonFatal(data.asInstanceOf[PortletRecentContrib])
          .leftMap(e =>
            s"Failed to retrieve the configuration of Recent Contribution portlet '${portlet.getUuid}': ${e.getMessage}"
          )
      case None =>
        Left(s"No configuration found for Recent Contribution portlet '${portlet.getUuid}'")
    }
  }

  private def showTitleOnly(portlet: Portlet): Boolean =
    Option(portlet.getAttribute(KEY_TITLEONLY)).contains(KEY_TITLEONLY)

  def apply(
      portlet: Portlet,
      commonConfig: PortletBase
  ): Either[String, RecentContributionsPortlet] = {
    for {
      recentConfig <- getConfig(portlet)
      itemStatus   <- getItemStatus(portlet)
    } yield RecentContributionsPortlet(
      commonDetails = commonConfig,
      collectionUuids = getCollectionUuids(recentConfig),
      query = Option(recentConfig.getQuery),
      maxAge = Option(recentConfig.getAgeDays),
      itemStatus = itemStatus,
      isShowTitleOnly = showTitleOnly(portlet)
    )
  }
}
