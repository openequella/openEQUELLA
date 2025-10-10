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

package com.tle.web.api.search

import cats.implicits.toTraverseOps
import com.dytech.edge.exceptions.InvalidSearchQueryException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.tle.beans.entity.Schema
import com.tle.common.i18n.CurrentLocale
import com.tle.common.search.{DefaultSearch, PresetSearch}
import com.tle.common.security.SecurityConstants
import com.tle.core.auditlog.AuditLogService
import com.tle.core.collection.service.ItemDefinitionService
import com.tle.core.freetext.service.FreeTextService
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.remoting.MatrixResults
import com.tle.core.security.TLEAclManager
import com.tle.exceptions.PrivilegeRequiredException
import com.tle.web.api.ApiErrorResponse
import com.tle.web.api.browsehierarchy.{BrowseHierarchyHelper, HierarchyCompoundUuid}
import com.tle.web.api.search.ExportCSVHelper.{buildCSVHeaders, writeRow}
import com.tle.web.api.search.SearchHelper._
import com.tle.web.api.search.model._
import com.tle.web.api.search.service.ExportService
import io.swagger.annotations.{Api, ApiOperation}
import org.jboss.resteasy.annotations.cache.NoCache
import org.slf4j.{Logger, LoggerFactory}

import java.io.BufferedOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.{Inject, Singleton}
import javax.servlet.http.HttpServletResponse
import javax.ws.rs._
import javax.ws.rs.core.{Context, Response}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Bind
@Singleton
@NoCache
@Path("search2")
@Produces(Array("application/json"))
@Api("Search V2")
class SearchResource {
  val Logger: Logger = LoggerFactory.getLogger(getClass)

  @Inject private var browseHierarchyHelper: BrowseHierarchyHelper = _
  @Inject private var hierarchyService: HierarchyService           = _
  @Inject private var auditLogService: AuditLogService             = _
  @Inject private var exportService: ExportService                 = _
  @Inject private var itemDefinitionService: ItemDefinitionService = _
  @Inject private var aclManager: TLEAclManager                    = _
  @Inject private var freetextService: FreeTextService             = _

  @GET
  @ApiOperation(
    value = "Search items",
    notes = "This endpoint is used to search for items based on specified criteria.",
    response = classOf[SearchResult[SearchResultItem]]
  )
  def searchItems(@BeanParam params: SearchParam): Response = {
    val searchPayload = SearchPayload(params)
    doSearch(searchPayload)
  }

  @GET
  @Path("/facet")
  @ApiOperation(
    value = "Faceted search",
    notes =
      "This endpoint is used to a faceted search and returns a list of terms and the number of Items matching the terms by a list of Schema nodes and general search queries.",
    response = classOf[FacetedSearchResult]
  )
  def searchFacet(@BeanParam params: FacetedSearchParam): Response = {
    val searchPayload = SearchPayload(params)

    searchPayload.hierarchy.map(createPresetSearch).sequence match {
      case Right(hierarchySearch) =>
        val search = createSearch(searchPayload, None, hierarchySearch)
        doFacetSearch(search, params)
      case Left(errorMessage) =>
        ApiErrorResponse.resourceNotFound(s"Failed to get facet search result: $errorMessage")
    }
  }

  @POST
  @ApiOperation(
    value = "Search items - typically used to perform a search with large search criteria",
    notes =
      "This endpoint supports searching for items based on large search criteria such a hundreds of MIME types.",
    response = classOf[SearchResult[SearchResultItem]]
  )
  def searchItemsPostVersion(payload: SearchPayload): Response = {
    doSearch(payload)
  }

  @POST
  @Path("/advanced")
  @ApiOperation(
    value = "Search items with Advanced search criteria",
    notes =
      "This endpoint is used to search for items based on specified criteria, including Advanced search criteria.",
    response = classOf[SearchResult[SearchResultItem]]
  )
  def searchItemsWithAdvCriteria(
      @BeanParam params: SearchParam,
      advancedSearchCriteria: AdvancedSearchParameters
  ): Response = {
    val searchPayload                      = SearchPayload(params)
    val AdvancedSearchParameters(criteria) = advancedSearchCriteria
    getSearchResult(createSearch(searchPayload, Option(criteria)), searchPayload)
  }

  @HEAD
  @Path("/export")
  def exportCSV(@BeanParam params: SearchParam): Response = {
    confirmExport(params)
    Response.ok().build()
  }

  @GET
  @Produces(Array("text/csv"))
  @Path("/export")
  def exportCSV(@BeanParam params: SearchParam, @Context resp: HttpServletResponse): Unit = {
    val schema = confirmExport(params)
    auditLogService.logSearchExport("CSV", convertParamsToJsonString(params))

    resp.setContentType("text/csv")
    resp.setHeader("Content-Disposition", " attachment; filename=search.csv")
    val bos = new BufferedOutputStream(resp.getOutputStream)

    // Build the first row for headers.
    val csvHeaders = buildCSVHeaders(schema)
    writeRow(bos, s"${csvHeaders.map(c => c.name).mkString(",")}")

    exportService.export(
      createSearch(SearchPayload(params)),
      params.searchAttachments,
      csvHeaders,
      writeRow(bos, _)
    )

    bos.close()
  }

  private def convertParamsToJsonString(params: SearchParam): String = {
    val mapper = JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .build()

    mapper.writeValueAsString(params)
  }

  // Check ACL, number of Collections and whether Schema of the Collection can be found.
  // Return the Schema if all checks pass.
  private def confirmExport(params: SearchParam): Schema = {
    if (params.collections.length != 1) {
      throw new BadRequestException("Download limited to one collection.")
    }

    val collectionId = params.collections(0)
    val collection = Option(itemDefinitionService.getByUuid(collectionId)) match {
      case Some(c) => c
      case None => throw new NotFoundException(s"Failed to find Collection for ID: $collectionId")
    }

    if (
      aclManager
        .filterNonGrantedPrivileges(collection, SecurityConstants.EXPORT_SEARCH_RESULT)
        .isEmpty
    ) {
      throw new PrivilegeRequiredException(SecurityConstants.EXPORT_SEARCH_RESULT)
    }

    Option(collection.getSchema) match {
      case Some(s) => s
      case None =>
        throw new NotFoundException(
          s"Failed to find Schema for Collection: ${CurrentLocale.get(collection.getName)}"
        )
    }
  }

  // create a PresetSearch for a hierarchy search
  private def createPresetSearch(compoundUuidStr: String): Either[String, PresetSearch] = {
    def buildSearch(compoundUuid: HierarchyCompoundUuid) =
      Option(hierarchyService.getHierarchyTopicByUuid(compoundUuid.uuid)) match {
        case Some(topic) =>
          val fullUuidNameMap = compoundUuid.getAllVirtualHierarchyMap.asJava
          Right(hierarchyService.buildSearch(topic, fullUuidNameMap))
        case None =>
          Left(s"Failed to get preset search: Topic $compoundUuidStr not found.")
      }

    HierarchyCompoundUuid(compoundUuidStr) match {
      case Right(compoundUuid) => buildSearch(compoundUuid)
      case Left(e) =>
        Left(s"Failed to parse hierarchy compound UUID ${compoundUuidStr}: ${e.getMessage}")
    }
  }

  private def getSearchResult(searchRequest: DefaultSearch, payload: SearchPayload): Response = {
    Try {
      val searchResults =
        search(searchRequest, payload.start, payload.length, payload.searchAttachments)

      val freetextResults         = searchResults.getSearchResults.asScala.toList
      val itemIds                 = freetextResults.map(_.getItemIdKey)
      val serializer              = createSerializer(itemIds)
      val items: List[SearchItem] = freetextResults.map(result => SearchItem(result, serializer))
      val highlight               = getHighlightedList(payload.query)

      SearchResult(
        searchResults.getOffset,
        searchResults.getCount,
        searchResults.getAvailable,
        items.map(convertToItem(_, payload.includeAttachments)),
        highlight
      )
    } match {
      case Success(searchResult) => Response.ok.entity(searchResult).build()
      case Failure(_: InvalidSearchQueryException) =>
        ApiErrorResponse.badRequest("Invalid search query - please remove any special characters.")
      case Failure(e) => throw e
    }
  }

  private def doSearch(searchPayload: SearchPayload): Response = {
    searchPayload.hierarchy
      .map(URLDecoder.decode(_, StandardCharsets.UTF_8))
      .map(createPresetSearch)
      .sequence match {
      case Right(hierarchySearch) =>
        val search = createSearch(searchPayload, None, hierarchySearch)
        getSearchResult(search, searchPayload)
      case Left(errorMessage) =>
        ApiErrorResponse.resourceNotFound(s"Failed to get search result: $errorMessage")
    }
  }

  private def doFacetSearch(search: DefaultSearch, params: FacetedSearchParam) = {
    val matrixResults: MatrixResults =
      freetextService.matrixSearch(search, params.nodes.toList.asJava, true, true)
    val results = matrixResults.getEntries.asScala
      .map(matrixEntry =>
        FacetedResultItem(
          term = matrixEntry.getFieldValues.toArray.mkString(","),
          count = matrixEntry.getCount
        )
      )
      .toList
    Response.ok.entity(FacetedSearchResult(results)).build()
  }
}
