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

package com.tle.core.favourites.service;

import com.tle.common.searching.Search;
import com.tle.core.favourites.SearchFavouritesSearchResults;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.exceptions.AuthenticationException;
import com.tle.web.sections.SectionInfo;
import java.util.List;

public interface FavouriteSearchService {
  FavouriteSearch getById(long id);

  /**
   * Checks if the current user is the owner of the given favourite search.
   *
   * @param favouriteSearch the favourite search to check.
   */
  boolean isOwner(FavouriteSearch favouriteSearch);

  /** Delete the favourite search if the current user is the owner. */
  void deleteIfOwned(long id);

  /**
   * Save a search definition to user's search favourites.
   *
   * @param search A search definition to be saved to database.
   * @return A FavouriteSearch instance of the new entry.
   * @throws AuthenticationException If the current user is a guest (unauthenticated) he is not
   *     allowed to favourite searches.
   */
  FavouriteSearch save(FavouriteSearch search);

  SearchFavouritesSearchResults search(Search search, int offset, int perPage);

  void deleteById(long id);

  void executeSearch(SectionInfo info, long id);

  List<FavouriteSearch> getSearchesForOwner(String userID, int maxResults);
}
