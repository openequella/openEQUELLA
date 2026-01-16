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

package com.tle.search

import com.dytech.edge.queries.FreeTextQuery.{FIELD_BOOKMARK_OWNER, FIELD_BOOKMARK_TAGS}
import com.tle.common.search.DefaultSearch
import com.tle.common.searching.Field
import com.tle.common.usermanagement.user.CurrentUser

import java.util.Collections

/** Search class to retrieve favourite items for the current user. Manly used for the legacy UI.
  */
@SerialVersionUID(1L)
class FavouritesSearch extends DefaultSearch {
  // Add owner must field to only get current user's favourites.
  override def addExtraMusts(musts: java.util.List[java.util.List[Field]]): Unit = {
    val ownerField =
      new Field(FIELD_BOOKMARK_OWNER, CurrentUser.getUserID)
    musts.add(Collections.singletonList(ownerField))
  }

  // Search in bookmark tags field.
  override def getExtraQueries: java.util.List[String] =
    Collections.singletonList(String.format("%s:(%s)", FIELD_BOOKMARK_TAGS, getQuery))
}

/** Companion object for FavouritesSearch includes helper methods used in new UI.
  */
object FavouritesSearch {

  /** Remove favourite tags search query "OR bookmark_tags(...)" parts from the query string.
    *
    * In the legacy UI, it uses the `getExtraQueries` to add the bookmark_tags query, but in new UI,
    * it's added directly to the query string, and for some cases we need to remove it when
    * processing the query.
    *
    * Example: "apple* OR bookmark_tags(apple*)" -> "apple*"
    */
  def removeBookmarkTagsQuery(query: String): String = {
    val regex = s"""(?i)\\s+OR\\s+${FIELD_BOOKMARK_TAGS}:\\([^)]*\\)"""
    Option(query).map((q: String) => q.replaceAll(regex, "")).getOrElse("")
  }
}
