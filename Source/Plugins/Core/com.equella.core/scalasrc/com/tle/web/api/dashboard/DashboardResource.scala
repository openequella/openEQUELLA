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

package com.tle.web.api.dashboard

import com.tle.core.dashboard.service.{DashboardLayout, DashboardService}
import com.tle.core.guice.Bind
import com.tle.web.api.ApiErrorResponse.{badRequest, serverError}
import com.tle.web.api.dashboard.bean.{PortletClosedBean, PortletCreatableBean, PortletResponse}
import io.swagger.annotations.{Api, ApiModelProperty, ApiOperation}
import org.jboss.resteasy.annotations.cache.NoCache

import javax.inject.{Inject, Singleton}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, PUT, Path, Produces}
import scala.util.{Failure, Success, Try}

final case class DashboardResponse(
    portlets: List[PortletResponse],
    layout: Option[String]
)

final case class DashboardLayoutUpdate(
    @ApiModelProperty(
      allowableValues = "SingleColumn, TwoEqualColumns, TwoColumnsRatio2to1, TwoColumnsRatio1to2"
    )
    layout: String
)

@Bind
@Singleton
@NoCache
@Path("dashboard")
@Produces(Array("application/json"))
@Api("Dashboard")
class DashboardResource @Inject() (dashboardService: DashboardService) {

  @GET
  @ApiOperation(
    value = "Dashboard configuration",
    notes = "Retrieve a list of viewable portlets and the dashboard layout",
    response = classOf[DashboardResponse]
  )
  def dashboard: Response = {
    val layout   = dashboardService.getDashboardLayout.map(_.toString)
    val portlets = dashboardService.getViewablePortlets.map(PortletResponse(_))

    Response.ok(DashboardResponse(portlets, layout)).build()
  }

  @PUT
  @Path("layout")
  @ApiOperation(value = "Update Dashboard layout")
  def layout(payload: DashboardLayoutUpdate): Response = {
    def update(layout: DashboardLayout.Value): Response =
      dashboardService.updateDashboardLayout(layout) match {
        case Right(_)       => Response.noContent().build()
        case Left(errorMsg) => serverError(errorMsg)
      }

    val newLayout = payload.layout
    Try(DashboardLayout.withName(newLayout)) match {
      case Success(layout) => update(layout)
      case Failure(_)      => badRequest(s"Invalid Dashboard layout: $newLayout")
    }
  }

  @GET
  @Path("portlet/creatable")
  @ApiOperation(
    value = "List of creatable portlets",
    notes = "Retrieve a list of portlet types that can be added to the dashboard",
    response = classOf[PortletCreatableBean],
    responseContainer = "List"
  )
  def creatable(): Response = {
    val portlets: List[PortletCreatableBean] =
      dashboardService.getCreatablePortlets.map(PortletCreatableBean(_))
    Response.ok(portlets).build()
  }

  @GET
  @Path("portlet/closed")
  @ApiOperation(
    value = "List of closed portlets",
    notes = "Retrieve the IDs and names of portlets that have been closed on the dashboard",
    response = classOf[PortletClosedBean],
    responseContainer = "List"
  )
  def closed(): Response = {
    val closedPortlets: List[PortletClosedBean] =
      dashboardService.getClosedPortlets.map(PortletClosedBean(_))
    Response.ok(closedPortlets).build()
  }
}
