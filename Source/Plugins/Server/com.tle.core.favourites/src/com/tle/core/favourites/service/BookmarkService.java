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
