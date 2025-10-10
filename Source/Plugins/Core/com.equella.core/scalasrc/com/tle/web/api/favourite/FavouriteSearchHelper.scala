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

package com.tle.web.api.favourite

import com.tle.common.search.DefaultSearch
import com.tle.common.searching.Search.SortType
import com.tle.common.searching.SortField
import com.tle.common.searching.SortField.Type
import com.tle.core.favourites.SearchFavouritesSearchResults
import com.tle.web.api.favourite.model.{FavouriteSearch, FavouriteSearchPayload}
import com.tle.web.api.search.SearchHelper._
import com.tle.web.api.search.model.SearchResult

import scala.jdk.CollectionConverters._

/** Utility for converting legacy favourite-search results into API models and constructing search
  * queries for the FavouriteSearch API.
  */
object FavouriteSearchHelper {
  private final val ORDER_NAME = "name"

  /** Convert [[SearchFavouritesSearchResults]] to SearchResult[FavouriteSearch].
    *
    * @param query
    *   The query string used for get highlighted text.
    * @param legacySearchResults
    *   The legacy object SearchFavouritesSearchResults to convert.
    */
  def toSearchResults(
      query: Option[String],
      legacySearchResults: SearchFavouritesSearchResults
  ): SearchResult[FavouriteSearch] = SearchResult(
    legacySearchResults.getOffset,
    legacySearchResults.getCount,
    legacySearchResults.getAvailable,
    legacySearchResults.getResults.asScala.map(FavouriteSearch(_)).toList,
    getHighlightedList(query)
  )

  /** Create a search object for searching “favourite search” from the favourite API payload.
    */
  def createSearch(payload: FavouriteSearchPayload): DefaultSearch = {
    val search = new DefaultSearch()
    search.setQuery(payload.query.orNull)
    setDateRange(search, payload.addedAfter, payload.addedBefore)
    val sort = payload.order.map(_.toLowerCase) match {
      case Some(ORDER_NAME) => SortType.NAME.getSortField()
      case _                => SortType.ADDEDAT.getSortField()
    }
    search.setSortFields(sort)
    search
  }
}
