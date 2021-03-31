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
import com.tle.beans.item.ItemIdKey
import com.tle.common.search.DefaultSearch
import com.tle.core.item.serializer.ItemSerializerItemBean
import com.tle.core.security.impl.RequiresPrivilege
import com.tle.core.services.item.{FreetextResult, FreetextSearchResults}
import com.tle.exceptions.PrivilegeRequiredException
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.search.ExportCSVHelper.{
  NEED_FULL_XPATH_IN_CONTENT,
  STANDARD_HEADER_LIST,
  buildCSVCell,
  buildHeadersForSchema,
  checkCollectionNumber,
  checkDownloadACL,
  convertSearchResultToXML
}
import com.tle.web.api.search.SearchHelper._
import com.tle.web.api.search.model.{SearchParam, SearchResult, SearchResultItem}
import io.swagger.annotations.{Api, ApiOperation}
import org.apache.commons.lang.StringEscapeUtils

import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{BeanParam, GET, NotAcceptableException, Path, Produces}
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
    SearchResult(
      searchResults.getOffset,
      searchResults.getCount,
      searchResults.getAvailable,
      items.map(convertToItem),
      highlight
    )
    Response.ok.entity().build()
  }

  @GET
  @Produces(Array("text/csv"))
  @Path("/export")
  def export(@BeanParam params: SearchParam,
             @Context req: HttpServletRequest,
             @Context resp: HttpServletResponse) = {
    checkDownloadACL()
    checkCollectionNumber(params.collections)
    req.getHeader("accept") match {
      case "text/csv"         => exportCSV(params, req, resp)
      case "application/json" => // todo
      case _                  => throw new NotAcceptableException("Only support CSV format")
    }
  }

  private def exportCSV(params: SearchParam,
                        req: HttpServletRequest,
                        resp: HttpServletResponse): Unit = {
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
      csvContentContainer.clear()
    }

    def getXmlList: List[PropBagEx] = {
      convertSearchResultToXML(search(params, Option(-1)).getSearchResults.asScala.toList)
    }

    def buildCSVHeaders: List[CSVHeader] = {
      val schema  = LegacyGuice.itemDefinitionService.getByUuid(params.collections(0)).getSchema
      val headers = buildHeadersForSchema(schema.getRootSchemaNode.getChildNodes, None) ++ STANDARD_HEADER_LIST
      headers.foreach(header => {
        inputContents(header.name, ",")
      })
      inputContents("\n")
      outputContents()
      headers
    }

    def buildCSVRow(xml: PropBagEx, headers: List[CSVHeader]): Unit = {
      headers.foreach(header => {
        // Regex for XPATH that points to an attribute(e.g. item/@name).
        val xpathAttributeRegex = """^.+/@.+$""".r
        // If the xpath points to an attribute, read the value directly.
        val cellContent: String = header.xpath match {
          case xpathAttributeRegex() => xml.getNode(header.xpath)
          case _ =>
            xml
              .iterator(header.xpath) // This is a PropBagIterator.
              .iterator() // So we have to call 'iterator' again.
              .asScala
              .map(rootNode => {
                buildCSVCell(rootNode,
                             parentNodeName =
                               Option(header.name).filter(NEED_FULL_XPATH_IN_CONTENT.contains))
              })
              .toList
              .mkString("|")
        }

        // Add a comma to separate each cell.
        inputContents(StringEscapeUtils.escapeCsv(cellContent), ",")
      })
      // Add a new row for next Item.
      inputContents("\n")
      outputContents()
    }

    val csvHeaders = buildCSVHeaders
    getXmlList.foreach(xml => buildCSVRow(xml, csvHeaders))

    resp.setHeader("Content-Disposition", " attachment; filename=search.csv")
    bos.flush()
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
