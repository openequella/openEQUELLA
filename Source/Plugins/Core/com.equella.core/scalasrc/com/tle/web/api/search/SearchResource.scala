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
import com.tle.common.searching.SearchResults
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.api.search.model.{SearchParam, SearchResult}
import com.tle.web.api.search.SearchHelper._
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.{BeanParam, GET, Path, Produces}
import javax.ws.rs.core.Response
import org.jboss.resteasy.annotations.cache.NoCache

import scala.collection.JavaConverters._

@NoCache
@Path("search2")
@Produces(Array("application/json"))
@Api("Search V2")
class SearchResource {

  @GET
  @ApiOperation(
    value = "List items",
    notes = "This endpoint is used to retrieve items.",
    response = classOf[SearchResult],
  )
  def searchItems(@BeanParam params: SearchParam): Response = {
    val searchResults: SearchResults[ItemIdKey] =
      LegacyGuice.freeTextService.searchIds(createSearch(params), params.start, params.length)
    val itemIds: List[ItemIdKey] = searchResults.getResults.asScala.toList
    val serializer               = createSerializer(itemIds)
    val items: List[(ItemIdKey, EquellaItemBean)] = for { itemId <- itemIds } yield {
      val itemBean = new EquellaItemBean
      itemBean.setUuid(itemId.getUuid)
      itemBean.setVersion(itemId.getVersion)
      serializer.writeItemBeanResult(itemBean, itemId.getKey)
      LegacyGuice.itemLinkService.addLinks(itemBean)
      itemId -> itemBean
    }
    val result = SearchResult(searchResults.getOffset,
                              searchResults.getCount,
                              searchResults.getAvailable,
                              items.map(convertToItem))

    Response.ok.entity(result).build()
  }
}
