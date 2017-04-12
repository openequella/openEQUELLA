package com.tle.freetext;

import java.util.Collection;
import java.util.Date;

import com.tle.beans.Institution;
import com.tle.beans.item.ItemIdKey;

public interface BackgroundIndexer extends Runnable
{
	IndexedItem createIndexedItem(ItemIdKey key);

	void addToQueue(IndexedItem item);

	void addToQueue(ItemIdKey key, boolean newSearcher);

	void addAllToQueue(Collection<IndexedItem> items);

	void kill();

	IndexedItem getIndexedItem(ItemIdKey key);

	void synchronizeNew(Collection<Institution> institutions, Date since);

	void synchronizeFull(Collection<Institution> institutions);

	boolean isRoomForItems(int size);

}