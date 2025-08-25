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

import com.tle.core.dashboard.service.DashboardService
import com.tle.core.guice.Bind
import com.tle.web.api.dashboard.bean.PortletResponse
import io.swagger.annotations.{Api, ApiOperation}
import org.jboss.resteasy.annotations.cache.NoCache

import javax.inject.{Inject, Singleton}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}

final case class DashboardResponse(
    portlets: List[PortletResponse],
    layout: Option[String]
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
}
