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

package com.tle.web.api.favourite.model

import com.tle.core.favourites.bean.{FavouriteSearch => FavouriteSearchBean}

import java.util.Date

/** This model class includes favourite search information required in the favourites page. It also
  * supports the transformation from [[FavouriteSearchBean]]
  *
  * @param id
  *   ID of the favourite search.
  * @param name
  *   Name of the favourite search.
  * @param url
  *   The URL of the favourite search, which includes all query strings.
  * @param addedAt
  *   When was the search added to favourites.
  */
final case class FavouriteSearch(
    id: Long,
    name: String,
    url: String,
    addedAt: Date
)

/** Build Convert a FavouriteSearch bean to the API model FavouriteSearch.
  */
object FavouriteSearch {
  def apply(favouriteSearchBean: FavouriteSearchBean): FavouriteSearch =
    FavouriteSearch(
      id = favouriteSearchBean.getId,
      name = favouriteSearchBean.getName,
      url = favouriteSearchBean.getUrl,
      addedAt = favouriteSearchBean.getDateModified
    )
}
