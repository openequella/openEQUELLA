package com.tle.core.favourites.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

public interface BookmarkDao extends GenericInstitutionalDao<Bookmark, Long>
{
	Bookmark getByItemAndUserId(String userId, ItemKey itemId);

	Bookmark findById(long id);

	List<Bookmark> listAll();

	void deleteAll();

	void deleteAllForUser(String user);

	void changeOwnership(String fromUser, String toUser);

	Collection<Bookmark> getAllMentioningItem(Item item);

	List<Item> updateAlwaysLatest(Item item);

	List<Item> filterNonBookmarkedItems(Collection<Item> items);

	Map<Long, List<Bookmark>> getBookmarksForIds(Collection<Long> ids);

	Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items, String userId);
}
