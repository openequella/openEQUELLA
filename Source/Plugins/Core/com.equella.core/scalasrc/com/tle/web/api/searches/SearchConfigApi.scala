/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.searches

import java.util.UUID

import cats.data.OptionT
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.settings.SettingsDB
import com.tle.web.api.ApiHelper
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs._
import javax.ws.rs.core.Response.Status

@Api("Search page configuration")
@Path("searches")
@Produces(Array("application/json"))
class SearchConfigApi {

  @GET
  @Path("config/{uuid}")
  @ApiOperation(value = "Get search configuration", response = classOf[SearchConfig])
  def getConfig(@PathParam("uuid") configId: UUID): Response = ApiHelper.runAndBuild {
    ApiHelper.entityOrNotFoundDB(SearchConfigDB.readConfig(configId))
  }

  @PUT
  @Path("config/{uuid}")
  @ApiOperation(value = "Edit search configuration")
  def editConfig(@PathParam("uuid") configId: UUID, config: SearchConfig): Response = {
    ApiHelper.runAndBuild {
      SearchConfigDB.writeConfig(configId, config).map(_ => Response.ok())
    }
  }

  @POST
  @Path("config")
  @ApiOperation(value = "Create new search configuration")
  def newConfig(config: SearchConfig): Response = {
    val newID = UUID.randomUUID()
    ApiHelper.runAndBuild {
      SearchConfigDB.writeConfig(newID, config).map(_ => Response.ok().header("X-UUID", newID))
    }
  }

  @GET
  @Path("page/{pagename}/resolve")
  @ApiOperation("Resolve configuration for a page")
  def resolveConfig(@PathParam("pagename") pagename: String): Response = ApiHelper.runAndBuild {
    for {
      config <- SearchConfigDB.readPageConfig(pagename).flatMap { sc =>
        SearchConfigDB.readConfig(sc.configId)
      }.value
    } yield {
      (config, SearchDefaults.defaultMap.get(pagename)) match {
        case (Some(c), Some(d)) => Response.ok(SearchDefaults.mergeDefaults(d, c))
        case (a, b) => ApiHelper.entityOrNotFound(a.orElse(b))
      }
    }
  }

  @GET
  @Path("page/{pagename}")
  @ApiOperation("Read configuration association for a page")
  def readPageConfig(@PathParam("pagename") pagename: String): Response = ApiHelper.runAndBuild {
    ApiHelper.entityOrNotFoundDB(SearchConfigDB.readPageConfig(pagename))
  }

  @PUT
  @Path("page/{pagename}")
  @ApiOperation("Edit page configuration association")
  def editPageConfig(@PathParam("pagename") pagename: String, config: SearchPageConfig): Response = ApiHelper.runAndBuild {
    SearchConfigDB.writePageConfig(pagename, config).map(_ => Response.ok())
  }
}
