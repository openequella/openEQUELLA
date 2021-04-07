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

import com.dytech.devlib.PropBagEx
import com.tle.beans.entity.Schema
import com.tle.beans.item.ItemIdKey
import com.tle.common.search.DefaultSearch
import com.tle.core.item.serializer.ItemSerializerItemBean
import com.tle.core.services.item.{FreetextResult, FreetextSearchResults}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.search.ExportCSVHelper.{
  buildCSVHeaders,
  buildCSVRow,
  checkDownloadACL,
  convertSearchResultToXML,
  getSchemaFromCollection
}
import com.tle.web.api.search.SearchHelper._
import com.tle.web.api.search.model.{SearchParam, SearchResult, SearchResultItem}
import io.swagger.annotations.{Api, ApiOperation}

import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{BadRequestException, BeanParam, GET, NotFoundException, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

import java.io.BufferedOutputStream
import scala.collection.JavaConverters._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

@NoCache
@Path("search2")
@Produces(Array("application/json"))
@Api("Search V2")
class SearchResource {
  @GET
  @ApiOperation(
    value = "Search items",
    notes = "This endpoint is used to search for items based on specified criteria.",
    response = classOf[SearchResult[SearchResultItem]],
  )
  def searchItems(@BeanParam params: SearchParam): Response = {
    val searchResults           = search(params)
    val freetextResults         = searchResults.getSearchResults.asScala.toList
    val itemIds                 = freetextResults.map(_.getItemIdKey)
    val serializer              = createSerializer(itemIds)
    val items: List[SearchItem] = freetextResults.map(result => SearchItem(result, serializer))
    val highlight =
      new DefaultSearch.QueryParser(params.query).getHilightedList.asScala.toList
    val result = SearchResult(
      searchResults.getOffset,
      searchResults.getCount,
      searchResults.getAvailable,
      items.map(convertToItem),
      highlight
    )
    Response.ok.entity(result).build()
  }

  @GET
  @Produces(Array("text/csv"))
  @Path("/export")
  def export(@BeanParam params: SearchParam,
             @Context req: HttpServletRequest,
             @Context resp: HttpServletResponse) = {
    checkDownloadACL()

    if (params.collections.length != 1) {
      throw new BadRequestException("Only one Collection is allowed")
    }
    val collectionId = params.collections(0)
    val schema: Schema = getSchemaFromCollection(collectionId) match {
      case Some(s) => s
      case None =>
        throw new NotFoundException(s"Failed to find Schema for Collection: $collectionId")
    }

    resp.setContentType("text/csv")
    resp.setHeader("Content-Disposition", " attachment; filename=search.csv")
    LegacyGuice.auditLogService.logGeneric("Download",
                                           "SearchResult",
                                           "CSV",
                                           req.getQueryString,
                                           null,
                                           null)

    val bos                 = new BufferedOutputStream(resp.getOutputStream)
    val csvContentContainer = new StringBuilder

    def inputContents(contents: String*): Unit = {
      contents.foreach(c => csvContentContainer.append(c))
    }

    def outputContents(): Unit = {
      bos.write(csvContentContainer.toString().getBytes())
      bos.flush()
      csvContentContainer.clear()
    }

    def getXmlList: List[PropBagEx] = {
      convertSearchResultToXML(search(params, Option(-1)).getSearchResults.asScala.toList)
    }

    // Build the first row for headers.
    val csvHeaders = buildCSVHeaders(schema)
    csvHeaders.foreach(header => inputContents(header.name, ","))
    inputContents("\n") // Move to the second row.
    outputContents()

    // Build a row for each Item XML.
    getXmlList.foreach(xml => {
      inputContents(buildCSVRow(xml, csvHeaders), "\n")
      outputContents()
    })

    bos.close()
  }

  private def search(params: SearchParam,
                     length: Option[Int] = None): FreetextSearchResults[FreetextResult] =
    LegacyGuice.freeTextService.search(createSearch(params),
                                       params.start,
                                       length.getOrElse(params.length),
                                       params.searchAttachments)
}

/**
  * This class provides general information of an Item to be used inside a SearchResult.
  * @param idKey An ItemIdKey
  * @param bean An EquellaItemBean
  * @param keywordFound Indicates if a search term has been found inside attachment content
  */
case class SearchItem(idKey: ItemIdKey, bean: EquellaItemBean, keywordFound: Boolean)
object SearchItem {
  def apply(item: FreetextResult, serializer: ItemSerializerItemBean): SearchItem = {
    val keywordFoundInAttachment = item.isKeywordFoundInAttachment
    val itemId                   = item.getItemIdKey
    val itemBean                 = new EquellaItemBean
    itemBean.setUuid(itemId.getUuid)
    itemBean.setVersion(itemId.getVersion)
    serializer.writeItemBeanResult(itemBean, itemId.getKey)
    LegacyGuice.itemLinkService.addLinks(itemBean)
    SearchItem(itemId, itemBean, keywordFoundInAttachment)
  }
}
