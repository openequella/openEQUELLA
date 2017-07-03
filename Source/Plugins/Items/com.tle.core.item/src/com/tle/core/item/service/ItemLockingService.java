package com.tle.core.item.service;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;

public interface ItemLockingService
{
	ItemLock lock(ItemKey itemId);

	ItemLock lock(Item item);

	ItemLock get(ItemKey itemId);

	ItemLock get(Item item);

	boolean isLocked(Item item);

	ItemLock useLock(Item item, String lockId);

	void unlock(ItemLock itemLock);

	void unlock(ItemKey itemId);

	void unlock(Item item, boolean force);
}
