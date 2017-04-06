package com.tle.core.services.item;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;

public interface ItemLockingService
{
	ItemLock lock(ItemKey itemId);

	ItemLock get(ItemKey itemId);

	boolean isLocked(Item item);

	ItemLock useLock(Item item, String lockId);

	void unlock(ItemLock itemLock);

	void unlock(ItemKey itemId);
}
