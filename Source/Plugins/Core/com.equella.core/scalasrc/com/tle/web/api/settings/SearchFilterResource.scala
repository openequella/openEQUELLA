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
import com.dytech.edge.common.Constants
import com.tle.beans.mime.MimeEntry
import com.tle.common.Check
import com.tle.common.settings.standard.SearchSettings
import com.tle.common.settings.standard.SearchSettings.SearchFilter
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.settings.SettingsApiHelper.{loadSettings, updateSettings}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces, QueryParam}
import org.jboss.resteasy.annotations.cache.NoCache
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

case class GeneralFilter(ownerFilterEnabled: Boolean, dateModifiedFilterEnabled: Boolean)

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SearchFilterResource {

  val searchPrivProvider = LegacyGuice.searchPrivProvider

  @GET
  @Path("search/filter")
  @ApiOperation(
    value = "List MIME type filters",
    notes = "This endpoint is used to retrieve all MIME type filters.",
    response = classOf[SearchFilter],
    responseContainer = "List"
  )
  def listMimeTypeFilters: Response = {
    searchPrivProvider.checkAuthorised()
    val filters = loadSettings(new SearchSettings).getFilters
    Response.ok().entity(filters).build()
  }

  @GET
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Retrieve a MIME type filter",
    notes = "This endpoint is used to retrieve one MIME type filter.",
    response = classOf[SearchFilter]
  )
  def getMimeTypeFilter(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) => Response.ok().entity(filter).build()
      case None         => ApiErrorResponse.resourceNotFound(uuidNotFound(uuid))
    }
  }

  @POST
  @Path("search/filter")
  @ApiOperation(
    value = "Add a MIME type filter",
    notes =
      "This endpoint is used to add a MIME type filter. A JSON object representing the new filter is returned if operation is successful.",
    response = classOf[SearchFilter]
  )
  def addMimeTypeFilter(
      @ApiParam(value = "filter name", required = true) @QueryParam("name") name: String,
      @ApiParam(value = "filter types", required = true) @QueryParam("mimeTypes") mimeTypes: java.util.List[
        String]): Response = {
    searchPrivProvider.checkAuthorised()
    validate(name, mimeTypes) match {
      case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
      case Right(_) =>
        val searchFilter = new SearchFilter
        searchFilter.setId(UUID.randomUUID().toString)
        searchFilter.setName(name)
        searchFilter.setMimeTypes(mimeTypes)

        // Adding a search filter is essentially a update of the search setting
        val searchSetting = loadSettings(new SearchSettings)
        searchSetting.getFilters.add(searchFilter)
        updateSettings(searchSetting)
        Response.status(Status.CREATED).entity(searchFilter).build()
    }
  }

  @PUT
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Update a MIME type filter",
    notes =
      "This endpoint is used to update a MIME type filter. A JSON object representing the updated filter is returned if operation is successful.",
    response = classOf[SearchFilter]
  )
  def updateMimeTypeFilter(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID,
      @ApiParam(value = "filter name", required = true) @QueryParam("name") name: String,
      @ApiParam(value = "filter types", required = true) @QueryParam("mimeTypes") mimeTypes: java.util.List[
        String]): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) =>
        validate(name, mimeTypes) match {
          case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
          case Right(_) =>
            filter.setMimeTypes(mimeTypes)
            filter.setName(name)
            updateSettings(searchSettings)
            Response.ok().entity(filter).build()
        }
      case None => ApiErrorResponse.resourceNotFound(uuidNotFound(uuid))
    }
  }

  @DELETE
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Delete a MIME type filter",
    notes = "This endpoint is used to delete a MIME type filter.",
  )
  def deleteMimeTypeFilter(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)

    getFilterById(uuid, searchSettings) match {
      case Some(filter) =>
        searchSettings.getFilters.remove(filter)
        updateSettings(searchSettings)
        Response.ok().build()
      case None => ApiErrorResponse.resourceNotFound(uuidNotFound(uuid))
    }
  }

  @GET
  @Path("search/general-filter")
  @ApiOperation(
    value = "Check status of general filters",
    notes = "This endpoint is used to check the status of Owner filter and Date-modified filer).",
    response = classOf[GeneralFilter]
  )
  def getGeneralFilters: Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    Response
      .ok()
      .entity(GeneralFilter(searchSettings.isOwnerFilter, searchSettings.isDateModifiedFilter))
      .build()
  }

  @PUT
  @Path("search/general-filter")
  @ApiOperation(
    value = "Update status of general filters",
    notes = "This endpoint is used to update the status of Owner filter and Date-modified filer."
  )
  def updateOwnerFilter(
      @ApiParam(required = true) @QueryParam("enableOwnerFilter") enableOwnerFilter: Boolean,
      @ApiParam(required = true) @QueryParam("enableDateModifiedFilter") enableDateModifiedFilter: Boolean)
    : Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    searchSettings.setOwnerFilter(enableOwnerFilter)
    searchSettings.setDateModifiedFilter(enableDateModifiedFilter)
    updateSettings(searchSettings)
    Response.noContent().build()
  }

  private def getFilterById(filterId: UUID,
                            searchSettings: SearchSettings): Option[SearchFilter] = {
    Option(searchSettings.getSearchFilter(filterId.toString))
  }

  private def uuidNotFound(uuid: UUID) = s"No Search filters matching UUID: $uuid."

  private def validate(filterName: String,
                       mimeTypes: java.util.List[String]): Either[Array[String], Unit] = {
    val errorMessages = ArrayBuffer[String]()

    if (Check.isEmpty(filterName)) {
      errorMessages += "Filter name cannot be empty."
    }

    if (Check.isEmpty(mimeTypes)) {
      errorMessages += "Need at least one MIME type."
    } else {
      if (mimeTypes.asScala.exists(_.isEmpty)) {
        errorMessages += "Value of MIME type cannot be empty."
      }
      // Find out MIMETypes of which the values are non-empty but invalid
      val invalidTypes = mimeTypes.asScala.filter(mimeType =>
        mimeType.nonEmpty && !validMimeTypes.contains(mimeType))
      if (invalidTypes.nonEmpty) {
        errorMessages += s"Invalid MIMETypes found : ${invalidTypes.mkString(",")}"
      }
    }

    if (errorMessages.nonEmpty) Left(errorMessages.toArray) else Right()
  }

  private def validMimeTypes: List[String] = {
    val mimeEntries: Seq[MimeEntry] =
      LegacyGuice.mimeTypeService.searchByMimeType(Constants.BLANK, 0, -1).getResults.asScala
    mimeEntries.map(_.getType).toList
  }
}
