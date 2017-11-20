/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;

public interface BookmarkService
{
	void add(Item item, String tags, boolean latest);

	void delete(long id);

	Bookmark getByItem(ItemKey itemId);

	/**
	 * Return a set of items that are not bookmarked by the current user.
	 */
	List<Item> filterNonBookmarkedItems(Collection<Item> items);

	Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items);

	List<Bookmark> getBookmarksForOwner(String ownerUuid, int maxResults);
}
