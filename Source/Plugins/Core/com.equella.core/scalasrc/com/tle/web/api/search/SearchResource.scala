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

import com.tle.beans.item.ItemIdKey
import com.tle.common.search.DefaultSearch
import com.tle.core.item.serializer.ItemSerializerItemBean
import com.tle.core.services.item.{FreetextResult, FreetextSearchResults}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.search.SearchHelper._
import com.tle.web.api.search.model.{SearchParam, SearchResult, SearchResultItem}
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{BeanParam, GET, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

import scala.collection.JavaConverters._

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
    val searchResults: FreetextSearchResults[FreetextResult] =
      LegacyGuice.freeTextService.search(createSearch(params),
                                         params.start,
                                         params.length,
                                         params.searchAttachments)
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
