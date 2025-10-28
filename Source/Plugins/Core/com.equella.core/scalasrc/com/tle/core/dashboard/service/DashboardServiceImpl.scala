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
import com.tle.common.beans.exception.NotFoundException
import com.tle.common.i18n.LangUtils
import com.tle.common.portal.entity.{Portlet, PortletPreference}
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.dashboard.model.PortletCreatable.fromDescriptor
import com.tle.core.dashboard.model._
import com.tle.core.dashboard.service.DashboardService.DASHBOARD_LAYOUT
import com.tle.core.guice.Bind
import com.tle.core.portal.service.PortletService
import com.tle.core.services.user.UserPreferenceService
import com.tle.exceptions.AccessDeniedException
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Singleton
@Bind(classOf[DashboardService])
class DashboardServiceImpl @Inject() (
    portletService: PortletService,
    userPreferenceService: UserPreferenceService
) extends DashboardService {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[DashboardServiceImpl])

  override def getViewablePortlets: List[PortletDetails] = {
    val result: List[Either[String, PortletDetails]] =
      portletService.getViewablePortletsForDisplay.asScala.toList
        .map(p => (p, Option(portletService.getPreference(p))))
        .filter { case (_, preference) =>
          preference.isEmpty || preference.exists(_.isClosed == false)
        }
        .map { case (portlet, preference) =>
          buildPortletDetails(portlet, preference)
        }

    val (errors, portlets) = result.separate
    errors.foreach(LOGGER.error)

    portlets
  }

  override def getDashboardLayout: Option[DashboardLayout.Value] = {
    val layout: String = userPreferenceService.getPreference(DASHBOARD_LAYOUT)
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
        userPreferenceService.setPreference(DASHBOARD_LAYOUT, layout.toString)
      }
      .leftMap(e => s"Failed to update Dashboard layout: ${e.getMessage}")
  }

  def buildPortletDetails(
      portlet: Portlet,
      preference: Option[PortletPreference]
  ): Either[String, PortletDetails] = {

    def commonDetails: PortletBase = {
      val nonGuestUser: Boolean = !CurrentUser.wasAutoLoggedIn && !CurrentUser.isGuest
      val isOwner               = portlet.getOwner == CurrentUser.getUserID

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
        column = preference.map(getPortletColumn).getOrElse(PortletColumn.left),
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

  override def getCreatablePortlets: List[PortletCreatable] = {
    // portletService.listContributableTypes requires a flag to know whether the request comes from the Portlet
    // Management page or Dashboard. Since this method is implemented for Dashboard, set that flag to false.
    val result: List[Either[String, PortletCreatable]] =
      portletService
        .listContributableTypes(false)
        .asScala
        .toList
        .map(fromDescriptor)

    // Log errors for portlet types that cannot be created, exclude the deprecated types, and return the rest.
    val (errors, creatables) = result.separate
    val deprecated           = Set(PortletType.iframe, PortletType.rss)

    errors.foreach(LOGGER.error)
    creatables
      .filterNot(p => deprecated.contains(p.portletType))
      .sortBy(_.name)
  }

  override def getClosedPortlets: List[PortletClosed] = {
    portletService.getViewableButClosedPortlets.asScala.toList.map(PortletClosed(_))
  }

  override def updatePortletPreference(
      uuid: String,
      updates: PortletPreferenceUpdate
  ): Either[Throwable, Unit] = {
    Option(portletService.getByUuid(uuid)) match {
      case Some(portlet) =>
        Either.catchNonFatal {
          portletService.updatePreference(portlet, updates)
        }
      case None =>
        Left(new NotFoundException(s"Portlet with UUID $uuid not found"))
    }
  }

  override def deletePortlet(uuid: String): Either[Throwable, Unit] = {
    Option(portletService.getByUuid(uuid)) match {
      case Some(portlet) if portlet.getOwner == CurrentUser.getUserID =>
        Either.catchNonFatal {
          portletService.delete(portlet, true)
        }
      case Some(_) => Left(new AccessDeniedException(s"No permission to delete portlet $uuid."))
      case None    => Left(new NotFoundException(s"Portlet with UUID $uuid not found"))
    }
  }

  /** Determine which column the portlet should be displayed, based on its preference.
    *
    * Since the legacy portlet positions are represented by integers, ranging from 0 to 3, the new
    * layout also uses integers 0 and 1 to represent the two columns 'left' and 'right',
    * respectively. Therefore, if the portlet position is 1, it should be displayed in the right
    * column. For all other cases, the portlet should be displayed in the left column.
    *
    * This logic will break how portlets are displayed after OEQ is upgraded to 2025.2, but this is
    * expected, and users can easily re-configure portlet positions in the new layout.
    */
  private def getPortletColumn(pref: PortletPreference): PortletColumn.Value = {
    pref.getPosition match {
      case 1         => PortletColumn.right
      case 0 | 2 | 3 => PortletColumn.left
      case invalidPos =>
        LOGGER.warn(
          s"Invalid portlet position {} found for portlet {}. Default to the left column.",
          invalidPos,
          pref.getPortlet.getUuid
        )
        PortletColumn.left
    }
  }
}
