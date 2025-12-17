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

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.exceptions.AuthenticationException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookmarkService {
  /**
   * Add a bookmark for the current user.
   *
   * @throws AuthenticationException If the current user is a guest (unauthenticated) he is not
   *     allowed to favourite items.
   */
  Bookmark add(Item item, Set<String> tags, boolean latest);

  void delete(long id);

  /** Delete the favourite item if the current user is the owner. */
  void deleteIfOwned(long id);

  /** Get the current user's bookmark for the given item. */
  Bookmark getByItem(ItemKey itemId);

  Bookmark getById(long id);

  /** Return a set of items that are not bookmarked by the current user. */
  List<Item> filterNonBookmarkedItems(Collection<Item> items);

  Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items);

  List<Bookmark> getBookmarksForOwner(String ownerUuid, int maxResults);

  /**
   * Checks if the current user is the owner of the given bookmark.
   *
   * @param favouriteItem the favourite item to check.
   */
  boolean isOwner(Bookmark favouriteItem);
}
