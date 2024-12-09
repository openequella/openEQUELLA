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
import com.tle.web.api.{ApiBatchOperationResponse, ApiErrorResponse}
import com.tle.web.api.settings.SettingsApiHelper.{loadSettings, updateSettings}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces, QueryParam}
import org.jboss.resteasy.annotations.cache.NoCache
import scala.jdk.CollectionConverters._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SearchFilterResource {

  val searchPrivProvider = LegacyGuice.searchPrivProvider

  @GET
  @Path("search/filter")
  @ApiOperation(
    value = "List search filters",
    notes = "This endpoint is used to retrieve all search filters.",
    response = classOf[SearchFilter],
    responseContainer = "List"
  )
  def listSearchFilters: Response = {
    val filters = loadSettings(new SearchSettings).getFilters
    Response.ok().entity(filters).build()
  }

  @GET
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Retrieve a search filter",
    notes = "This endpoint is used to retrieve one search filter.",
    response = classOf[SearchFilter]
  )
  def getSearchFilter(@ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID): Response = {
    val searchSettings = loadSettings(new SearchSettings)
    val filterId       = uuid.toString
    getFilterById(filterId, searchSettings) match {
      case Some(filter) => Response.ok().entity(filter).build()
      case None         => ApiErrorResponse.resourceNotFound(uuidNotFound(filterId))
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
  def addSearchFilter(searchFilter: SearchFilter): Response = {
    searchPrivProvider.checkAuthorised()
    validate(searchFilter) match {
      case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
      case Right(_) =>
        searchFilter.setId(UUID.randomUUID().toString)

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
    value = "Update a search filter",
    notes =
      "This endpoint is used to update a search filter. A JSON object representing the updated filter is returned if operation is successful.",
    response = classOf[SearchFilter]
  )
  def updateSearchFilter(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID,
      searchFilter: SearchFilter
  ): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    val filterId       = uuid.toString

    getFilterById(filterId, searchSettings) match {
      case Some(filter) =>
        validate(searchFilter) match {
          case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
          case Right(_) =>
            filter.setMimeTypes(searchFilter.getMimeTypes)
            filter.setName(searchFilter.getName)
            updateSettings(searchSettings)
            Response.ok().entity(filter).build()
        }
      case None => ApiErrorResponse.resourceNotFound(uuidNotFound(filterId))
    }
  }

  @PUT
  @Path("search/filter")
  @ApiOperation(
    value = "Update multiple MIME type filters",
    notes =
      "This endpoint is used to update multipe MIME type filters. A JSON object representing a collection of updated filters is returned if operation is successful.",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def batchUpdate(searchFilters: Array[SearchFilter]): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    val batchResponses = ListBuffer[ApiBatchOperationResponse]()

    searchFilters.foreach(searchFilter => {
      val response = validate(searchFilter) match {
        case Left(errors) =>
          ApiBatchOperationResponse(isFilterIdNull(searchFilter.getId), 400, errors.mkString(""))
        case Right(_) =>
          if (searchFilter.getId == null) {
            val filterId = UUID.randomUUID().toString
            searchFilter.setId(filterId)
            searchSettings.getFilters.add(searchFilter)
            ApiBatchOperationResponse(
              filterId,
              200,
              s"A new filter has been created. ID: $filterId"
            )
          } else {
            val filterId = searchFilter.getId
            getFilterById(filterId, searchSettings) match {
              case Some(filter) =>
                filter.setMimeTypes(searchFilter.getMimeTypes)
                filter.setName(searchFilter.getName)
                ApiBatchOperationResponse(
                  filterId,
                  200,
                  s"MIME type filter $filterId has been updated."
                )
              case None =>
                ApiBatchOperationResponse(filterId, 404, uuidNotFound(filterId))
            }
          }
      }
      batchResponses += response
    })
    updateSettings(searchSettings)
    Response.status(207).entity(batchResponses).build()
  }

  @DELETE
  @Path("search/filter/{uuid}")
  @ApiOperation(
    value = "Delete a MIME type filter",
    notes = "This endpoint is used to delete a MIME type filter."
  )
  def deleteSearchFilters(
      @ApiParam(value = "filter UUID") @PathParam("uuid") uuid: UUID
  ): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    val filterId       = uuid.toString
    getFilterById(filterId, searchSettings) match {
      case Some(filter) =>
        searchSettings.getFilters.remove(filter)
        updateSettings(searchSettings)
        Response.ok().build()
      case None => ApiErrorResponse.resourceNotFound(uuidNotFound(filterId))
    }
  }

  @DELETE
  @Path("search/filter/")
  @ApiOperation(
    value = "Delete multiple MIME type filter",
    notes = "This endpoint is used to delete multiple MIME type filter.",
    response = classOf[ApiBatchOperationResponse],
    responseContainer = "List"
  )
  def batchDelete(
      @ApiParam(value = "filter UUID") @QueryParam("ids") ids: Array[UUID]
  ): Response = {
    searchPrivProvider.checkAuthorised()
    val searchSettings = loadSettings(new SearchSettings)
    val batchResponses = ListBuffer[ApiBatchOperationResponse]()

    ids.foreach(id => {
      val filterId = id.toString
      val response = getFilterById(filterId, searchSettings) match {
        case Some(filter) =>
          searchSettings.getFilters.remove(filter)
          ApiBatchOperationResponse(filterId, 200, s"MIME type filter $filterId has been deleted.")
        case None => ApiBatchOperationResponse(filterId, 404, uuidNotFound(filterId))
      }
      batchResponses += response
    })

    updateSettings(searchSettings)
    Response.status(207).entity(batchResponses).build()
  }

  private def getFilterById(
      filterId: String,
      searchSettings: SearchSettings
  ): Option[SearchFilter] = {
    Option(searchSettings.getSearchFilter(filterId))
  }

  private def uuidNotFound(uuid: String) = s"No Search filters matching UUID: $uuid"

  private def isFilterIdNull(id: String): String = id match {
    case null => ""
    case _    => id
  }

  private def validate(searchFilter: SearchFilter): Either[Array[String], Unit] = {
    val errorMessages = ArrayBuffer[String]()
    val filterName    = searchFilter.getName
    val mimeTypes     = searchFilter.getMimeTypes

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
        mimeType.nonEmpty && !validMimeTypes.contains(mimeType)
      )
      if (invalidTypes.nonEmpty) {
        errorMessages += s"Invalid MIMETypes found : ${invalidTypes.mkString(",")}"
      }
    }

    if (errorMessages.nonEmpty) Left(errorMessages.toArray) else Right()
  }

  private def validMimeTypes: List[String] = {
    LegacyGuice.mimeTypeService
      .searchByMimeType(Constants.BLANK, 0, -1)
      .getResults
      .asScala
      .map(_.getType)
      .toList
  }
}
