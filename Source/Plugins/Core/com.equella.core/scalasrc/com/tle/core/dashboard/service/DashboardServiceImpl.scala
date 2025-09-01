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

package com.tle.core.dashboard.service

import cats.implicits._
import com.tle.common.i18n.LangUtils
import com.tle.common.portal.entity.Portlet
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.dashboard.model._
import com.tle.core.dashboard.service.DashboardService.DASHBOARD_LAYOUT
import com.tle.core.guice.Bind
import com.tle.core.portal.service.PortletService
import com.tle.core.settings.service.ConfigurationService
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Singleton
@Bind(classOf[DashboardService])
class DashboardServiceImpl @Inject() (
    portletService: PortletService,
    configurationService: ConfigurationService
) extends DashboardService {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[DashboardServiceImpl])

  override def getViewablePortlets: List[PortletDetails] = {
    val result: List[Either[String, PortletDetails]] =
      portletService.getViewablePortletsForDisplay.asScala.toList
        .map(buildPortletDetails)

    val (errors, portlets) = result.separate
    errors.foreach(LOGGER.error)

    portlets
  }

  override def getDashboardLayout: Option[DashboardLayout.Value] = {
    val layout: String = configurationService.getProperty(DASHBOARD_LAYOUT)
    Try(Option(layout).map(DashboardLayout.withName)) match {
      case Success(value) => value
      case Failure(_) =>
        LOGGER.error(
          "Invalid dashboard layout '{}' found. Default to the single-column layout.",
          layout
        )
        Some(DashboardLayout.SingleColumn)
    }
  }

  override def updateDashboardLayout(layout: DashboardLayout.Value): Either[String, Unit] = {
    Either
      .catchNonFatal {
        configurationService.setProperty(DASHBOARD_LAYOUT, layout.toString)
      }
      .leftMap(e => s"Failed to update Dashboard layout: ${e.getMessage}")
  }

  def buildPortletDetails(portlet: Portlet): Either[String, PortletDetails] = {

    def commonDetails: PortletBase = {
      val nonGuestUser: Boolean = !CurrentUser.wasAutoLoggedIn && !CurrentUser.isGuest
      val isOwner               = portlet.getOwner == CurrentUser.getUserID
      val preference            = Option(portletService.getPreference(portlet))

      PortletBase(
        uuid = portlet.getUuid,
        name = LangUtils.getString(portlet.getName),
        isInstitutionWide = portlet.isInstitutional,
        isClosed = preference.exists(_.isClosed),
        isMinimised = preference.exists(_.isMinimised),
        // Only institution-wide portlets can be closed by non-guest users
        canClose = nonGuestUser && portlet.isCloseable && portlet.isInstitutional,
        // Only private portlets can be deleted/edited by their owners.
        canDelete = isOwner && !portlet.isInstitutional && portletService.canDelete(portlet),
        canEdit = isOwner && !portlet.isInstitutional && portletService.canEdit(portlet),
        canMinimise = nonGuestUser && portlet.isMinimisable,
        column = preference.map(_.getPosition).getOrElse(0),
        order = preference.map(_.getOrder).getOrElse(0)
      )
    }

    def buildPortlet(portletType: PortletType.Value): Either[String, PortletDetails] =
      portletType match {
        case PortletType.recent         => RecentContributionsPortlet(portlet, commonDetails)
        case PortletType.taskstatistics => TaskStatisticsPortlet(portlet, commonDetails)
        case PortletType.html =>
          Right(FormattedTextPortlet(commonDetails, rawHtml = portlet.getConfig))
        case PortletType.browse      => Right(BrowsePortlet(commonDetails))
        case PortletType.favourites  => Right(FavouritesPortlet(commonDetails))
        case PortletType.freemarker  => Right(ScriptedPortlet(commonDetails))
        case PortletType.myresources => Right(MyResourcesPortlet(commonDetails))
        case PortletType.search      => Right(SearchPortlet(commonDetails))
        case PortletType.tasks       => Right(TasksPortlet(commonDetails))
        case PortletType.iframe      => Left("Web page portlet has been deprecated since 2025.2")
        case PortletType.rss         => Left("RSS portlet has been deprecated since 2025.2")
      }

    val portletType: String = portlet.getType

    Try(PortletType.withName(portletType)) match {
      case Success(t) => buildPortlet(t)
      case Failure(_) =>
        Left(s"Invalid portlet type '$portletType' configured for Portlet ${portlet.getUuid}")
    }
  }
}
