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

package com.tle.web.api.settings

import java.util.UUID
import com.tle.common.settings.standard.SearchSettings
import com.tle.common.settings.standard.SearchSettings.SearchFilter
import com.tle.legacy.LegacyGuice
import com.tle.web.api.settings.SettingsApiHelper.{loadSettings, updateSettings}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import javax.ws.rs.core.Response
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces, QueryParam}
import org.jboss.resteasy.annotations.cache.NoCache

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SearchFilterResource {

  val searchPrivProvider = LegacyGuice.searchPrivProvider

  @GET
  @Path("search/filter")
  @ApiOperation(
    value = "Load search filters",
    notes = "This endpoint is used to retrieve all search filters.",
    response = classOf[SearchFilter],
    responseContainer = "List"
  )
  def loadSearchFilters: Response = {
    searchPrivProvider.checkAuthorised()
    val filters = loadSettings(new SearchSettings).getFilters
    Response.ok().entity(filters).build()
  }

  @GET
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Load a search filter",
    notes = "This endpoint is used to retrieve one search filter.",
    response = classOf[SearchFilter]
  )
  def loadSearchFilter(@ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) => Response.ok().entity(filter).build()
      case None         => Response.status(404).build()
    }
  }

  @POST
  @Path("search/filter")
  @ApiOperation(
    value = "Add a search filter",
    notes = "This endpoint is used to add a search filter.",
  )
  def addSearchFilter(
      @ApiParam(value = "filter name", required = true) @QueryParam("name") name: String,
      @ApiParam(value = "filter types", required = true) @QueryParam("mimeTypes") mimeTypes: java.util.List[
        String]): Response = {
    val searchFilter = new SearchFilter
    searchFilter.setId(UUID.randomUUID().toString)
    searchFilter.setName(name)
    searchFilter.setMimeTypes(mimeTypes)
    loadSettings(new SearchSettings).getFilters.add(searchFilter)
    Response.status(201).build()
  }

  @PUT
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Update a search filter",
    notes = "This endpoint is used to update a search filter.",
  )
  def updateSearchFilter(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID,
      @ApiParam(value = "filter name", required = true) @QueryParam("name") name: String,
      @ApiParam(value = "filter types", required = true) @QueryParam("mimeTypes") mimeTypes: java.util.List[
        String]): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) =>
        filter.setMimeTypes(mimeTypes)
        filter.setName(name)
        updateSettings(searchSettings)
        Response.status(204).build()
      case None => Response.status(404).build()
    }
  }

  @DELETE
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Delete a search filter",
    notes = "This endpoint is used to delete a search filter.",
  )
  def deleteSearchFilters(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) =>
        searchSettings.getFilters.remove(filter)
        updateSettings(searchSettings)
        Response.ok().build()
      case None => Response.status(404).build()
    }
  }

  private def getFilterById(filterId: UUID,
                            searchSettings: SearchSettings): Option[SearchFilter] = {
    Option(searchSettings.getSearchFilter(filterId.toString))
  }
}
